package lphy.core.simulator;

import lphy.core.io.FileConfig;
import lphy.core.logger.ValueFileLoggerListener;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lphy.core.io.FileConfig.getLPhyFilePrefix;

/**
 * Simulate lphy file, and log true values and true trees.
 * It also includes some preprocessing, such as initiating {@link SimulatorListener},
 * creating {@link Sampler}.
 * In the end, it will use {@link ValueFileLoggerListener} to log true values and trees.
 */
public class NamedRandomValueSimulator {

    protected final SimulatorListener simulatorListener;

    protected Sampler sampler;

    public NamedRandomValueSimulator() {
        this(new ValueFileLoggerListener());
    }

    public NamedRandomValueSimulator(SimulatorListener simulatorListener) {
        this.simulatorListener = simulatorListener;
    }

    public LPhyParserDictionary getParserDictionary() {
        if (sampler == null) return null;
        return sampler.getParserDictionary();
    }

    /**
     * Simulate using the model defined by a lphy file, which may contain Macro.
     * Call {@link #simulate(File, int, String[], String[], Long)},
     * after the preprocessing is done.
     * It should consider this method first, unless there is some customised process.
     *
     * @param lphyFile         input file
     * @param outputFilePrefix  output file prefix, if null, then use the input file prefix
     * @param numReplicates    number of replicates of simulations
     * @param constants    constants inputted by user using macro
     * @param seed         the seed value, if null then use a random seed.
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    public Map<Integer, List<Value>> simulateAndLog(File lphyFile, String outputFilePrefix, int numReplicates,
                                              String[] constants, String[] varNotLog, Long seed) throws IOException {
        // must use absolute path, otherwise parent could be null for relative path
        File outDir = lphyFile.getAbsoluteFile().getParentFile();
        FileConfig.Utils.validate(lphyFile, outDir);

        // ValueFileLoggerListener start() requires
        if (outputFilePrefix == null)
            outputFilePrefix = getLPhyFilePrefix(lphyFile);
        simulatorListener.start(numReplicates, outputFilePrefix);

        // TODO duplicate to maps in ValueFileLoggerListener
        // must provide File lphyFile, int numReplicates, Long seed
        Map<Integer, List<Value>> allReps = simulate(lphyFile, numReplicates, constants, varNotLog, seed);

        for (Map.Entry<Integer, List<Value>> entry : allReps.entrySet()) {
            List<Value> namedRandomValueList = entry.getValue();

            simulatorListener.replicate(entry.getKey(), namedRandomValueList);
        }
        simulatorListener.complete();

        return allReps;
    }

    /**
     * Simulate using the model defined by a lphy file, which may contain Macro,
     * and create {@link Sampler}.
     * This is only doing simulation, logging is in another method,
     * e.g. {@link #simulateAndLog(File, String, int, String[], String[], Long)}.
     * @param lphyFile         input file
     * @param numReplicates    number of replicates of simulations
     * @param constants    constants inputted by user using macro
     * @param seed         the seed value, if null then use a random seed.
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    public Map<Integer, List<Value>> simulate(File lphyFile, int numReplicates, String[] constants,
                                              String[] varNotLog, Long seed) throws IOException {

        if (numReplicates < 1)
            throw new IllegalArgumentException("The replicate must be at least 1 time ! " +
                    "But numReplicates = " + numReplicates);

        Map<Integer, List<Value>> simResMap = new HashMap<>();

        long start = System.currentTimeMillis();

        // create Sampler by parsing a lphy script file,
        // during parsing, the values are simulated.
        sampler = Sampler.createSampler(lphyFile, constants);
        // take the values already simulated from parsing
        List<Value> values = GraphicalModelUtils.getAllValuesFromSinks(getParserDictionary());
        List<Value> namedRandomValueList = getNamedRandomValues(values, varNotLog);
        simResMap.put(SimulatorListener.REPLICATES_START_INDEX, namedRandomValueList);

        // start from 2nd replicate
        for (int i = SimulatorListener.REPLICATES_START_INDEX + 1; i < numReplicates; i++) {
            // sample() does not parse the script again
            values = sampler.sample(seed);
            // filter to RandomValue
            namedRandomValueList = getNamedRandomValues(values, varNotLog);

            simResMap.put(i, namedRandomValueList);
        }

        long end = System.currentTimeMillis();
        System.out.println("Sampled " + lphyFile + " at " + numReplicates + (numReplicates >1?" times":" time") +
                " which takes " + (end - start) + " ms.");
        return simResMap;
    }

    /**
     * this does not have a clear view for the requirement,
     * and should be replaced by {@link #simulate(File, int, String[], String[], Long)}
     * @param fileConfig   require lphyInputFile, numReplicates, and seed
     * @param constants    constants inputted by user using macro
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    @Deprecated
    public Map<Integer, List<Value>> simulate(FileConfig fileConfig, String[] constants,
                                              String[] varNotLog) throws IOException {

        File lphyFile = fileConfig.lphyInputFile;
        int numReplicates = fileConfig.numReplicates;
        Long seed = fileConfig.seed; // if null then random seed

        return simulate(lphyFile, numReplicates, constants, varNotLog, seed);
    }


    public static boolean isNamedRandomValue(Value value) {
        return value instanceof RandomVariable ||
                // random value but no anonymous
                (value.isRandom() && !value.isAnonymous());
    }

    // String[] varNotLog marks the random variables that will not be logged.
    // String is id or Canonical Id
    public static List<Value> getNamedRandomValues(List<Value> values, String[] varNotLog) {
        List<Value> namedRV = values.stream()
                .filter(NamedRandomValueSimulator::isNamedRandomValue)
                .toList();

        if (varNotLog == null || varNotLog.length < 1)
            return namedRV;
        else {
            List<Value> loggableRV = new ArrayList<>();

            for (Value val : namedRV) {
                boolean add = true;
                for (String id : varNotLog) {
                    if (id.equals(val.getId()) || id.equals(val.getCanonicalId())) {
                        add = false;
                        break;
                    }
                }
                if (add)
                    loggableRV.add(val);
            }

            return loggableRV;
        }
    }

}
