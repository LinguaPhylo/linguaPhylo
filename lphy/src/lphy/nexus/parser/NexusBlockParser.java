package lphy.nexus.parser;

import lphy.evolution.alignment.Alignment;
import lphy.nexus.parser.datatype.DataType;
import lphy.nexus.parser.datatype.Nucleotides;
import lphy.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modified from BEAST 2
 */
public class NexusBlockParser {
    /**
     * keep track of nexus file line number, to report when the file does not parse *
     */
    protected int lineNr;

    public NexusBlockParser() {
        this.lineNr = 0;
    }

    public int getLineNr() {
        return lineNr;
    }

    public void plus1LineNr() {
        this.lineNr += 1;
    }

    /**
     * read next line from nexus file that is not a comment and not empty *
     */
    public String nextLine(final BufferedReader reader) throws IOException {
        String str = reader.readLine();
        lineNr++;
        if (str == null) return null;

        if (str.contains("[")) {
            final int start = str.indexOf('[');
            int end = str.indexOf(']', start);
            while (end < 0) {
                str += reader.readLine();
                end = str.indexOf(']', start);
            }
            str = str.substring(0, start) + str.substring(end + 1);
            if (str.matches("^\\s*$")) {
                return nextLine(reader);
            }
        }
        if (str.matches("^\\s*$")) {
            return nextLine(reader);
        }
        return str;
    }


    /**
     * Parse taxa block and add to taxa and taxonList.
     */
    public List<String> parseTaxaBlock(final BufferedReader reader) throws IOException {
        List<String> taxaNames = new ArrayList<>();
        int expectedTaxonCount = -1;
        NexusCommand nextCommand;
        do {
            nextCommand = readNextCommand(reader);
            if (nextCommand.isCommand("dimensions")) {
                if (nextCommand.getKeyValueArgs().get("ntax") != null)
                    expectedTaxonCount = Integer.parseInt(nextCommand.getKeyValueArgs().get("ntax"));
            } else if (nextCommand.isCommand("taxlabels")) {

                List<String> labels = nextCommand.getArgList();

                for (String taxonString : labels) {
                    taxonString = stripNexusComments(taxonString).trim();

                    if (taxonString.isEmpty())
                        continue;

                    if (taxonString.charAt(0) == '\'' || taxonString.charAt(0) == '\"')
                        taxonString = taxonString.substring(1, taxonString.length() - 1).trim();

                    if (taxonString.isEmpty())
                        continue;

                    if (!taxaNames.contains(taxonString))
                        taxaNames.add(taxonString);

//                    if (!taxonListContains(taxonString))
////                        taxonList.add(new Taxon(taxonString));
//                        taxonList.add(taxonString);

                }
            }
        } while (!nextCommand.isEndOfBlock());
        if (expectedTaxonCount >= 0 && taxaNames.size() != expectedTaxonCount) {
            throw new IOException("Number of taxaNames (" + taxaNames.size() + ") is not equal to 'dimension' " +
                    "field (" + expectedTaxonCount + ") specified in 'taxaNames' block");
        }

        return taxaNames;
    }

    /**
     * Get next nexus command, if available.
     *
     * @param fin nexus file reader
     * @return nexus command, or null if none available.
     * @throws IOException if error reading from file
     */
    NexusCommand readNextCommand(BufferedReader fin) throws IOException {
        StringBuilder commandBuilder = new StringBuilder();

        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;

            char nextChar = (char)nextVal;
            if (nextChar == ';')
                break;

            commandBuilder.append(nextChar);

            switch (nextChar) {
                case '[':
                    readNexusComment(fin, commandBuilder);
                    break;

                case '"':
                case '\'':
                    readNexusString(fin, commandBuilder, nextChar);
                    break;

                case '\n':
                    lineNr += 1;
                    break;

                default:
                    break;
            }
        }

