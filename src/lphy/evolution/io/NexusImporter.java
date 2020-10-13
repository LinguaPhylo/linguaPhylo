package lphy.evolution.io;


import jebl.evolution.io.ImportException;
import jebl.evolution.io.ImportHelper;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.sequences.Continuous;
import lphy.evolution.sequences.DataType;
import lphy.evolution.sequences.SequenceTypeFactory;
import lphy.evolution.traits.CharSetBlock;
import lphy.utils.LoggerUtils;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modified from {@link jebl.evolution.io.NexusImporter}.
 * Try to use Lphy objects as many as possible.
 *
 * @author Walter Xie
 */
public class NexusImporter {

    protected final ImportHelper helper;
    protected final SequenceTypeFactory sequenceTypeFactory = new SequenceTypeFactory();

    protected NexusData nexusData;

    protected NexusBlock nextBlock = null;
    protected String nextBlockName = null;

    protected int taxonCount = 0, siteCount = 0;
    protected SequenceType sequenceType = null;
    protected String gapCharacters = "-";
    protected String matchCharacters = ".";
    protected String missingCharacters = "?";
    protected boolean isInterleaved = false;

    //TODO
    protected ContinuousCharacterData continuousCharacterData;

//    public ExtNexusImporter(Reader reader) {
//        super(reader);
//    }

