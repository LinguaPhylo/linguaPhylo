package lphy.evolution.io;


import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.ImportHelper;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;
import lphy.evolution.io.TaxaAttr.AgeType;
import lphy.evolution.sequences.SequenceTypeFactory;
import lphy.evolution.traits.CharSetBlock;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension of {@link NexusImporter}, adding
 * 1) multiple partitions, 2) dates.
 * <p>
 * The light version of {@link #importAlignments()} is replaced by
 * {@link #importNexus()} which stores <code>List<Alignment></code>,
 * <code>Map<String, List<CharSetBlock>></code>, and other parsed data.
 * To retrieve the result, the getters have to be used, for example
 * {@link #getAlignments()}, {@link #getCharsetMap()}.
 *
 * @author Walter Xie
 */
public class ExtNexusImporter extends NexusImporter {

    protected List<Alignment> alignments;
    protected Map<String, List<CharSetBlock>> charsetMap;
    protected Map<String, String> dateMap; // TODO replace to ageMap
    protected ChronoUnit chronoUnit = null;
    protected AgeType ageType = null;

    protected final SequenceTypeFactory sequenceTypeFactory = new SequenceTypeFactory();

    public ExtNexusImporter(Reader reader) {
        super(reader);
    }

    /**
     * TODO make it extendable
     * interface NexusBlockImp{ public NexusBlock findNextBlock(); }
     * enum NexusBlock implements NexusBlockImp{ TAXA, ..., DATA; }
     * // or T extends Enum<? extends NexusBlockImp>
     * class NexusImporterDefault<T implements NexusBlockImp> {
     * protected T enumNexusBlock;
     * protected NexusImporterDefault(T block){
     * this.block = block;
     * } }
     * class NexusImporter extends NexusImporterDefault<NexusBlock>{
     * public NexusImporter(NexusBlock block){
     * super(block);
     * } }
     * protected not private ...
     */
    public enum ExtNexusBlock {
        UNKNOWN,
        TAXA,
        CHARACTERS,
        DATA,
        ASSUMPTIONS, // new
        CALIBRATION, // new
        UNALIGNED,
        DISTANCES,
        TREES
    }


    //****** NexusBlock ******//

    public ExtNexusBlock findNextBlockExt() throws IOException {
        findToken("BEGIN", true);
        nextBlockName = helper.readToken(";").toUpperCase();
        return findBlockName(nextBlockName);
    }

    protected ExtNexusBlock findBlockName(String blockName) {
        try {
            nextBlock = ExtNexusBlock.valueOf(blockName);
        } catch (IllegalArgumentException e) {
            // handle unknown blocks. java 1.5 throws an exception in valueOf
            nextBlock = null;
        }

        if (nextBlock == null) {
            nextBlock = ExtNexusBlock.UNKNOWN;
        }

        return nextBlock;
    }


    public void importNexus() throws IOException, ImportException {
        boolean done = false;

        List<Taxon> taxonList = null;
        alignments = new ArrayList<>();

        while (!done) {
            try {

                ExtNexusImporter.ExtNexusBlock block = findNextBlockExt();

                if (block == ExtNexusImporter.ExtNexusBlock.TAXA) {
                    //TODO new datatype
                    taxonList = readTaxaBlock(); // TODO ? parseTaxaBlock()

                } else if (block == ExtNexusImporter.ExtNexusBlock.CHARACTERS) {

                    if (taxonList == null) {
                        throw new NexusImporter.MissingBlockException("TAXA block is missing");
                    }

                    List<Sequence> sequences = readCharactersBlock(taxonList); //TODO ? parseCharactersBlock(taxonList)
                    alignments.add(new BasicAlignment(sequences));

                } else if (block == ExtNexusImporter.ExtNexusBlock.DATA) {

                    // A data block doesn't need a taxon block before it
                    // but if one exists then it will use it.
                    List<Sequence> sequences = readDataBlock(taxonList); //TODO ? parseDataBlock(taxonList)
                    alignments.add(new BasicAlignment(sequences));

                } else if (block == ExtNexusImporter.ExtNexusBlock.ASSUMPTIONS) {

                    readAssumptionsBlock(); // only CHARSET

                } else if (block == ExtNexusImporter.ExtNexusBlock.CALIBRATION) {

                    readCalibrationBlock(); // only TIPCALIBRATION

                } else {
                    //TODO new block
                }

            } catch (EOFException ex) {
                done = true;
            }
        }

        if (alignments.size() == 0)
            throw new NexusImporter.MissingBlockException("DATA or CHARACTERS block is missing");

//        return nexusContent;
//        return importer.importAlignments();
    }

    //****** getters ******//

    public List<Alignment> getAlignments() {
        if (alignments == null) alignments = new ArrayList<>();
        return alignments;
    }


    public Map<String, List<CharSetBlock>> getCharsetMap() {
        if (charsetMap == null) charsetMap = new TreeMap<>();
        return charsetMap;
    }

    public Map<String, String> getDateMap() {
        if (dateMap == null) dateMap = new TreeMap<>();
        return dateMap;
    }

//****** Data Type ******//

    /**
     * Extract data type after "DATATYPE" keyword, and convert into {@link SequenceType}.
     * Override this method if there is new data type.
     *
     * @param token the token returned from {@link ImportHelper#readToken(String)}.
     * @return the corresponding {@link SequenceType}.
     * @throws ImportException.UnparsableDataException
     */
    protected SequenceType getSequenceType(String token) throws ImportException.UnparsableDataException {
        try {
            // new data type?
            return sequenceTypeFactory.getNexusDataType(token);
        } catch (UnsupportedOperationException e) {
            throw new ImportException.UnparsableDataException(e.getMessage());
        }
    }

    //****** CALIBRATION Block : TIPCALIBRATION ******//

    protected boolean isNotEnd(String token) {
        return !token.equalsIgnoreCase("END") && !token.equalsIgnoreCase("ENDBLOCK");
    }

    protected void readCalibrationBlock() throws ImportException, IOException {

        dateMap = new LinkedHashMap<>();

        String token;
        do {
            token = helper.readToken(";");
            if (token.equalsIgnoreCase("OPTIONS")) {
                String token2 = helper.readToken("=");
                if (token2.equalsIgnoreCase("SCALE")) {
                    String scale = helper.readToken(";");
                    if (scale.toLowerCase().endsWith("s"))
                        scale = scale.substring(0, scale.length() - 1);

                    switch (scale) {
                        case "year":
                            chronoUnit = ChronoUnit.YEARS; break;
//                        case "month":
//                            chronoUnit = ChronoUnit.MONTHS; break;
//                        case "day":
//                            chronoUnit = ChronoUnit.DAYS; break;
                        default:
                            throw new UnsupportedOperationException("Unsupported scale = " + scale);
                    }
                }

            } else if (token.equalsIgnoreCase("TIPCALIBRATION")) {
                if (chronoUnit == null)
                    throw new ImportException("Cannot find SCALE unit, e.g. year");

                // 94 = 1994:D4ElSal94, // 86 = 1986:D4PRico86,
                do {
                    String date = null;
                    String taxonNm = null;
                    int lastDelimiter;
                    do {
                        String token2 = helper.readToken(":=,;");

                        if (helper.getLastDelimiter() != '=') { // ignore date's labels, e.g. 94 =
                            if (helper.getLastDelimiter() == ':')
                                date = token2;
                            else
                                taxonNm = token2;
                        }

                        lastDelimiter = helper.getLastDelimiter();
                        if (date != null && taxonNm != null) {
                            // put inside loop for same date, 1984:D4Mexico84 D4Philip84 D4Thai84,
                            dateMap.put(taxonNm, date);
                        } else if (lastDelimiter == ',' || lastDelimiter == ';') throw new ImportException();

                    } while (lastDelimiter != ',' && lastDelimiter != ';');
                    // next date mapping
                } while (helper.getLastDelimiter() != ';');

                if (dateMap.size() < 1)
                    throw new ImportException("Cannot parse TIPCALIBRATION !");
                if (dateMap.size() != taxonCount)
                    System.err.println("Warning: " + dateMap.size() +
                            " tips have dates, but taxon count = " + taxonCount);

            } // end if else

        } while (isNotEnd(token));

        //validation ?
    }

    /**
     * TODO parse date "uuuu-MM-dd"
     * @param type  forward backward age
     * @return
     * @throws DateTimeParseException
     */
    public Map<String, Double> getAgeMap(final String type) {
        if (dateMap==null || dateMap.size() < 1) return null;

        // LinkedHashMap supposes to maintain the order in keySet() and values()
        String[] taxaNames = dateMap.keySet().toArray(String[]::new);
        String[] datesStr = dateMap.values().toArray(String[]::new);

        TaxaAttr taxaAttr = new TaxaAttr(taxaNames, datesStr, type.toLowerCase());
        ageType = taxaAttr.getAgeType();
        return taxaAttr.getTaxaAgeMap();
    }


    //****** ASSUMPTIONS Block : charset ******//

    /**
     * begin assumptions;
     * charset coding = 2-457 660-896;
     * charset noncoding = 1 458-659 897-898;
     * end;
     */
    protected void readAssumptionsBlock() throws ImportException, IOException {

        charsetMap = new TreeMap<>();
        String token;
        do {
            token = helper.readToken(";");

            if (token.equalsIgnoreCase("CHARSET")) {
                String charset = helper.readToken("=");
                List<CharSetBlock> charSetBlocks = new ArrayList<>();
                do {
                    String token2 = helper.readToken(";");
                    String[] parts = token2.split("-");

                    int from, to, every = 1;
                    try {
                        if (parts.length == 2) {
                            // from site
                            from = Integer.parseInt(parts[0].trim());

                            // codons : 629\3
                            if (parts[1].contains("/"))
                                throw new ImportException("Invalid delimiter for codon positions ! " + parts[1]);
                            String[] toParts = parts[1].split("\\\\");
                            // to site
                            if (toParts[0].trim().equals("."))
                                to = -1; // if (to <= 0) toSite = nchar;
                            else
                                to = Integer.parseInt(toParts[0].trim());
                            // codon position
                            if (toParts.length > 1)
                                every = Integer.parseInt(toParts[1].trim());
                            else
                                every = 1;

                        } else if (parts.length == 1) {
                            // only 1 site
                            from = Integer.parseInt(parts[0].trim());
                            to = from;
                        } else
                            throw new ImportException("Charset " + charset + " = " + token2 + " cannot be parsed");
                    } catch (NumberFormatException nfe) {
                        throw new ImportException("Charset " + charset + " = " + token2 + " cannot be parsed");
                    }

                    charSetBlocks.add(new CharSetBlock(from, to, every));

                } while (helper.getLastDelimiter() != ';');

                charsetMap.put(charset, charSetBlocks);
            }
        } while (isNotEnd(token));
        //validation ?
    }


    //****** Can be removed if pull request is accepted ******//

    private void findToken(String query, boolean ignoreCase) throws IOException {
        String token;
        boolean found = false;

        do {
            token = helper.readToken();

            if ((ignoreCase && token.equalsIgnoreCase(query)) || token.equals(query)) {
                found = true;
            }
        } while (!found);
    }

    static void parseAndClearMetaComments(Attributable item, ImportHelper importHelper) throws ImportException.BadFormatException {
        for (String meta : importHelper.getMetaComments()) {
            // A meta-comment which should be in the form:
            // \[&label[=value][,label[=value]>[,/..]]\]
            parseMetaCommentPairs(meta, item);

        }
        importHelper.clearLastMetaComment();
    }

    static void parseMetaCommentPairs(String meta, Attributable item) throws ImportException.BadFormatException {
        // This regex should match key=value pairs, separated by commas
        // This can match the following types of meta comment pairs:
        // value=number, value="string", value={item1, item2, item3}
        // (label must be quoted if it contains spaces (i.e. "my label"=label)

//        Pattern pattern = Pattern.compile("(\"[^\"]*\"+|[^,=\\s]+)\\s*(=\\s*(\\{[^=}]*\\}|\"[^\"]*\"+|[^,]+))?");
        Pattern pattern = Pattern.compile("(\"[^\"]*\"+|[^,=\\s]+)\\s*(=\\s*(\\{(\\{[^\\}]+\\},?)+\\}|\\{[^\\}]+\\}|\"[^\"]*\"+|[^,]+))?");
        Matcher matcher = pattern.matcher(meta);

        while (matcher.find()) {
            String label = matcher.group(1);
            if (label.charAt(0) == '\"') {
                label = label.substring(1, label.length() - 1);
            }
            if (label == null || label.trim().length() == 0) {
                throw new ImportException.BadFormatException("Badly formatted attribute: '" + matcher.group() + "'");
            }
            final String value = matcher.group(2);
            if (value != null && value.trim().length() > 0) {
                // there is a specified value so try to parse it
                item.setAttribute(label, parseValue(value.substring(1)));
            } else {
                item.setAttribute(label, Boolean.TRUE);
            }
        }
    }

    static Object parseValue(String value) {

        value = value.trim();

        if (value.startsWith("{")) {
            value = value.substring(1, value.length() - 1);

            String[] elements;

            if (value.startsWith("{")) {
                // the value is a list of a list so recursively parse the elements
                // and return an array

                // need to match },{ but leave the brackets in place
                value = value.replaceAll("\\},\\{", "}@,@{");
                elements = value.split("@,@");

            } else {
                // the value is a list so recursively parse the elements
                // and return an array
                elements = value.split(",");
            }
            Object[] values = new Object[elements.length];
            for (int i = 0; i < elements.length; i++) {
                values[i] = parseValue(elements[i]);
            }
            return values;
        }

        if (value.startsWith("#")) {
            // I am not sure whether this is a good idea but
            // I am going to assume that a # denotes an RGB colour
            String colourValue = value.substring(1);
            if (colourValue.startsWith("-")) {
                // old style decimal numbers
                try {
                    return Color.decode(colourValue);
                } catch (NumberFormatException nfe1) {
                    // not a colour
                }
            } else {
                return Color.decode("0x" + colourValue);
            }
        }

        // A string qouted by the nexus exporter and such
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.subSequence(1, value.length() - 1);
        }

        if (value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("FALSE")) {
            return Boolean.valueOf(value);
        }

        // Attempt to format the value as an integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe1) {
            // not an integer
        }

        // Attempt to format the value as a double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe2) {
            // not a double
        }

        // return the trimmed string
        return value;
    }


    private List<Taxon> readTaxaBlock() throws ImportException, IOException {
        taxonCount = 0;

        readDataBlockHeader("TAXLABELS", NexusBlock.TAXA);

        if (taxonCount == 0) {
            throw new ImportException.MissingFieldException("NTAXA");
        }

        List<Taxon> taxa = new ArrayList<Taxon>();

        do {
            String name = helper.readToken(";");
            if (name.equals("")) {
                throw new ImportException.UnknownTaxonException("Expected nonempty taxon name, got empty string");
            }
            Taxon taxon = Taxon.getTaxon(name);
            taxa.add(taxon);

            parseAndClearMetaComments(taxon, helper);
        } while (helper.getLastDelimiter() != ';');

        if (taxa.size() != taxonCount) {
            throw new ImportException.BadFormatException("Number of taxa doesn't match NTAXA field");
        }

        findEndBlock();

        return taxa;
    }

    private List<Sequence> readCharactersBlock(List<Taxon> taxonList) throws ImportException, IOException {

        siteCount = 0;
        sequenceType = null;

        readDataBlockHeader("MATRIX", NexusBlock.CHARACTERS);

        List<Sequence> sequences = readSequenceData(taxonList);

        findEndBlock();

        return sequences;
    }

    /**
     * Reads a 'DATA' block.
     */
    private List<Sequence> readDataBlock(List<Taxon> taxonList) throws ImportException, IOException {

        taxonCount = 0;
        siteCount = 0;
        sequenceType = null;

        readDataBlockHeader("MATRIX", NexusBlock.DATA);

        List<Sequence> sequences = readSequenceData(taxonList);

        findEndBlock();

        return sequences;
    }

    private List<Sequence> readSequenceData(List<Taxon> taxonList) throws ImportException, IOException {
        boolean sequencherStyle = false;
        String firstSequence = null;
        List<Sequence> sequences = new ArrayList<Sequence>();

        if (isInterleaved) {
            List<StringBuilder> sequencesData = new ArrayList<StringBuilder>(taxonCount);
            List<Taxon> taxons = new ArrayList<Taxon>();
            List<Taxon> taxList = (taxonList != null) ? taxonList : taxons;

            int[] charsRead = new int[taxonCount];
            for (int i = 0; i < taxonCount; i++) {
                sequencesData.add(new StringBuilder());
                charsRead[i] = 0;
            }
            //throw new ImportException.UnparsableDataException("At present, interleaved data is not parsable");
            boolean firstLoop = true;

            int readCount = 0;
            while (readCount < siteCount * taxonCount) {

                for (int i = 0; i < taxonCount; i++) {

                    String token = helper.readToken();

                    int sequenceIndex;
                    Taxon taxon = Taxon.getTaxon(token);
                    if (firstLoop) {
                        if (taxonList != null) {
                            sequenceIndex = taxonList.indexOf(taxon);
                        } else {
                            sequenceIndex = taxons.size();
                            taxons.add(taxon);
                        }
                    } else {
                        sequenceIndex = taxList.indexOf(taxon);
                    }

                    if (sequenceIndex < 0) {
                        // taxon not found in taxon list...
                        // ...perhaps it is a numerical taxon reference?
                        throw new ImportException.UnknownTaxonException("Unexpected taxon:" + token
                                + " (expecting " + taxList.get(i).getName() + ")");
                    }

                    StringBuffer buffer = new StringBuffer();

                    helper.readSequenceLine(buffer, sequenceType, ";", gapCharacters, missingCharacters,
                            matchCharacters, firstSequence);

                    String seqString = buffer.toString();

                    // We now check if this file is in Sequencher* style NEXUS, this style has the taxon and site counts
                    // before the sequence data.
                    try {
                        if (firstLoop && Integer.parseInt(taxon.toString()) == taxonCount &&
                                Integer.parseInt(seqString) == siteCount) {
                            i--;
                            taxons.remove(taxon);
                            sequencherStyle = true;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // Do nothing, this just means that this is the NEXUS format we usually expect rather than sequencher
                    }

                    readCount += seqString.length();
                    charsRead[sequenceIndex] += seqString.length();

                    sequencesData.get(sequenceIndex).append(seqString);
                    if (i == 0) {
                        firstSequence = seqString;
                    }

                    if (helper.getLastDelimiter() == ';') {
                        if (i < taxonCount - 1) {
                            throw new ImportException.TooFewTaxaException();
                        }
                        for (int k = 0; k < taxonCount; k++) {
                            if (charsRead[k] != siteCount) {
                                throw new ImportException.ShortSequenceException(taxList.get(k).getName()
                                        + " has length " + charsRead[k] + ", expecting " + siteCount);
                            }
                        }
                    }
                }

                firstLoop = false;
            }

            // Sequencher style apparently doesnt use a ';' after the sequence data.
            if (!sequencherStyle && helper.getLastDelimiter() != ';') {
                throw new ImportException.BadFormatException("Expecting ';' after sequences data");
            }

            for (int k = 0; k < taxonCount; k++) {
                Sequence sequence = new BasicSequence(sequenceType, taxList.get(k), sequencesData.get(k));
                sequences.add(sequence);
            }

        } else {

            for (int i = 0; i < taxonCount; i++) {
                String token = helper.readToken();

                Taxon taxon = Taxon.getTaxon(token);

                if (taxonList != null && !taxonList.contains(taxon)) {
                    // taxon not found in taxon list...
                    // ...perhaps it is a numerical taxon reference?
                    StringBuilder message = new StringBuilder("Expected: ").append(token).append("\nActual taxa:\n");
                    for (Taxon taxon1 : taxonList) {
                        message.append(taxon1).append("\n");
                    }
                    throw new ImportException.UnknownTaxonException(message.toString());
                }

                StringBuilder buffer = new StringBuilder();
                helper.readSequence(buffer, sequenceType, ";", siteCount, gapCharacters,
                        missingCharacters, matchCharacters, firstSequence, true);
                String seqString = buffer.toString();

                if (seqString.length() != siteCount) {
                    throw new ImportException.ShortSequenceException(taxon.getName()
                            + " has length " + seqString.length() + ", expecting " + siteCount);
                }

                if (i == 0) {
                    firstSequence = seqString;
                }

                if (helper.getLastDelimiter() == ';' && i < taxonCount - 1) {
                    throw new ImportException.TooFewTaxaException();
                }

                Sequence sequence = new BasicSequence(sequenceType, taxon, seqString);
                sequences.add(sequence);
            }

            if (helper.getLastDelimiter() != ';') {
                throw new ImportException.BadFormatException("Expecting ';' after sequences data");
            }

        }

        return sequences;
    }

//TODO new datatypes    readDataBlockHeader

    private void readDataBlockHeader(String tokenToLookFor, NexusBlock block) throws ImportException, IOException {

        boolean foundDimensions = false, foundTitle = false, foundFormat = false;
        String token;

        do {
            token = helper.readToken();

            if (token.equalsIgnoreCase("TITLE")) {
                if (foundTitle) {
                    throw new ImportException.DuplicateFieldException("TITLE");
                }

                foundTitle = true;
            } else if (token.equalsIgnoreCase("DIMENSIONS")) {

                if (foundDimensions) {
                    throw new ImportException.DuplicateFieldException("DIMENSIONS");
                }

                boolean nchar = (block == NexusBlock.TAXA);
                boolean ntax = (block == NexusBlock.CHARACTERS);

                do {
                    String token2 = helper.readToken("=;");

                    if (helper.getLastDelimiter() != '=') {
                        throw new ImportException.BadFormatException("Unknown subcommand, '" + token2 + "', or missing '=' in DIMENSIONS command");
                    }

                    if (token2.equalsIgnoreCase("NTAX")) {

                        if (block == NexusBlock.CHARACTERS) {
                            throw new ImportException.BadFormatException("NTAX subcommand in CHARACTERS block");
                        }

                        taxonCount = helper.readInteger(";");
                        ntax = true;

                    } else if (token2.equalsIgnoreCase("NCHAR")) {

                        if (block == NexusBlock.TAXA) {
                            throw new ImportException.BadFormatException("NCHAR subcommand in TAXA block");
                        }

                        siteCount = helper.readInteger(";");
                        nchar = true;

                    } else {
                        throw new ImportException.BadFormatException("Unknown subcommand, '" + token2 + "', in DIMENSIONS command");
                    }

                } while (helper.getLastDelimiter() != ';');

                if (!ntax) {
                    throw new ImportException.BadFormatException("NTAX subcommand missing from DIMENSIONS command");
                }
                if (!nchar) {
                    throw new ImportException.BadFormatException("NCHAR subcommand missing from DIMENSIONS command");
                }
                foundDimensions = true;

            } else if (token.equalsIgnoreCase("FORMAT")) {

                if (foundFormat) {
                    throw new ImportException.DuplicateFieldException("FORMAT");
                }

                sequenceType = null;

                do {
                    String token2 = helper.readToken("=;");

                    if (token2.equalsIgnoreCase("GAP")) {

                        if (helper.getLastDelimiter() != '=') {
                            throw new ImportException.BadFormatException("Expecting '=' after GAP subcommand in FORMAT command");
                        }

                        gapCharacters = helper.readToken(";");

                    } else if (token2.equalsIgnoreCase("MISSING")) {

                        if (helper.getLastDelimiter() != '=') {
                            throw new ImportException.BadFormatException("Expecting '=' after MISSING subcommand in FORMAT command");
                        }

                        missingCharacters = helper.readToken(";");

                    } else if (token2.equalsIgnoreCase("MATCHCHAR")) {

                        if (helper.getLastDelimiter() != '=') {
                            throw new ImportException.BadFormatException("Expecting '=' after MATCHCHAR subcommand in FORMAT command");
                        }

                        matchCharacters = helper.readToken(";");

                    } else if (token2.equalsIgnoreCase("DATATYPE")) {

                        if (helper.getLastDelimiter() != '=') {
                            throw new ImportException.BadFormatException("Expecting '=' after DATATYPE subcommand in FORMAT command");
                        }

                        String token3 = helper.readToken(";");
                        // replace getSequenceType if there is new data type
                        sequenceType = getSequenceType(token3);

                    } else if (token2.equalsIgnoreCase("INTERLEAVE")) {
                        isInterleaved = true;
                    }

                } while (helper.getLastDelimiter() != ';');

                foundFormat = true;
            }
        } while (!token.equalsIgnoreCase(tokenToLookFor));

        if (!foundDimensions) {
            throw new ImportException.MissingFieldException("DIMENSIONS");
        }
        if (block != NexusBlock.TAXA && sequenceType == null) {
            throw new ImportException.MissingFieldException("DATATYPE. Only Nucleotide or Protein sequences are supported.");
        }
    }

    // they are private in NexusImporter
    protected ExtNexusBlock nextBlock = null;
    protected String nextBlockName = null;

    protected int taxonCount = 0, siteCount = 0;
    protected SequenceType sequenceType = null;
    protected String gapCharacters = "-";
    protected String matchCharacters = ".";
    protected String missingCharacters = "?";
    protected boolean isInterleaved = false;
}
