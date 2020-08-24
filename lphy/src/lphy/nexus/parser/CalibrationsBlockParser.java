package lphy.nexus.parser;

import lphy.evolution.traits.CharSetBlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse calibrations
 * Modified from BEAST 2
 */
public class CalibrationsBlockParser extends NexusBlockParser {

    public static final String TIPCALIBRATION = "tipcalibration";
    public static final String SCALE = "scale";
    public static final String OPTIONS = "options";

    // charset
    protected Map<String, List<CharSetBlock>> charsetMap = new HashMap<>();

    public Map<String, List<CharSetBlock>> getCharsetMap() {
        return charsetMap;
    }

    /**TODO scale fixed to year
     * parse calibrations block and create TraitSet *
     */
    public Map<String, Double> parseCalibrationsBlock(final BufferedReader fin) throws IOException {
        String str;
        do {
            str = nextLine(fin);
            if (str.toLowerCase().contains(OPTIONS)) {
                String scale = getAttValue(SCALE, str);
                if (scale.endsWith("s")) { // years
                    scale = scale.substring(0, scale.length() - 1);
                }

            }
        } while (str.toLowerCase().contains(TIPCALIBRATION));

        Map<String, Double> taxaAges = new TreeMap<>();

        String text = "";
        while ( !(str = nextLine(fin)).contains(";") ) {
            text += str;
        }
        final String[] strs = text.split(",");
        for (final String str2 : strs) {
            final String[] parts = str2.split(":");
            final String[] taxa = parts[1].split("\\s+");
            final String date = parts[0].replaceAll(".*=\\s*", "");
            // TODO incorrect with month, day
            Double numericDate = Double.parseDouble(date);
            for (final String taxon : taxa) {
                if (!taxon.matches("^\\s*$")) {
                    taxaAges.put(taxon, numericDate);
                }
            }
        }

        return taxaAges;
    } // parseCalibrations


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
    public void parseAssumptionsBlock(final BufferedReader fin) throws IOException {
        String str;
        do {
            str = nextLine(fin);
            if (str.toLowerCase().matches("\\s*charset\\s.*")) {
                // remove text in brackets (as TreeBase files are wont to contain)
                str = str.replaceAll("\\(.*\\)", "");
                // clean up spaces
                str = str.replaceAll("=", " = ");
                str = str.replaceAll("^\\s+", "");
                str = str.replaceAll("\\s*-\\s*", "-");
                str = str.replaceAll("\\s*\\\\\\s*", "\\\\");
                str = str.replaceAll("\\s*;", "");
                // replace "," to " " as BEAST 1 uses ,
                str = str.replaceAll(",\\s+", " ");
                // use white space as delimiter
                final String[] strs = str.trim().split("\\s+");
                final String id = strs[1];
                String rangeString = "";
                for (int i = 3; i < strs.length; i++) {
                    rangeString += strs[i] + " ";
                }
                rangeString = rangeString.trim().replace(' ', ',');

                List<CharSetBlock> blocks = getCharSetBlocks(rangeString);
                charsetMap.put(id, blocks);


            } else if (str.toLowerCase().matches("\\s*wtset\\s.*")) {
                throw new UnsupportedOperationException("in dev");
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
            } else if (str.toLowerCase().matches("\\s*taxset\\s.*")) {
                throw new UnsupportedOperationException("in dev");
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
            } else if (str.toLowerCase().matches("^\\s*calibrate\\s.*")) {
                throw new UnsupportedOperationException("in dev");
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
            }

        } while (!str.toLowerCase().contains("end;"));
    }


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

    private List<CharSetBlock> getCharSetBlocks(String rangeString) {
        List<CharSetBlock> blocks = new ArrayList<>();

        String[] ranges = rangeString.split(",");

        for (String range : ranges) {
            String[] parts = range.split("-");

            int from;
            int to;
            int every = 1;

            try {
                if (parts.length == 2) {
                    from = Integer.parseInt(parts[0].trim());

                    String[] toParts = parts[1].split("\\\\");

                    if (toParts[0].trim().equals(".")) {
                        to = -1;
                    } else {
                        to = Integer.parseInt(toParts[0].trim());
                    }

                    every = 1;
                    if (toParts.length > 1) every = Integer.parseInt(toParts[1].trim());

                } else if (parts.length == 1) {
                    from = Integer.parseInt(parts[0].trim());
                    to = from;
                } else {
                    throw new IllegalArgumentException("CharSet: " + rangeString + ", unable to be parsed");
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("CharSet: " + rangeString + ", unable to be parsed");
            }

            blocks.add(new CharSetBlock(from, to, every));
        }
        return blocks;
    }



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