    public NexusImporter(String fileName) {

        Reader reader = getReader(fileName);

        // store raw data
        nexusData = new NexusData(fileName);

        helper = new ImportHelper(reader);
        helper.setExpectedInputLength(0);
        // ! defines a comment to be written out to a log file
        // & defines a meta comment
        helper.setCommentDelimiters('[', ']', '\0', '!', '&');

    }

//    protected final int READ_AHEAD_LIMIT = 50000;
    protected Reader getReader(String fileName) {
        Reader reader = null;
        try {
            if (!(fileName.endsWith("nex") || fileName.endsWith("nexus") ||
                    fileName.endsWith("nxs")))
                throw new IOException("Nexus file name's suffix is invalid ! " + fileName);

            final Path nexFile = Paths.get(fileName);

            if (!nexFile.toFile().exists() || nexFile.toFile().isDirectory())
                throw new IOException("Cannot find Nexus file ! " + nexFile);

            reader = Files.newBufferedReader(nexFile); // StandardCharsets.UTF_8
//            reader.mark(READ_AHEAD_LIMIT); // to reset reader back to READ_AHEAD_LIMIT
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    //****** main ******//
    public static void main(final String[] args) {
        try {
            String fileName = args[0];
            System.out.println("Loading " + fileName);
            final NexusImporter importer = new NexusImporter(fileName);

//            List<Alignment> alignmentList = parser.importAlignments();
//                alignmentList.forEach(System.out::println);

            if (fileName.equals("Dengue4.nex")) {
                SimpleAlignment lphyAlg =
                        (SimpleAlignment) importer.getLPhyAlignment(true, "forward", null);

                System.out.println(lphyAlg.toJSON());

            } else if (fileName.equals("primate.nex")) {
                lphy.evolution.alignment.CharSetAlignment lphyAlg =
                        (CharSetAlignment) importer.getLPhyAlignment(false, null, null);
                System.out.println(lphyAlg.toJSON());
//            lphy.evolution.alignment.Alignment[] twoAlg = lphyAlg.getPartAlignments(new String[]{"noncoding", "coding"});
                System.out.println(lphyAlg.toJSON(new String[]{"noncoding", "coding"}));

            } else if (fileName.equals("haemulidae_trophic_traits.nex")) {

                importer.importNexus();
                System.out.println(importer.continuousCharacterData.toJSON());


            } else { // for testing or dev

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main

    //****** import ******//

    public NexusData importNexus() throws IOException, ImportException {
        boolean done = false;

        List<Taxon> taxonList = null;

        while (!done) {
            try {

                NexusBlock block = findNextBlock();

                if (block == NexusBlock.TAXA) {

                    taxonList = readTaxaBlock();

                } else if (block == NexusBlock.CHARACTERS) {

                    if (taxonList == null)
                        throw new jebl.evolution.io.NexusImporter.MissingBlockException("TAXA block is missing");

                    List<Sequence> sequences = readCharactersBlock(taxonList);
                    setSimpleAlignment(sequences);

                } else if (block == NexusBlock.DATA) {

                    // A data block doesn't need a taxon block before it
                    // but if one exists then it will use it.
                    // this reads continuous data into lphy ContinuousCharacterData, not alignments
                    // the rest data type will add alignments
                    readDataBlock(taxonList);

                } else if (block == NexusBlock.ASSUMPTIONS) {

                    readAssumptionsBlock(); // only CHARSET

                } else if (block == NexusBlock.CALIBRATION) {

                    readCalibrationBlock(); // only TIPCALIBRATION

                } else {
                    //TODO new block
                }

            } catch (EOFException ex) {
                done = true;
            }
        }

        if (DataType.isSame(sequenceType, Continuous.getInstance())) {
            if (continuousCharacterData == null)
                throw new jebl.evolution.io.NexusImporter.MissingBlockException("Fail to load continuous data in MATRIX");
        } else if (nexusData.getData() == null)
            throw new jebl.evolution.io.NexusImporter.MissingBlockException("DATA or CHARACTERS block is missing");

        return nexusData;
    }

    // use jebl State to convert char into int
    // create lphy SimpleAlignment from jebl Sequence
    private void setSimpleAlignment(List<Sequence> sequences) {
        if (sequenceType == null)
            throw new IllegalArgumentException("Fail to find data type before parsing sequences !");
        if (siteCount < 1)
            throw new IllegalArgumentException("NCHAR < 1 ! " + siteCount);

        final int seqSize = sequences.size();
        lphy.evolution.Taxon[] taxons = new lphy.evolution.Taxon[seqSize];
        // init Taxon[]
        for (int t = 0; t < seqSize; t++) {
            Sequence sequence = sequences.get(t);
            Taxon jeblTaxon = sequence.getTaxon();
            if (jeblTaxon == null)
                throw new IllegalArgumentException("Cannot find taxon in sequence ! " + t);
            // TODO getAttributeMap()
            taxons[t] = new lphy.evolution.Taxon(jeblTaxon.getName());
        }

        Alignment alignment = new SimpleAlignment(taxons, siteCount, sequenceType);
        // fill in sequences for single partition
        for (int t = 0; t < seqSize; t++) {
            Sequence sequence = sequences.get(t);
            for (int s = 0; s < sequence.getLength(); s++) {
                //*** convert char into int ***//
                State state = sequence.getState(s);
                int stateNum = state.getIndex();
                // the taxon index in List should be same to Taxon[] taxonArray in Alignment
                alignment.setState(t, s, stateNum);
            }
        }

        nexusData.setData(alignment);
    }


    /**
     * Parse Nexus to LPHY {@link SimpleAlignment}
     *
     * @param ignoreCharset If true, ignore charset in Nexus,
     *                      only return single {@link SimpleAlignment}.
     *                      If false, return {@link CharSetAlignment} when Nexus has charsets.
     * @param ageDirectionStr  either forward or backward,
     *                         if null and nex has TIPCALIBRATION block, then assume forward.
     * @param dateRegxStr  Java regular expression to extract dates from taxa names.
     *                     if null, check TIPCALIBRATION block,
     *                     if not null, then ignore TIPCALIBRATION block.
     * @return LPHY {@link SimpleAlignment} or {@link CharSetAlignment}.
     */
    public lphy.evolution.alignment.Alignment getLPhyAlignment(boolean ignoreCharset,
                                                               String ageDirectionStr, String dateRegxStr) {
        NexusData nexusData = null;
        try {
            nexusData = importNexus();
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }
        if (nexusData == null)
            throw new IllegalArgumentException("Fail to parse file ! ");

        lphy.evolution.alignment.SimpleAlignment lphyAlg = nexusData.getSimpleAlignment();
        System.out.println("Create " + lphyAlg.getSequenceType() + " alignment, ntax = " +
                lphyAlg.ntaxa() + ", nchar = " + lphyAlg.nchar());

        //*** ages ***//

        if (dateRegxStr != null) { // ages from taxon names, so ignore TIPCALIBRATION in Nexus
            // extract dates from names
            nexusData.setAgeMapFromTaxa(lphyAlg, dateRegxStr);
        }
        if (nexusData.hasAges()) {
            // ageStringMap is filled in from either TIPCALIBRATION or taxon names
            nexusData.assignAges(lphyAlg, ageDirectionStr);  // forward backward
        }

        //*** charset ***//
        if (nexusData.hasCharsets()) {
            final Map<String, List<CharSetBlock>> charsetMap = nexusData.getCharsetMap();
//            System.out.println( Arrays.toString(charsetMap.entrySet().toArray()) );
//            CharSetAlignment charSetAlignment = new CharSetAlignment(charsetMap, partNames, lphyAlg);
//            System.out.println(charSetAlignment);
            if (ignoreCharset)
                System.out.println("Ignore charsets in the nexus file, charsetMap = " + charsetMap);
            else
                return new CharSetAlignment(charsetMap, lphyAlg); // this imports all charsets
        }
        return lphyAlg; // sing partition
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

    protected void readCalibrationBlock() throws ImportException, IOException {

        String token;
        do {
            token = helper.readToken(";");
            if (token.equalsIgnoreCase("OPTIONS")) {
                String token2 = helper.readToken("=");
                if (token2.equalsIgnoreCase("SCALE")) {
                    String scale = helper.readToken(";");
                    if (scale.toLowerCase().endsWith("s"))
                        scale = scale.substring(0, scale.length() - 1);

                    ChronoUnit chronoUnit;
                    switch (scale) {
                        case "year":
                            chronoUnit = ChronoUnit.YEARS;
                            break;
//                        case "month":
//                            chronoUnit = ChronoUnit.MONTHS; break;
//                        case "day":
//                            chronoUnit = ChronoUnit.DAYS; break;
                        default:
                            throw new UnsupportedOperationException("Unsupported scale = " + scale);
                    }

                    nexusData.setChronoUnit(chronoUnit);
                }

            } else if (token.equalsIgnoreCase("TIPCALIBRATION")) {

                if (nexusData.getChronoUnit() == null) // TODO is it necessary?
                    throw new ImportException("Cannot find SCALE unit, e.g. year");

                // 94 = 1994:D4ElSal94, // 86 = 1986:D4PRico86,
                Map<String, String> ageMap = new LinkedHashMap<>();
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
                            ageMap.put(taxonNm, date);
                        } else if (lastDelimiter == ',' || lastDelimiter == ';') throw new ImportException();

                    } while (lastDelimiter != ',' && lastDelimiter != ';');
                    // next date mapping
                } while (helper.getLastDelimiter() != ';');

                if (ageMap.size() < 1)
                    throw new ImportException("Cannot parse TIPCALIBRATION !");
                if (ageMap.size() != taxonCount)
                    System.err.println("Warning: " + ageMap.size() +
                            " tips have dates, but taxon count = " + taxonCount);

                // store into NexusData
                nexusData.setAgeStringMap(ageMap);

            } // end if else

        } while (isNotEnd(token));

        //validation ?
    }


    //****** ASSUMPTIONS Block : charset ******//

    /**
     * begin assumptions;
     * charset coding = 2-457 660-896;
     * charset noncoding = 1 458-659 897-898;
     * end;
     */
    protected void readAssumptionsBlock() throws ImportException, IOException {

        Map<String, List<CharSetBlock>> charsetMap = new TreeMap<>();
        String token;
        do {
            token = helper.readToken(";");

            if (token.equalsIgnoreCase("CHARSET")) {
                String charset = helper.readToken("=");
                List<CharSetBlock> charSetBlocks = new ArrayList<>();
                do {
                    String oneBlock = helper.readToken(";");
                    try {
                        CharSetBlock charSetBlock = parseCharSet(oneBlock);
                        charSetBlocks.add(charSetBlock);
                    } catch (IllegalArgumentException e) {
                        throw new ImportException("Charset " + charset + e.getMessage());
                    }
                } while (helper.getLastDelimiter() != ';');

                charsetMap.put(charset, charSetBlocks);
            }
        } while (isNotEnd(token));
        //validation ?

        // store into NexusData
        nexusData.setCharsetMap(charsetMap);
    }

    //****** CharSet Utils ******//

    /**
     * @see #parseCharSet(String)
     * @param charset  such as "2-457\3 660-896\3".
     * @return
     */
    public static List<CharSetBlock> getCharSetBlocks(String charset) {
        String[] blocks = charset.split("\\s+");
        List<CharSetBlock> charSetBlocks = new ArrayList<>();
        for (String oneBlock : blocks) {
            CharSetBlock charSetBlock = parseCharSet(oneBlock);
            charSetBlocks.add(charSetBlock);
        }
        return charSetBlocks;
    }

    /**
     * @param charSet1Block The string must only contain one block.
     *                      The blocks could be an array, <code>charset="[3-629\3, 4-629\3, 5-629\3]"</code>.
     *                      The blocks could also be separated by spaces, for example,
     *                      "2-457\3 660-896\3" is considered as 2 charset blocks.
     *                      Use <code>split("\\s+")</code> to spilt charset blocks
     *                      before call this method.
     * @return only 1 {@link CharSetBlock} parsed from string
     * @throws IllegalArgumentException
     */
    public static CharSetBlock parseCharSet(String charSet1Block) throws IllegalArgumentException {
        // "4-629\3"
        String[] parts = Objects.requireNonNull(charSet1Block).split("-");

        int from, to, every = 1;
        try {
            if (parts.length == 2) {
                // from site
                from = Integer.parseInt(parts[0].trim());

                // codons : 629\3
                if (parts[1].contains("/")) // to avoid typo
                    throw new IllegalArgumentException("Invalid delimiter for codon positions ! " + parts[1]);
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
                throw new NumberFormatException();
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("block " + charSet1Block + " cannot be parsed");
        }

        return new CharSetBlock(from, to, every);
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
    private void readDataBlock(List<Taxon> taxonList) throws ImportException, IOException {

        taxonCount = 0;
        siteCount = 0;
        sequenceType = null;

        readDataBlockHeader("MATRIX", NexusBlock.DATA);

        if ( DataType.isSame(sequenceType, Continuous.getInstance()) ) {

            LoggerUtils.log.info("Loading continuous character data ... ");
            continuousCharacterData = readContinuousCharacterData();

        } else {
            List<Sequence> sequences = readSequenceData(taxonList);
            setSimpleAlignment(sequences);
        }

        findEndBlock();
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


    // rows are taxa, cols are traits.
    // Double[][] taxa should have same order of Taxon[].
    private ContinuousCharacterData readContinuousCharacterData() throws ImportException, IOException {
        assert taxonCount > 0 && siteCount > 0;
        Double[][] continuousData = new Double[taxonCount][siteCount];
        lphy.evolution.Taxon[] taxa = new lphy.evolution.Taxon[taxonCount];

        if (isInterleaved) {

            throw new UnsupportedOperationException("in dev");

        } else {

            for (int i = 0; i < taxonCount; i++) {
                // 1st col is taxon name
                String token = helper.readToken();
                taxa[i] = new lphy.evolution.Taxon(token);

                // from 2nd col is traits, must be double
                for (int j = 0; j < siteCount; j++) {
                    token = helper.readToken();

                    try {
                        continuousData[i][j] = Double.parseDouble(token);
                    } catch (NumberFormatException ex) {
                        // not enough columns
                        if (j < siteCount - 1)
                            throw new ImportException.ShortSequenceException(taxa[i].getName()
                                    + " has " + j + " traits, expecting " + siteCount);
                        else
                            throw new ImportException.BadFormatException("Double value is expected " +
                                    "for continuous data at taxon " + i + " trait " + j);
                    }

                } // end j loop

                // not enough
                if (helper.getLastDelimiter() == ';' && i < taxonCount - 1)
                    throw new ImportException.TooFewTaxaException(Integer.toString(i+1));
            } // end i loop

        }
        String token = helper.readToken(";");
        if (helper.getLastDelimiter() != ';') {
            throw new ImportException.BadFormatException("Expecting ';' after continuous data\n" +
                    helper.getLastDelimiter());
        }

        return new ContinuousCharacterData(new Taxa.Simple(taxa), continuousData);
    }



    private void readDataBlockHeader(String tokenToLookFor, NexusBlock block) throws ImportException, IOException {

        boolean foundDimensions = false, foundTitle = false, foundFormat = false;
        String token;

        do {
            token = helper.readToken(); //TODO read comments after MATRIX, but readToken() skips comments

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
//TODO new datatypes here
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


    //****** NexusBlock ******//

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
    public enum NexusBlock {
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

    public NexusBlock findNextBlock() throws IOException {
        findToken("BEGIN", true);
        nextBlockName = helper.readToken(";").toUpperCase();
        return findBlockName(nextBlockName);
    }

    protected NexusBlock findBlockName(String blockName) {
        try {
            nextBlock = NexusBlock.valueOf(blockName);
        } catch (IllegalArgumentException e) {
            // handle unknown blocks. java 1.5 throws an exception in valueOf
            nextBlock = null;
        }

        if (nextBlock == null) {
            nextBlock = NexusBlock.UNKNOWN;
        }

        return nextBlock;
    }

    /**
     * Read ahead to the end of the current block.
     */
    public void findEndBlock() throws IOException
    {
        try {
            String token;

            do {
                token = helper.readToken(";");
            } while ( !token.equalsIgnoreCase("END") && !token.equalsIgnoreCase("ENDBLOCK") );
        } catch (EOFException e) {
            // Doesn't matter if the End is missing
        }

        nextBlock = NexusBlock.UNKNOWN;
    }


    protected boolean isNotEnd(String token) {
        return !token.equalsIgnoreCase("END") && !token.equalsIgnoreCase("ENDBLOCK");
    }

    //****** parser ******//

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

        List<Taxon> taxa = new ArrayList<>();

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


}