        if (commandBuilder.toString().isEmpty())
            return null;
        else
            return new NexusCommand(commandBuilder.toString());
    }

    /**
     * Remove nexus comments from a given string.
     *
     * @param string input string
     * @return string with nexus comments removed.
     */
    String stripNexusComments(String string) {
        return string.replaceAll("\\[[^]]*]","");
    }

    /**
     * Used to advance reader past nexus strings.
     *
     * @param fin intput file reader
     * @param builder string builder where characters read are to be appended
     * @param stringDelim string delimiter
     *
     * @throws IOException on unterminated string
     */
    private void readNexusString(BufferedReader fin, StringBuilder builder, char stringDelim) throws IOException {
        boolean stringTerminated = false;
        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;

            char nextChar = (char)nextVal;

            builder.append(nextChar);

            if (nextChar == stringDelim) {
                stringTerminated = true;
                break;
            }

            if (nextChar == '\n')
                lineNr += 1;
        }

        if (!stringTerminated)
            throw new IOException("Unterminated string.");
    }

    /**
     * Used to advance reader past nexus comments. Comments may themselves
     * contain strings.
     *
     * @param fin intput file reader
     * @param builder string builder where characters read are to be appended
     *
     * @throws IOException on unterminated comment.
     */
    private void readNexusComment(BufferedReader fin, StringBuilder builder) throws IOException {
        boolean commentTerminated = false;
        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;


            char nextChar = (char)nextVal;
            builder.append(nextChar);

            if (nextChar == ']') {
                commentTerminated = true;
                break;
            }

            if (nextChar == '"' || nextChar == '\'')
                readNexusString(fin, builder, nextChar);

            if (nextChar == '\n')
                lineNr += 1;
        }

        if (!commentTerminated)
            throw new IOException("Unterminated comment.");
    }


    /**
     * parse data block and create Alignment *
     */
    public Alignment parseDataBlock(final BufferedReader fin) throws IOException {

        String str;
        int taxonCount = -1;
        int charCount = -1;
        int totalCount = 4;
        String missing = "?";
        String gap = "-";
        // indicates character matches the one in the first sequence
        String matchChar = null;
        do {
            str = nextLine(fin);

            //dimensions ntax=12 nchar=898;
            if (str.toLowerCase().contains("dimensions")) {
                str = getNextDataBlock(str, fin);

                final String character = getAttValue("nchar", str);
                if (character == null) {
                    throw new IOException("nchar attribute expected (e.g. 'dimensions nchar=123') expected, not " + str);
                }
                charCount = Integer.parseInt(character);
                final String taxa = getAttValue("ntax", str);
                if (taxa != null) {
                    taxonCount = Integer.parseInt(taxa);
                }
            } else if (str.toLowerCase().contains("format")) {
                str = getNextDataBlock(str, fin);

                //format datatype=dna interleave=no gap=-;
                final String dataTypeName = getAttValue("datatype", str);
                final String symbols;
                if (getAttValue("symbols", str) == null) {
                    symbols = getAttValue("symbols", str);
                } else {
                    symbols = getAttValue("symbols", str).replaceAll("\\s", "");
                }
                if (dataTypeName == null) {
                    throw new UnsupportedOperationException("datatype = " + dataTypeName);
//                    Log.warning.println("Warning: expected datatype (e.g. something like 'format datatype=dna;') not '" + str + "' Assuming integer dataType");
//                    alignment.dataTypeInput.setValue("integer", alignment);
//                    if (symbols != null && (symbols.equals("01") || symbols.equals("012"))) {
//                        totalCount = symbols.length();
//                    }
                } else if (dataTypeName.toLowerCase().equals("rna") || dataTypeName.toLowerCase().equals("dna") || dataTypeName.toLowerCase().equals("nucleotide")) {
//                    alignment.dataTypeInput.setValue("nucleotide", alignment);
                    totalCount = 4;
                } else if (dataTypeName.toLowerCase().equals("aminoacid") || dataTypeName.toLowerCase().equals("protein")) {
//                    alignment.dataTypeInput.setValue("aminoacid", alignment);
                    totalCount = 20;
                } else if (dataTypeName.toLowerCase().equals("standard")) {
//                    alignment.dataTypeInput.setValue("standard", alignment);
                    totalCount = symbols.length();
                } else if (dataTypeName.toLowerCase().equals("binary")) {
//                    alignment.dataTypeInput.setValue("binary", alignment);
                    totalCount = 2;
                } else {
//                    alignment.dataTypeInput.setValue("integer", alignment);
                    if (symbols != null && (symbols.equals("01") || symbols.equals("012"))) {
                        totalCount = symbols.length();
                    }
                }
                final String missingChar = getAttValue("missing", str);
                if (missingChar != null) {
                    missing = missingChar;
                }
                final String gapChar = getAttValue("gap", str);
                if (gapChar != null) {
                    gap = gapChar;
                }
                matchChar = getAttValue("matchchar", str);
            }
        } while (!str.trim().toLowerCase().startsWith("matrix") && !str.toLowerCase().contains("charstatelabels"));

//        if (alignment.dataTypeInput.get().equals("standard")) {
//            StandardData type = new StandardData();
//            type.setInputValue("nrOfStates", totalCount);
//            //type.setInputValue("symbols", symbols);
//            type.initAndValidate();
//            alignment.setInputValue("userDataType", type);
//        }

        //reading CHARSTATELABELS block
        if (str.toLowerCase().contains("charstatelabels")) {
//            if (!alignment.dataTypeInput.get().equals("standard")) {
//                throw new IllegalArgumentException("If CHARSTATELABELS block is specified then DATATYPE has to be Standard");
//            }
//            StandardData standardDataType = (StandardData)alignment.userDataTypeInput.get();
//            int[] maxNumberOfStates = new int[] {0};
//            ArrayList<String> tokens = readInCharstatelablesTokens(fin);
//            ArrayList<UserDataType> charDescriptions = processCharstatelabelsTokens(tokens, maxNumberOfStates);
//
//            standardDataType.setInputValue("charstatelabels", charDescriptions);
//            standardDataType.setInputValue("nrOfStates", Math.max(maxNumberOfStates[0], totalCount));
//            standardDataType.initAndValidate();
//            for (UserDataType dataType : standardDataType.charStateLabelsInput.get()) {
//                dataType.initAndValidate();
//            }
        }

        //skipping before MATRIX block
        while (!str.toLowerCase().contains(("matrix"))) {
            str = nextLine(fin);
        }

        //TODO replace seqMap to idMap and int[][]
        final Map<String, StringBuilder> seqMap = new HashMap<>();
        final List<String> taxa = new ArrayList<>();
        String prevTaxon = null;
        int seqLen = 0;
        while (true) {
            str = nextLine(fin);

            int start = 0, end;
            final String taxon;
            while (Character.isWhitespace(str.charAt(start))) {
                start++;
            }
            if (str.charAt(start) == '\'' || str.charAt(start) == '\"') {
                final char c = str.charAt(start);
                start++;
                end = start;
                while (str.charAt(end) != c) {
                    end++;
                }
                taxon = str.substring(start, end).trim();
                seqLen = 0;
                end++;
            } else {
                end = start;
                while (end < str.length() && !Character.isWhitespace(str.charAt(end))) {
                    end++;
                }
                if (end < str.length()) {
                    taxon = str.substring(start, end).trim();
                    seqLen = 0;
                } else if ((prevTaxon == null || seqLen == charCount) && end == str.length()) {
                    taxon = str.substring(start, end).trim();
                    seqLen = 0;
                } else {
                    taxon = prevTaxon;
                    if (taxon == null) {
                        throw new IOException("Could not recognise taxon");
                    }
                    end = start;
                }
            }
            prevTaxon = taxon;
            String data = str.substring(end);
            for (int k = 0; k < data.length(); k++) {
                if (!Character.isWhitespace(data.charAt(k))) {
                    seqLen++;
                }
            }

            data = data.replaceAll(";", "");
            if (data.trim().length() > 0) {
                if (seqMap.containsKey(taxon)) {
                    seqMap.put(taxon, seqMap.get(taxon).append(data));
                } else {
                    seqMap.put(taxon, new StringBuilder(data));
                    taxa.add(taxon);
                }
            }
            if (str.contains(";")) {
                break;
            }

        } //TODO why seqLen == 0 in the end?

        if (taxonCount > 0 && taxa.size() != taxonCount) {
            throw new IOException("dimensions block says there are " + taxonCount +
                    " taxa, but there were " + taxa.size() + " taxa found");
        }

        //****** create alignment here
        Map<String, Integer> idMap = new TreeMap<>();
        // TODO could be wrong
        int id = 0;
        for (Map.Entry<String, StringBuilder> entry : seqMap.entrySet()) {
            idMap.put(entry.getKey(), id++);
        }
//        seqLen = seqMap.get(taxa.get(0)).length(); // ? seqLen != charCount here
//        alignment[idMap.get(taxon)][position] = state;
        final Alignment alignment = new Alignment(taxonCount, charCount, idMap, totalCount);

        //****** parse sequences into int
        HashSet<String> sortedAmbiguities = new HashSet<>();
        for (final String taxon : taxa) {
//            if (!taxonListContains(taxon)) {
//                taxonList.add(new Taxon(taxon));
//            }
            final StringBuilder bsData = seqMap.get(taxon);
            String sequence = solveUncertChar(taxon, bsData, missing, gap, sortedAmbiguities);

            //check the length of the sequence (treat ambiguity sets as single characters)
            if (sequence.length() != charCount) {
                throw new IOException("Expected sequence of length " + charCount + " instead of " +
                        sequence.length() + " for taxon " + taxon);
            }

            seqMap.put(taxon, new StringBuilder(sequence));

            // resolve matching char, if any
            if (matchChar != null && sequence.contains(matchChar)) {
                final char cMatchChar = matchChar.charAt(0);
                final String baseData = seqMap.get(taxa.get(0)).toString();
                for (int i = 0; i < sequence.length(); i++) {
                    if (sequence.charAt(i) == cMatchChar) {
                        final char cReplaceChar = baseData.charAt(i);
                        sequence = sequence.substring(0, i) + cReplaceChar +
                                (i + 1 < sequence.length() ? sequence.substring(i + 1) : "");
                    }
                }
            }

            //TODO
            DataType dataType = Nucleotides.INSTANCE;

            assert charCount == sequence.length();
            for (int i = 0; i < sequence.length(); i++){
                char c = sequence.charAt(i);
                int state = dataType.getState(c);
                alignment.setState(taxon, i, state);
            }

        }

        return alignment;
    } // parseDataBlock

    private String solveUncertChar(final String taxon, final StringBuilder bsData, final String missing,
                                   final String gap, HashSet<String> sortedAmbiguities)
            throws IOException {
        String data = bsData.toString().replaceAll("\\s", "");

        //collect all ambiguities in the sequence
        List<String> ambiguities = new ArrayList<>();
        Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(data);
        while (m.find()) {
            int mLength = m.group().length();
            ambiguities.add(m.group().substring(1, mLength-1));
        }

//TODO check with b1 DataType
        //sort elements of ambiguity sets
        String data_without_ambiguities = data.replaceAll("\\{(.*?)\\}", "?");
        for (String amb : ambiguities) {
            List<Integer> ambInt = new ArrayList<>();
            for (int i=0; i<amb.length(); i++) {
                char c = amb.charAt(i);
                if (c >= '0' && c <= '9') {
                    ambInt.add(Integer.parseInt(amb.charAt(i) + ""));
                } else {
                    // ignore
                    if (data != data_without_ambiguities) {
                        LoggerUtils.log.warning("Ambiguity found in " + taxon + " that is treated as missing value");
                    }
                    data = data_without_ambiguities;
                }
            }
            Collections.sort(ambInt);
            String ambStr = "";
            for (int i=0; i<ambInt.size(); i++) {
                ambStr += Integer.toString(ambInt.get(i));
            }
            sortedAmbiguities.add(ambStr);
        }

        // map to standard missing and gap chars
        data = data.replace(missing.charAt(0), DataType.UNKNOWN_CHARACTER);
        data = data.replace(gap.charAt(0), DataType.GAP_CHARACTER);

        return data;
    }

    /**
     * return attribute value as a string *
     */
    protected String getAttValue(final String attribute, final String str) {
        final Pattern pattern = Pattern.compile(".*" + attribute + "\\s*=\\s*([^\\s;]+).*");
        final Matcher matcher = pattern.matcher(str.toLowerCase());
        if (!matcher.find()) {
            return null;
        }
        String att = matcher.group(1);
        if (att.startsWith("\"") && att.endsWith("\"")) {
            final int start = matcher.start(1);
            att = str.substring(start + 1, str.indexOf('"', start + 1));
        }
        return att;
    }




