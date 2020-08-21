package lphy.nexus.parser;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.tree.TimeTree;
import lphy.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Modified from BEAST 2
 * TODO ANTLR
 */
public class NexusParser {

    final protected Path nexfile;
    final protected NexusBlockParser blockParser;

    public List<String> taxa; // this will empty if no "taxlabels"
    public Alignment alignment;


//    public TraitSet traitSet;
//    public List<MRCAPrior> calibrations;

//    List<String> taxonList = new ArrayList<>(); // calibrate
    public List<TimeTree> trees;

//    public Map<String, String> translationMap = null;

//    public List<TaxonSet> taxonsets = new ArrayList<>();



//    protected List<NexusParserListener> listeners = new ArrayList<>();
    /**
     * Adds a listener for client classes that want to monitor progress of the parsing.
     */
//    public void addListener(final NexusParserListener listener) {
//        listeners.add(listener);
//    }


    public NexusParser(Path nexfile) {
        this.nexfile = nexfile;
        assert nexfile.toString().endsWith("nex") || nexfile.toString().endsWith("nexus");


        final String fileNameStem = nexfile.getFileName().toString().
                replaceAll(".*[\\/\\\\]", "").
                replaceAll("\\..*", "");

        this.blockParser = new NexusBlockParser();

        try {
            parseFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * parse DataFrame from nexus
     */
    public void parseFile() throws IOException {
        final BufferedReader reader = Files.newBufferedReader(nexfile); // StandardCharsets.UTF_8
        try {
//            while (reader.ready()) {
            String line;
            while ((line = blockParser.nextLine(reader)) != null) {
//                line = blockParser.nextLine(reader); // get the line not comment and not empty
//                if (line == null) { // why ?
////                    processTaxonSets();
//                    return;
//                }

                final String lower = line.toLowerCase();
                if (lower.matches("^\\s*begin\\s+taxa;\\s*$")) {
                    taxa = blockParser.parseTaxaBlock(reader);
                } else if (lower.matches("^\\s*begin\\s+data;\\s*$") ||
                        lower.matches("^\\s*begin\\s+characters;\\s*$")) {
                    alignment = blockParser.parseDataBlock(reader);
//                    alignment.setID(id);
                } else if (lower.matches("^\\s*begin\\s+trees;\\s*$")) {
//                    parseTreesBlock(reader);
                } else if (lower.matches("^\\s*begin\\s+calibration;\\s*$")) {
//                    traitSet = parseCalibrationsBlock(reader);
                } else if (lower.matches("^\\s*begin\\s+assumptions;\\s*$") ||
                        lower.matches("^\\s*begin\\s+sets;\\s*$") ||
                        lower.matches("^\\s*begin\\s+mrbayes;\\s*$")) {
//                parseAssumptionsBlock(reader);
                }
            } // end while

//            processTaxonSets();

        } catch (Exception e) {
        	e.printStackTrace();
            throw new IOException("Around line " + (blockParser.getLineNr()+1) + "\n" + e.getMessage());
        }
    } // parseFile
















    protected List<String> getIndexedTranslationMap(final Map<String, String> translationMap, final int origin) {

        LoggerUtils.log.warning("translation map size = " + translationMap.size());

        final String[] taxa = new String[translationMap.size()];

        for (final String key : translationMap.keySet()) {
            taxa[Integer.parseInt(key) - origin] = translationMap.get(key);
        }
        return Arrays.asList(taxa);
    }

    /**
     * @param translationMap
     * @return minimum key value if keys are a contiguous set of integers starting from zero or one, -1 otherwise
     */
    protected int getIndexedTranslationMapOrigin(final Map<String, String> translationMap) {

        final SortedSet<Integer> indices = new TreeSet<>();

        int count = 0;
        for (final String key : translationMap.keySet()) {
            final int index = Integer.parseInt(key);
            indices.add(index);
            count += 1;
        }
        if ((indices.last() - indices.first() == count - 1) && (indices.first() == 0 || indices.first() == 1)) {
            return indices.first();
        }
        return -1;
    }

    /**
     * @param translateArgs string containing arguments of the translate command
     * @return a map of taxa translations, keys are generally integer node number starting from 1
     *         whereas values are generally descriptive strings.
     * @throws IOException
     */
    protected Map<String, String> parseTranslateCommand(String translateArgs) throws IOException {

        final Map<String, String> translationMap = new HashMap<>();

        final String[] taxaTranslations = translateArgs.toString().split(",");
        for (String taxaTranslation : taxaTranslations) {
        	taxaTranslation = taxaTranslation.trim();
        	// find first whitespace character in taxaTranslation
        	int k = 0;
        	while (k < taxaTranslation.length() && !Character.isWhitespace(taxaTranslation.charAt(k))) {
        		k++;
        	}
            //final String[] translation = taxaTranslation.trim().split("[\t ]+");
            //if (translation.length == 2) {
            //    translationMap.put(translation[0], translation[1]);
        	if (k > 0) {
            	String nr = taxaTranslation.substring(0, k);
            	String translation = taxaTranslation.substring(k).trim();
            	translationMap.put(nr, translation);
//                Log.info.println(translation[0] + " -> " + translation[1]);
            } else {
                LoggerUtils.log.warning("Ignoring translation:" + taxaTranslation);
            }
        }
        return translationMap;
    }






//    protected void processSets() {
//    	// create monophyletic MRCAPrior for each taxon set that
//    	// does not already have a calibration associated with it
//    	for (TaxonSet taxonset : taxonsets) {
//    		boolean found = false;
//    		for (BEASTInterface o : taxonset.getOutputs()) {
//    			if (o instanceof MRCAPrior) {
//    				found = true;
//    				break;
//    			}
//    		}
//    		if (!found) {
//        		MRCAPrior prior = new MRCAPrior();
//        		prior.isMonophyleticInput.setValue(true, prior);
//        		prior.taxonsetInput.setValue(taxonset, prior);
//        		prior.setID(taxonset.getID() + ".prior");
//        		// should set Tree before initialising, but we do not know the tree yet...
//        		if (calibrations == null) {
//        			calibrations = new ArrayList<>();
//        		}
//        		calibrations.add(prior);
//    		}
//    	}
//	}



//    public static String generateSequenceID(final String taxon) {
//        String id = "seq_" + taxon;
//        int i = 0;
//        while (g_sequenceIDs.contains(id + (i > 0 ? i : ""))) {
//            i++;
//        }
//        id = id + (i > 0 ? i : "");
//        g_sequenceIDs.add(id);
//        return id;
//    }



    private ArrayList<String> readInCharstatelablesTokens(final BufferedReader fin) throws IOException {

        ArrayList<String> tokens = new ArrayList<>();
        String token="";
        final int READING=0, OPENQUOTE=1, WAITING=2;
        int mode = WAITING;
        int numberOfQuotes=0;
        boolean endOfBlock=false;
        String str;

        while (!endOfBlock) {
            str = blockParser.nextLine(fin);
            Character nextChar;
            for (int i=0; i< str.length(); i++) {
                nextChar=str.charAt(i);
                switch (mode) {
                    case WAITING:
                        if (!Character.isWhitespace(nextChar)) {
                            if (nextChar == '\'') {
                                mode=OPENQUOTE;
                            } else if (nextChar == '/' || nextChar == ',') {
                                tokens.add(nextChar.toString());
                                token="";
                            } else if (nextChar == ';') {
                                endOfBlock = true;
                            } else {
                                token=token+nextChar;
                                mode=READING;
                            }
                        }
                        break;
                    case READING:
                        if (nextChar == '\'') {
                            tokens.add(token);
                            token="";
                            mode=OPENQUOTE;
                        } else if (nextChar == '/' || nextChar == ',') {
                            tokens.add(token);
                            tokens.add(nextChar.toString());
                            token="";
                            mode=WAITING;
                        } else if (nextChar == ';') {
                            tokens.add(token);
                            endOfBlock = true;
                        } else if (Character.isWhitespace(nextChar)) {
                            tokens.add(token);
                            token="";
                            mode=WAITING;
                        } else {
                            token=token+nextChar;
                        }
                        break;
                    case OPENQUOTE:
                        if (nextChar == '\'') {
                            numberOfQuotes++;
                        } else {
                            if (numberOfQuotes % 2 == 0) {
                                for (int ind=0; ind< numberOfQuotes/2; ind++) {
                                    token=token+"'";
                                }
                                token=token+nextChar;
                            } else {
                                for (int ind=0; ind< numberOfQuotes/2; ind++) {
                                    token=token+"'";
                                }
                                tokens.add(token);
                                token="";
                                if (nextChar == '/' || nextChar == ',') {
                                    tokens.add(nextChar.toString());
                                    mode=WAITING;
                                } else if (nextChar == ';') {
                                    endOfBlock = true;
                                } else if (Character.isWhitespace(nextChar)) {
                                    mode=WAITING;
                                } else {
                                    token=token+nextChar;
                                    mode=READING;
                                }
                            }
                            numberOfQuotes=0;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (!tokens.get(tokens.size()-1).equals(",")) {
            tokens.add(",");
        }

        return tokens;
    }

//    private ArrayList<UserDataType> processCharstatelabelsTokens(ArrayList<String> tokens, int[] maxNumberOfStates) throws IOException {
//
//        ArrayList<UserDataType> charDescriptions = new ArrayList<>();
//
//        final int CHAR_NR=0, CHAR_NAME=1, STATES=2;
//        int mode = CHAR_NR;
//        int charNumber = -1;
//        String charName = "";
//        ArrayList<String> states = new ArrayList<>();
//
//        for (String token:tokens) {
//            switch (mode) {
//                case CHAR_NR:
//                    charNumber = Integer.parseInt(token);
//                    mode = CHAR_NAME;
//                    break;
//                case CHAR_NAME:
//                    if (token.equals("/")) {
//                        mode = STATES;
//                    } else if (token.equals(",")) {
//                        if (charNumber > charDescriptions.size()+1) {
//                            throw new IOException("Character descriptions should go in the ascending order and there " +
//                                    "should not be any description missing.");
//                        }
//                        charDescriptions.add(new UserDataType(charName, states));
//                        maxNumberOfStates[0] = Math.max(maxNumberOfStates[0], states.size());
//                        charNumber = -1;
//                        charName = "";
//                        states = new ArrayList<>();
//                        mode = CHAR_NR;
//                    } else {
//                        charName = token;
//                    }
//                    break;
//                case STATES:
//                    if (token.equals(",")) {
//                        if (charNumber > charDescriptions.size()+1) {
//                            throw new IOException("Character descriptions should go in the ascending order and there " +
//                                    "should not be any description missing.");
//                        }
//                        charDescriptions.add(new UserDataType(charName, states));
//                        maxNumberOfStates[0] = Math.max(maxNumberOfStates[0], states.size());
//                        charNumber = -1;
//                        charName = "";
//                        states = new ArrayList<>();
//                        mode = CHAR_NR;
//                    } else {
//                        states.add(token);
//                    }
//                default:
//                    break;
//            }
//        }
//
//        return charDescriptions;
//
//    }

    public static void main(final String[] args) {
        try {
            Path nexFile = Paths.get(args[0]);
            final NexusParser parser = new NexusParser(nexFile);

            if (parser.taxa != null) { // empty if no "taxlabels"
                System.out.println(parser.taxa.size() + " taxa");
                System.out.println(Arrays.toString(parser.taxa.toArray(new String[parser.taxa.size()])));
            }
            if (parser.alignment != null) {
                System.out.println(parser.alignment.toJSON());
            }
//            if (parser.traitSet != null) {
//                System.out.println(parser.traitSet);
//            }
//            if (parser.trees != null) {
//                System.out.println(parser.trees.size() + " trees");
//            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // main


} // class NexusParser
