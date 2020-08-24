package lphy.nexus.parser;

import lphy.evolution.alignment.Alignment;
import lphy.nexus.parser.datatype.DataType;
import lphy.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse data block including taxa
 * Modified from BEAST 2
 */
public class DataBlockParser extends NexusBlockParser {

    public static final String NTAX = "ntax";
    public static final String NCHAR = "nchar";
    public static final String DATATYPE = "datatype";


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
                if (nextCommand.getKeyValueArgs().get(NTAX) != null)
                    expectedTaxonCount = Integer.parseInt(nextCommand.getKeyValueArgs().get(NTAX));
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
     * parse data block and create Alignment *
     */
    public Alignment parseDataBlock(final BufferedReader fin) throws IOException {

        String str;
        int ntax = -1;
        int nchar = -1;
        int nrOfState = 4;
        String missing = "?";
        String gap = "-";
        DataType dataType = null;
        // indicates character matches the one in the first sequence
        String matchChar = null;
        do {
            str = nextLine(fin);

            //dimensions ntax=12 nchar=898;
            if (str.toLowerCase().contains("dimensions")) {
                str = getNextDataBlock(str, fin);

                final String character = getAttValue(NCHAR, str);
                if (character == null) {
                    throw new IOException("nchar attribute expected (e.g. 'dimensions nchar=123') expected, not " + str);
                }
                nchar = Integer.parseInt(character);
                final String taxa = getAttValue(NTAX, str);
                if (taxa != null) {
                    ntax = Integer.parseInt(taxa);
                }
            } else if (str.toLowerCase().contains("format")) {
                str = getNextDataBlock(str, fin);

                final String symbols;
                if (getAttValue("symbols", str) == null) {
                    symbols = getAttValue("symbols", str);
                } else {
                    symbols = getAttValue("symbols", str).replaceAll("\\s", "");
                }

                //format datatype=dna interleave=no gap=-;
                String dataTypeName = getAttValue(DATATYPE, str);

                if (dataTypeName == null) {
                    throw new UnsupportedOperationException("datatype = " + dataTypeName);
//                    Log.warning.println("Warning: expected datatype (e.g. something like 'format datatype=dna;') not '" + str + "' Assuming integer dataType");
//                    alignment.dataTypeInput.setValue("integer", alignment);
//                    if (symbols != null && (symbols.equals("01") || symbols.equals("012"))) {
//                        nrOfState = symbols.length();
//                    }
                } else {
                    dataTypeName = dataTypeName.toLowerCase();
                }

                //TODO "standard"
                dataType = DataType.getDataType(dataTypeName);
                nrOfState = dataType.getStateCount();
                System.out.println("Detect dataType " + dataType + ", number of data states = "+ nrOfState);

                
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
//            standardDataType.setInputValue("nrOfStates", Math.max(maxNumberOfStates[0], nrOfState));
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
                } else if ((prevTaxon == null || seqLen == nchar) && end == str.length()) {
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

        if (ntax < 0) ntax = taxa.size(); // no taxlabels
        if (taxa.size() != ntax) { // with taxlabels
            throw new IOException("dimensions block says there are " + ntax +
                    " taxa, but there were " + taxa.size() + " taxa found");
        }

        //****** create alignment here ******//
        Map<String, Integer> idMap = new TreeMap<>();
        // TODO could be wrong to map taxa id
        int id = 0;
        for (Map.Entry<String, StringBuilder> entry : seqMap.entrySet()) {
            idMap.put(entry.getKey(), id++);
        }
//        seqLen = seqMap.get(taxa.get(0)).length(); // ? seqLen != nchar here
//        alignment[idMap.get(taxon)][position] = state;
        final Alignment alignment = new Alignment(ntax, nchar, idMap, nrOfState);
        //****** create alignment above ******//


        //****** parse sequences into int
        HashSet<String> sortedAmbiguities = new HashSet<>();
        for (final String taxon : taxa) {
//            if (!taxonListContains(taxon)) {
//                taxonList.add(new Taxon(taxon));
//            }
            final StringBuilder bsData = seqMap.get(taxon);
            String sequence = solveAmbiguousChar(taxon, bsData, missing, gap, sortedAmbiguities);

            //check the length of the sequence (treat ambiguity sets as single characters)
            if (sequence.length() != nchar) {
                throw new IOException("Expected sequence of length " + nchar + " instead of " +
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

            assert nchar == sequence.length();
            if (dataType == null) throw new IllegalArgumentException("Cannot detect data type !");
            for (int i = 0; i < sequence.length(); i++){
                char c = sequence.charAt(i);
                int state = dataType.getState(c);
                alignment.setState(taxon, i, state);
            }

        }

        return alignment;
    } // parseDataBlock

    private String solveAmbiguousChar(final String taxon, final StringBuilder bsData, final String missing,
                                      final String gap, HashSet<String> sortedAmbiguities) {
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
                        LoggerUtils.log.warning("Ambiguity found in " + taxon +
                                " that is treated as missing value");
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
            throw new IllegalArgumentException("Malformed nexus file: perhaps a semi-colon " +
                    "is missing before 'matrix'");
        }
        return str;
    }




}