//    private boolean taxonListContains(String taxon) {
//    	for (Taxon t : taxonList) {
//    		if (t.getID().equals(taxon)) {
//    			return true;
//    		}
//    	}
//		return false;
//	}

    private String getNextDataBlock(String str, BufferedReader fin) throws IOException {
        while (str.indexOf(';') < 0) {
            str += nextLine(fin);
        }
        str = str.replace(";", " ");

        if (str.toLowerCase().matches(".*matrix.*")) {
            // will only get here when there
            throw new IllegalArgumentException("Malformed nexus file: perhaps a semi-colon is missing before 'matrix'");
        }
        return str;
    }


    /**
     * parse calibrations block and create TraitSet *
     */
//    public TraitSet parseCalibrationsBlock(final BufferedReader fin) throws IOException {
//        final TraitSet traitSet = new TraitSet();
//        traitSet.setID("traitsetDate");
//        traitSet.traitNameInput.setValue("date", traitSet);
//        String str;
//        do {
//            str = nextLine(fin);
//            if (str.toLowerCase().contains("options")) {
//                String scale = getAttValue("scale", str);
//                if (scale.endsWith("s")) {
//                    scale = scale.substring(0, scale.length() - 1);
//                }
//                traitSet.unitsInput.setValue(scale, traitSet);
//            }
//        } while (str.toLowerCase().contains("tipcalibration"));
//
//        String text = "";
//        while (true) {
//            str = nextLine(fin);
//            if (str.contains(";")) {
//                break;
//            }
//            text += str;
//        }
//        final String[] strs = text.split(",");
//        text = "";
//        for (final String str2 : strs) {
//            final String[] parts = str2.split(":");
//            final String date = parts[0].replaceAll(".*=\\s*", "");
//            final String[] taxa = parts[1].split("\\s+");
//            for (final String taxon : taxa) {
//                if (!taxon.matches("^\\s*$")) {
//                    text += taxon + "=" + date + ",\n";
//                }
//            }
//        }
//        text = text.substring(0, text.length() - 2);
//        traitSet.traitsInput.setValue(text, traitSet);
//        final TaxonSet taxa = new TaxonSet();
//        taxa.initByName("alignment", alignment);
//        traitSet.taxaInput.setValue(taxa, traitSet);
//
//        traitSet.initAndValidate();
//        return traitSet;
//    } // parseCalibrations


    /**
     * parse assumptions block
     * begin assumptions;
     * charset firsthalf = 1-449;
     * charset secondhalf = 450-898;
     * charset third = 1-457\3 662-896\3;
     * end;
     *
     * begin assumptions;
     * wtset MySoapWeights (VECTOR) = 13 13 13 50 50 88 8
     * end;
     *
     */
