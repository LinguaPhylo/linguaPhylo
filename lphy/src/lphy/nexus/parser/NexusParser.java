package lphy.nexus.parser;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.traits.CharSetBlock;
import lphy.evolution.tree.TimeTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Modified from BEAST 2
 * TODO ANTLR
 */
public class NexusParser {

    final protected Path nexfile;

    public List<String> taxa; // this will empty if no "taxlabels"
    public Alignment alignment;
    public Map<String, Double> taxaAges;

    public Map<String, List<CharSetBlock>> charsetMap;

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
        final NexusBlockParser blockParser = new NexusBlockParser(); // eventually move to subclasses
        final DataBlockParser dataBlockParser = new DataBlockParser();
        final CalibrationsBlockParser calibrationsBlockParser = new CalibrationsBlockParser();

        final BufferedReader reader = Files.newBufferedReader(nexfile); // StandardCharsets.UTF_8
        int ln = 0;
        try {
//            while (reader.ready()) {
            String line;
            while ((line = blockParser.nextLine(reader)) != null) {
//                line = blockParser.nextLine(reader);
//                if (line == null) { // why ?
////                    processTaxonSets();
//                    return;
//                }
                ln = blockParser.getLineNr(); // get the line not comment and not empty

                final String lower = line.toLowerCase();
                if (lower.matches("^\\s*begin\\s+taxa;\\s*$")) {
                    dataBlockParser.setLineNr(ln);
                    taxa = dataBlockParser.parseTaxaBlock(reader);
                    ln = dataBlockParser.getLineNr();

                } else if (lower.matches("^\\s*begin\\s+data;\\s*$") ||
                        lower.matches("^\\s*begin\\s+characters;\\s*$")) {
                    dataBlockParser.setLineNr(ln);
                    alignment = dataBlockParser.parseDataBlock(reader);
//                    alignment.setID(id);
                    ln = dataBlockParser.getLineNr();

                } else if (lower.matches("^\\s*begin\\s+calibration;\\s*$")) {
                    calibrationsBlockParser.setLineNr(ln);
                    taxaAges = calibrationsBlockParser.parseCalibrationsBlock(reader);
                    ln = calibrationsBlockParser.getLineNr();

                    if (taxaAges.size() != alignment.ntaxa())
                        throw new IllegalArgumentException("Number of dates " + taxaAges.size() +
                                " != number of taxa " + alignment.ntaxa());

                } else if (lower.matches("^\\s*begin\\s+assumptions;\\s*$") || // charset here
                        lower.matches("^\\s*begin\\s+sets;\\s*$") ||
                        lower.matches("^\\s*begin\\s+mrbayes;\\s*$")) {
                    calibrationsBlockParser.setLineNr(ln);
                    calibrationsBlockParser.parseAssumptionsBlock(reader); // TODO CharSetAlignment
                    ln = calibrationsBlockParser.getLineNr();

                    charsetMap = calibrationsBlockParser.getCharsetMap();


                } else if (lower.matches("^\\s*begin\\s+trees;\\s*$")) {
//                    parseTreesBlock(reader);

                }
            } // end while

//            processTaxonSets();

        } catch (Exception e) {
        	e.printStackTrace();
            throw new IOException("Around line " + ln + "\n" + e.getMessage());
        }
    } // parseFile


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




    public static void main(final String[] args) {
        try {
            Path nexFile = Paths.get(args[0]);
            final NexusParser parser = new NexusParser(nexFile);

            if (parser.taxa != null) { // empty if no "taxlabels"
                System.out.println(parser.taxa.size() + " taxa");
                System.out.println(Arrays.toString(parser.taxa.toArray(new String[parser.taxa.size()])));
            }
            if (parser.alignment != null) {
                if (parser.charsetMap != null) {
                    // primate-mtDNA.nex
                    for (Map.Entry<String, List<CharSetBlock>> entry : parser.charsetMap.entrySet()) {
                        System.out.println(entry.getKey() + " : " + Arrays.toString(entry.getValue().toArray()));
                    }

                } else {
                    System.out.println(parser.alignment.toJSON());
                }
            }
            if (parser.taxaAges != null) {
                // Dengue4.nex
                System.out.println(parser.taxaAges);
            }
//            if (parser.trees != null) {
//                System.out.println(parser.trees.size() + " trees");
//            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // main


} // class NexusParser
