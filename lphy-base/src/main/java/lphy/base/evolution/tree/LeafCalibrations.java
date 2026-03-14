package lphy.base.evolution.tree;

import lphy.base.distribution.Uniform;
import lphy.base.function.io.ReaderConst;
import lphy.core.io.UserDir;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LeafCalibrations implements GenerativeDistribution<Double[]> {

    Value<String> file;
    List<TipCalibration> calibrations;

    /**
     * Stores the calibration distributionName and its parameters for one tip.
     *
     * Supported distributionNames and their parameter layout:
         calibrate <node_name> = fixed()
         calibrate <node_name> = normal(<mean>,)
         calibrate <node_name> = uniform(<min_age>,<max_age>)
         calibrate <node_name> = offsetexponential(<min_age>,<mean_age>)
         calibrate <node_name> = truncatednormal(<min_age>,<mean_age>,)
         calibrate <node_name> = lognormal(<mean_age>,)
         calibrate <node_name> = offsetlognormal(<min_age>,<mean_age>,)
         calibrate <node_name> = gamma(<mean_age>,)
         calibrate <node_name> = offsetgamma(<min_age>,<mean_age>,)
     */
    public static class TipCalibration {

        public final String taxonName;
        public final String distributionName;
        public final double[] params;        // raw input params as parsed from nexus
        public final double[] computedParams; // actual distribution parameters used for sampling

        // LinkedHashMap preserves insertion order
        private static final Map<String, TipCalibration> calibrationMap = new LinkedHashMap<>();

        public TipCalibration(String taxonName, String distributionName, double... params) {
            this.taxonName = taxonName;
            this.distributionName = distributionName.toLowerCase(Locale.ROOT);
            this.params = params;
            this.computedParams = computeDistributionParams(this.distributionName, params);
            // Do NOT auto-insert here — insertion order is controlled externally
        }

        /**
         * Converts raw nexus input params into the actual distribution parameters used for sampling.
         * For distributions where input is on real scale (lognormal, offsetlognormal),
         * this performs moment-matching to get log-scale parameters.
         * For offset distributions, stores offset separately at index 0.
         */
        private static double[] computeDistributionParams(String distName, double[] params) {
            return switch (distName) {

                // fixed(<age>) → [age]
                case "fixed" -> new double[]{ params[0] };

                // uniform(<min_age>,<max_age>) → [min, max]
                case "uniform" -> new double[]{ params[0], params[1] };

                // normal(<mean>,<sd=1.0>) → [mean, sd]
                case "normal" -> new double[]{ params[0], paramOrDefault(params, 1, 1.0) };

                // truncatednormal(<min_age>,<mean_age>,<sd=1.0>) → [minAge, mean, sd]
                case "truncatednormal" -> new double[]{ params[0], params[1], paramOrDefault(params, 2, 1.0) };

                // offsetexponential(<min_age>,<mean_age>) → [minAge, mean]
                case "offsetexponential" -> new double[]{ params[0], params[1] };

                // lognormal(<mean_age>,<sd=1.0>): convert real-scale mean+sd to log-scale muLog, sigmaLog
                // sigmaLog = sqrt(log(1 + sd²/mean²)), muLog = log(mean) - 0.5*sigmaLog²
                case "lognormal" -> {
                    double mean = params[0];
                    double sd = paramOrDefault(params, 1, 1.0);
                    double sigmaLog = Math.sqrt(Math.log(1.0 + (sd * sd) / (mean * mean)));
                    double muLog  = Math.log(mean) - 0.5 * sigmaLog * sigmaLog;
                    yield new double[]{ muLog, sigmaLog };
                }

                // offsetlognormal(<min_age>,<mean_age>,<sd=1.0>): convert real-scale mean+sd to log-scale
                // sigmaLog = sqrt(log(1 + sd²/mean²)), muLog = log(mean) - 0.5*sigmaLog²
                case "offsetlognormal" -> {
                    double minAge = params[0];
                    double mean = params[1];
                    double sd = paramOrDefault(params, 2, 1.0);
                    double sigmaLog = Math.sqrt(Math.log(1.0 + (sd * sd) / (mean * mean)));
                    double muLog  = Math.log(mean) - 0.5 * sigmaLog * sigmaLog;
                    yield new double[]{ minAge, muLog, sigmaLog };
                }

                // gamma(<mean_age>,<sd=1.0>): moment-matching → shape = mean²/sd², scale = sd²/mean
                case "gamma" -> {
                    double mean  = params[0];
                    double sd  = paramOrDefault(params, 1, 1.0);
                    double shape = (mean * mean) / (sd * sd);
                    double scale = (sd * sd) / mean;
                    yield new double[]{ shape, scale };
                }

                // offsetgamma(<min_age>,<mean_age>,<sd=1.0>): moment-matching → shape = mean²/sd², scale = sd²/mean
                case "offsetgamma" -> {
                    double minAge = params[0];
                    double mean = params[1];
                    double sd = paramOrDefault(params, 2, 1.0);
                    double shape  = (mean * mean) / (sd * sd);
                    double scale  = (sd * sd) / mean;
                    yield new double[]{ minAge, shape, scale };
                }

                default -> params.clone();
            };
        }

        /**
         * Returns params[index] if it exists, otherwise returns defaultValue.
         * Used for optional sd parameter in distributions.
         */
        private static double paramOrDefault(double[] params, int index, double defaultValue) {
            return (params.length > index) ? params[index] : defaultValue;
        }

        // Called explicitly after reordering, so map reflects MATRIX order
        static void register(TipCalibration cal) {
            calibrationMap.put(cal.taxonName, cal);
        }

        /**
         * Look up a calibration by taxon name and return a formatted description
         * of its distribution and parameters, e.g. "normal(1984.0, 5.0)".
         */
        public static String getDistribution(String taxonName) {
            TipCalibration cal = calibrationMap.get(taxonName);
            if (cal == null) return null;

            StringBuilder sb = new StringBuilder(cal.distributionName).append("(");
            for (int i = 0; i < cal.params.length; i++) {
                sb.append(cal.params[i]);
                if (i < cal.params.length - 1) sb.append(", ");
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * Look up a calibration by taxon name and return the TipCalibration object.
         */
        public static TipCalibration getCalibration(String taxonName) {
            return calibrationMap.get(taxonName);
        }

        public static String getTaxonName(int i) {
            List<String> keys = new ArrayList<>(calibrationMap.keySet());
            if (i < 0 || i >= keys.size())
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds for calibrationMap of size " + keys.size());
            return keys.get(i);
        }

        /**
         * Clear the static calibration map, e.g. between runs or in tests.
         */
        public static void clearCalibrations() {
            calibrationMap.clear();
        }

        /**
         * Sample an age from this calibration distribution using computedParams.
         */
        public double sampleAge() {
            return switch (distributionName) {

                // fixed(<age>)
                case "fixed" -> computedParams[0];

                // uniform(<min_age>,<max_age>)
                case "uniform" -> {
                    Uniform uniform = new Uniform(new Value<>("", computedParams[0]), new Value<>("", computedParams[1]));
                    yield uniform.sample().value();
                }

                // normal(<mean>,<sd=1.0>)
                case "normal" -> {
                    NormalDistribution normal = new NormalDistribution(computedParams[0], computedParams[1]);
                    yield normal.sample();
                }

                // truncatednormal(<min_age>,<mean_age>,<sd=1.0>)
                case "truncatednormal" -> {
                    double minAge = computedParams[0];
                    NormalDistribution normal = new NormalDistribution(computedParams[1], computedParams[2]);
                    double s;
                    do { s = normal.sample(); } while (s < minAge);
                    yield s;
                }

                // offsetexponential(<min_age>,<mean_age>)
                case "offsetexponential" -> {
                    ExponentialDistribution exp = new ExponentialDistribution(computedParams[1]);
                    yield computedParams[0] + exp.sample();
                }

                // lognormal: computedParams are [muLog, sigmaLog]
                case "lognormal" -> {
                    LogNormalDistribution logNormal = new LogNormalDistribution(computedParams[0], computedParams[1]);
                    yield logNormal.sample();
                }

                // offsetlognormal: computedParams are [minAge, muLog, sigmaLog]
                case "offsetlognormal" -> {
                    LogNormalDistribution logNormal = new LogNormalDistribution(computedParams[1], computedParams[2]);
                    yield computedParams[0] + logNormal.sample();
                }

                // gamma: computedParams are [shape, scale]
                case "gamma" -> {
                    GammaDistribution gamma = new GammaDistribution(computedParams[0], computedParams[1]);
                    yield gamma.sample();
                }

                // offsetgamma: computedParams are [minAge, shape, scale]
                case "offsetgamma" -> {
                    GammaDistribution gamma = new GammaDistribution(computedParams[1], computedParams[2]);
                    yield computedParams[0] + gamma.sample();
                }

                default -> throw new IllegalArgumentException(
                        "Unknown calibration distribution: '" + distributionName + "' for taxon '" + taxonName + "'");
            };
        }

        @Override
        public String toString() {
            return "TipCalibration{taxon='" + taxonName
                    + "', dist='" + distributionName
                    + "', inputParams=" + Arrays.toString(params)
                    + "', computedParams=" + Arrays.toString(computedParams)
                    + '}';
        }
    }


    public LeafCalibrations(
            @ParameterInfo(name = ReaderConst.FILE, description = "the name of nexus file including path. The nexus file contains leaf calibrations.") Value<String> file
    ) {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        String name = file.value();
        if (!(name.endsWith(".nexus") || name.endsWith(".nxs") || name.endsWith(".nex"))) {
            throw new IllegalArgumentException("File format error: only .nexus / .nxs / .nex files are accepted.");
        }

        this.file = file;
    }

    @GeneratorInfo(name = "LeafCalibrations", examples = {"readNexusCalibrations.lphy"}, description = "Get a double array of ages for the tip dates according to " +
            "the calibrations in the nexus file, ordered to match the sequence order in the DATA block.")
    @Override
    public RandomVariable<Double[]> sample() {

        Path filePath = UserDir.getUserPath(getFile().value());

        List<String> taxaOrder = new ArrayList<>();
        Map<String, TipCalibration> calibrationByName = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            parseBothBlocks(reader, taxaOrder, calibrationByName);
        } catch (IOException e) {
            throw new RuntimeException("Could not read nexus file: " + filePath
                    + "\n  UserDir: " + UserDir.getUserDir(), e);
        }

        if (calibrationByName.isEmpty())
            throw new IllegalStateException(
                    "No CALIBRATE entries found in the ASSUMPTIONS block of " + filePath);

        if (taxaOrder.isEmpty())
            throw new IllegalStateException(
                    "No taxa found in the DATA MATRIX block of " + filePath);

        // Reorder calibrations to match the sequence order in the MATRIX block
        calibrations = new ArrayList<>();
        TipCalibration.calibrationMap.clear(); // reset before re-registering in correct order
        for (String taxonName : taxaOrder) {
            TipCalibration cal = calibrationByName.get(taxonName);
            if (cal == null)
                throw new IllegalStateException("No CALIBRATE entry found for taxon: '" + taxonName + "'");
            calibrations.add(cal);
            TipCalibration.register(cal); // register in MATRIX order
        }

        Double[] ages = calibrations.stream().map(TipCalibration::sampleAge).toArray(Double[]::new);

        return new RandomVariable<>("", ages, this);
    }

    /**
     * Single-pass read of the whole file, populating:
     *   taxaOrder        — taxon names in MATRIX row order
     *   calibrationByName — taxonName → TipCalibration from ASSUMPTIONS block
     */
    private void parseBothBlocks(BufferedReader reader,
                                 List<String> taxaOrder,
                                 Map<String, TipCalibration> calibrationByName) throws IOException {
        boolean inMatrix = false;
        boolean inAssumptions = false;
        StringBuilder assumptionsBlock = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            String upper = trimmed.toUpperCase(Locale.ROOT);

            // --- MATRIX block: collect taxa order ---
            if (upper.startsWith("MATRIX"))
                inMatrix = true;

            if (inMatrix && !upper.startsWith("MATRIX")) {
                if (trimmed.equals(";")) {
                    inMatrix = false;
                } else if (!trimmed.isEmpty()) {
                    // first whitespace-delimited token is the taxon name
                    String taxonName = trimmed.split("\\s+")[0];
                    taxaOrder.add(taxonName);
                }
            }

            // --- ASSUMPTIONS block: accumulate text ---
            if (upper.startsWith("BEGIN ASSUMPTIONS"))
                inAssumptions = true;

            if (inAssumptions)
                assumptionsBlock.append(trimmed).append(' ');

            if (inAssumptions && upper.startsWith("END"))
                inAssumptions = false;
        }

        // Parse calibrations from the accumulated assumptions block
        if (assumptionsBlock.isEmpty()) return;

        String block = assumptionsBlock.toString();
        for (String semicolonToken : block.split(";")) {
            for (String token : splitOnCommasOutsideParens(semicolonToken)) {
                String s = token.trim();
                if (s.toUpperCase(Locale.ROOT).startsWith("CALIBRATE")) {
                    TipCalibration cal = parseCalibrateLine(s);
                    if (cal != null)
                        calibrationByName.put(cal.taxonName, cal);
                }
            }
        }
    }

    /**
     * Splits a string on commas that are outside parentheses, so that
     * parameter lists like "uniform(1984,1988)" are kept intact.
     */
    private static String[] splitOnCommasOutsideParens(String input) {
        List<String> parts = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) {
                parts.add(input.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(input.substring(start));
        return parts.toArray(new String[0]);
    }

    /**
     * Parses a single CALIBRATE statement such as:
     * <pre>
     *   CALIBRATE D4Thai_1984 = offsetlognormal(1980,4,1.25)
     * </pre>
     */
    private TipCalibration parseCalibrateLine(String stmt) {
        String rest = stmt.replaceFirst("(?i)^CALIBRATE\\s+", "").trim();

        int eq = rest.indexOf('=');
        if (eq < 0) return null;

        String taxonName = rest.substring(0, eq).trim();
        String distExpr  = rest.substring(eq + 1).trim();

        int parenOpen  = distExpr.indexOf('(');
        int parenClose = distExpr.lastIndexOf(')');
        if (parenOpen < 0 || parenClose < 0) return null;

        String distName = distExpr.substring(0, parenOpen).trim();
        String paramsPart = distExpr.substring(parenOpen + 1, parenClose).trim();

        double[] params = Arrays.stream(paramsPart.split(","))
                .map(String::trim)
                .mapToDouble(Double::parseDouble)
                .toArray();

        return new TipCalibration(taxonName, distName, params);
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(ReaderConst.FILE, file);
        return map;
    }

    @Override
    public void setParam(String paramName, Value<?> value) {
        if (ReaderConst.FILE.equals(paramName))
            file = (Value<String>) value;
        else
            throw new IllegalArgumentException("Unknown parameter: " + paramName);
    }

    public Value<String> getFile() {
        return getParams().get(ReaderConst.FILE);
    }

    public List<TipCalibration> getCalibrations() {
        return calibrations;
    }
}