//    public void parseAssumptionsBlock(final BufferedReader fin) throws IOException {
//        String str;
//        do {
//            str = nextLine(fin);
//            if (str.toLowerCase().matches("\\s*charset\\s.*")) {
//                // remove text in brackets (as TreeBase files are wont to contain)
//                str = str.replaceAll("\\(.*\\)", "");
//                // clean up spaces
//                str = str.replaceAll("=", " = ");
//                str = str.replaceAll("^\\s+", "");
//                str = str.replaceAll("\\s*-\\s*", "-");
//                str = str.replaceAll("\\s*\\\\\\s*", "\\\\");
//                str = str.replaceAll("\\s*;", "");
//                // replace "," to " " as BEAST 1 uses ,
//                str = str.replaceAll(",\\s+", " ");
//                // use white space as delimiter
//                final String[] strs = str.trim().split("\\s+");
//                final String id = strs[1];
//                String rangeString = "";
//                for (int i = 3; i < strs.length; i++) {
//                    rangeString += strs[i] + " ";
//                }
//                rangeString = rangeString.trim().replace(' ', ',');
//                final FilteredAlignment alignment = new FilteredAlignment();
//                alignment.setID(id);
//                alignment.alignmentInput.setValue(this.alignment, alignment);
//                alignment.filterInput.setValue(rangeString, alignment);
//                alignment.initAndValidate();
//                filteredAlignments.add(alignment);
//            } else if (str.toLowerCase().matches("\\s*wtset\\s.*")) {
//                String [] strs = str.split("=");
//                if (strs.length > 1) {
//                    str = strs[strs.length - 1].trim();
//                    strs = str.split("\\s+");
//                    int [] weights = new int[strs.length];
//                    for (int i = 0; i< strs.length; i++) {
//                        weights[i] = Integer.parseInt(strs[i]);
//                    }
//                    if (alignment != null) {
//                        if (weights.length != alignment.getSiteCount()) {
//                            throw new RuntimeException("Number of weights (" + weights.length+ ") " +
//                                    "does not match number of sites in alignment(" + alignment.getSiteCount()+ ")");
//                        }
//                        StringBuilder weightStr = new StringBuilder();
//                        for (String str2 : strs) {
//                            weightStr.append(str2);
//                            weightStr.append(',');
//                        }
//                        weightStr.delete(weightStr.length() - 1, weightStr.length());
//                        alignment.siteWeightsInput.setValue(weightStr.toString(), alignment);
//                        alignment.initAndValidate();
//                    } else {
//                        Log.warning.println("WTSET was specified before alignment. WTSET is ignored.");
//                    }
//                }
//            } else if (str.toLowerCase().matches("\\s*taxset\\s.*")) {
//                String [] strs = str.split("=");
//                if (strs.length > 1) {
//                    String str0 = strs[0].trim();
//                    String [] strs2 = str0.split("\\s+");
//                    if (strs2.length != 2) {
//                        throw new RuntimeException("expected 'taxset <name> = ...;' but did not get two words before the = sign: " + str);
//                    }
//                    String taxonSetName = strs2[1];
//                    str0 = strs[strs.length - 1].trim();
//                    if (!str0.endsWith(";")) {
//                        Log.warning.println("expected 'taxset <name> = ...;' semi-colon is missing: " + str + "\n"
//                                + "Taxa from following lines may be missing.");
//                    }
//                    str0 = str0.replaceAll(";", "");
//                    String [] taxonNames = str0.split("\\s+");
//                    TaxonSet taxonset = new TaxonSet();
//                    for (String taxon : taxonNames) {
//                        taxonset.taxonsetInput.get().add(new Taxon(taxon.replaceAll("'\"", "")));
//                    }
//                    taxonset.setID(taxonSetName.replaceAll("'\"", ""));
//                    taxonsets.add(taxonset);
//                }
//            } else if (str.toLowerCase().matches("^\\s*calibrate\\s.*")) {
//                // define calibration represented by an MRCAPRior,
//                // taxon sets need to be specified earlier, but can also be a single taxon
//                // e.g.
//                // begin mrbayes;
//                // calibrate germanic = normal(1000,50)
//                // calibrate hittite = normal(3450,100)
//                // calibrate english = fixed(0)
//                // end;
//                String [] strs = str.split("=");
//                if (strs.length > 1) {
//                    String str0 = strs[0].trim();
//                    String [] strs2 = str0.split("\\s+");
//                    if (strs2.length != 2) {
//                        throw new RuntimeException("expected 'calibrate <name> = ...' but did not get two words before the = sign: " + str);
//                    }
//                    // first, get the taxon
//                    String taxonSetName = strs2[1].replaceAll("'\"", "");
//                    TaxonSet taxonset = null;
//                    for (Taxon t : taxonsets) {
//                        if (t.getID().equals(taxonSetName) && t instanceof TaxonSet) {
//                            taxonset = (TaxonSet) t;
//                        }
//                    }
//                    if (taxonset == null) {
//                        // perhaps it is a singleton
//                        for (Taxon t : taxonList) {
//                            if (t.getID().equals(taxonSetName)) {
//                                taxonset = new TaxonSet();
//                                taxonset.setID(t.getID() + ".leaf");
//                                taxonset.taxonsetInput.setValue(t, taxonset);
//                            }
//                        }
//                    }
//                    if (taxonset == null) {
//                        throw new RuntimeException("Could not find taxon/taxonset " + taxonSetName + " in calibration: " + str);
//                    }
//
//                    // next get the calibration
//                    str0 = strs[strs.length - 1].trim();
//                    String [] strs3 = str0.split("[\\(,\\)]");
//
//                    try {
//                        MRCAPrior prior = getMRCAPrior(taxonset, strs3, false);
//
//                        // should set Tree before initialising, but we do not know the tree yet...
//                        if (calibrations == null) {
//                            calibrations = new ArrayList<>();
//                        }
//                        calibrations.add(prior);
//                    } catch (RuntimeException ex) {
//                        throw new RuntimeException(ex.getMessage() + "in calibration: " + str);
//                    }
//                }
//            }
//
//
//        } while (!str.toLowerCase().contains("end;"));
//    }

//    public void parseTreesBlock(final BufferedReader fin) throws IOException {
//        trees = new ArrayList<>();
//        // read to first command within trees block
//        NexusCommand nextCommand = readNextCommand(fin);
//
//        int origin = -1;
//
//        // if first non-empty line is "translate" then parse translate block
//        if (nextCommand.isCommand("translate")) {
//            translationMap = parseTranslateCommand(nextCommand.arguments);
//            origin = getIndexedTranslationMapOrigin(translationMap);
//            if (origin != -1) {
//                taxa = getIndexedTranslationMap(translationMap, origin);
//            }
//        }
//
//        // read trees
//        while (nextCommand != null && !nextCommand.isEndOfBlock()) {
//            if (nextCommand.isCommand("tree")) {
//                String treeString = nextCommand.arguments;
//                final int i = treeString.indexOf('(');
//                if (i > 0) {
//                    treeString = treeString.substring(i);
//                }
//                TreeParser treeParser;
//
//                if (origin != -1) {
//                    treeParser = new TreeParser(taxa, treeString, origin, false);
//                } else {
//                    try {
//                        treeParser = new TreeParser(taxa, treeString, 0, false);
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                        treeParser = new TreeParser(taxa, treeString, 1, false);
//                    }
//                }
//
//                // this needs to go after translation map or listeners have an incomplete tree!
//                for (final NexusParserListener listener : listeners) {
//                    listener.treeParsed(trees.size(), treeParser);
//                }
//
//                // this must come after listener or trees.size() gives the wrong index to treeParsed
//                trees.add(treeParser);
//
//            }
//            nextCommand = readNextCommand(fin);
//        }
//    }

    /**
     * parse sets block
     * BEGIN Sets;
     * TAXSET 'con' = 'con_SL_Gert2' 'con_SL_Tran6' 'con_SL_Tran7' 'con_SL_Gert6';
     * TAXSET 'spa' = 'spa_138a_Cerb' 'spa_JB_Eyre1' 'spa_JB_Eyre2';
     * END; [Sets]
     */
//    public void parseSetsBlock(final BufferedReader fin) throws IOException {
//        String str;
//        do {
//            str = nextLine(fin);
//            if (str.toLowerCase().matches("\\s*taxset\\s.*")) {
//                String [] strs = str.split("=");
//                if (strs.length > 1) {
//                    String str0 = strs[0].trim();
//                    String [] strs2 = str0.split("\\s+");
//                    if (strs2.length != 2) {
//                        throw new RuntimeException("expected 'taxset <name> = ...;' but did not get two words before the = sign: " + str);
//                    }
//                    String taxonSetName = strs2[1];
//                    str0 = strs[strs.length - 1].trim();
//                    if (!str0.endsWith(";")) {
//                        Log.warning.println("expected 'taxset <name> = ...;' semi-colin is missing: " + str + "\n"
//                                + "Taxa from following lines may be missing.");
//                    }
//                    str0 = str0.replaceAll(";", "");
//                    String [] taxonNames = str0.split("\\s+");
//                    TaxonSet taxonset = new TaxonSet();
//                    for (String taxon : taxonNames) {
//                        taxonset.taxonsetInput.get().add(new Taxon(taxon.replaceAll("'\"", "")));
//                    }
//                    taxonset.setID(taxonSetName.replaceAll("'\"", ""));
//                    taxonsets.add(taxonset);
//                }
//            }
//        } while (!str.toLowerCase().contains("end;"));
//    }

    /**
     * get a MRCAPrior object for given taxon set,
     * from a string array which determines the distribution
     * @param taxonset
     * @param strs3 [0] is distribution name,
     *              [1]-[3] for values to determine the distribution
     * @return a MRCAPrior object
     * @throws RuntimeException
     */
//    public MRCAPrior getMRCAPrior(TaxonSet taxonset, String[] strs3) throws RuntimeException {
//    	return getMRCAPrior(taxonset, strs3, false);
//    }
//
//    public MRCAPrior getMRCAPrior(TaxonSet taxonset, String[] strs3, boolean useOriginate) throws RuntimeException {
//        RealParameter[] param = new RealParameter[strs3.length];
//        for (int i = 1; i < strs3.length; i++) {
//            try {
//                param[i] = new RealParameter(strs3[i]);
//                param[i].setID("param." + i);
//            } catch (Exception  e) {
//                // ignore parsing errors
//            }
//        }
//        ParametricDistribution distr  = null;
//        switch (strs3[0]) {
//        case "normal":
//            distr = new Normal();
//            distr.initByName("mean", param[1], "sigma", param[2]);
//            distr.setID("Normal.0");
//            break;
//        case "uniform":
//            distr = new Uniform();
//            distr.initByName("lower", strs3[1], "upper", strs3[2]);
//            distr.setID("Uniform.0");
//            break;
//        case "fixed":
//            // uniform with lower == upper
//            distr = new Normal();
//            distr.initByName("mean", param[1], "sigma", "+Infinity");
//            distr.setID("Normal.0");
//            break;
//        case "offsetlognormal":
//            distr = new LogNormalDistributionModel();
//            distr.initByName("offset", strs3[1], "M", param[2], "S", param[3], "meanInRealSpace", true);
//            distr.setID("LogNormalDistributionModel.0");
//            break;
//        case "lognormal":
//            distr = new LogNormalDistributionModel();
//            distr.initByName("M", param[1], "S", param[2], "meanInRealSpace", true);
//            distr.setID("LogNormalDistributionModel.0");
//            break;
//        case "offsetexponential":
//            distr = new Exponential();
//            distr.initByName("offset", strs3[1], "mean", param[2]);
//            distr.setID("Exponential.0");
//            break;
//        case "gamma":
//            distr = new Gamma();
//            distr.initByName("alpha", param[1], "beta", param[2]);
//            distr.setID("Gamma.0");
//            break;
//        case "offsetgamma":
//            distr = new Gamma();
//            distr.initByName("offset", strs3[1], "alpha", param[2], "beta", param[3]);
//            distr.setID("Gamma.0");
//            break;
//        default:
//            throw new RuntimeException("Unknwon distribution "+ strs3[0]);
//        }
//        MRCAPrior prior = new MRCAPrior();
//        prior.isMonophyleticInput.setValue(true, prior);
//        prior.distInput.setValue(distr, prior);
//        prior.taxonsetInput.setValue(taxonset, prior);
//        prior.useOriginateInput.setValue(useOriginate, prior);
//        prior.setID(taxonset.getID() + (useOriginate ? ".originate." : "") + ".prior");
//        return prior;
//    }


}
