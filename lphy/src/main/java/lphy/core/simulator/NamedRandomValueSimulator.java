package lphy.core.simulator;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.logger.ValueFileLoggerListener;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;

import java.io.File;
import java.io.IOException;
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

    SimulatorListener simulatorListener;

    Sampler sampler;

    public NamedRandomValueSimulator() {
        simulatorListener = new ValueFileLoggerListener();
    }

    /**
     * Simulate using the model defined by a lphy file, which may contain Macro.
     * Call {@link #simulate(File, String, int, String[], Long) },
     * after the preprocessing is done.
     * It should consider this method first, unless there is some customised process.
     * @param lphyFile         input file
     * @param outputFilePrefix  output file prefix, if null, then use the input file prefix
     * @param numReplicates    number of replicates of simulations
     * @param constants    constants inputted by user using macro
     * @param seed         the seed value, if null then use a random seed.
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    public Map<Integer, List<Value>> simulateAndLog(File lphyFile, String outputFilePrefix, int numReplicates,
                                              String[] constants, Long seed) throws IOException {
        // must use absolute path, otherwise parent could be null for relative path
        File outDir = lphyFile.getAbsoluteFile().getParentFile();
        FileConfig.Utils.validate(lphyFile, outDir);
        // must provide File lphyFile, int numReplicates, Long seed
        Map<Integer, List<Value>> allReps = simulate(lphyFile, outputFilePrefix,
                    numReplicates, constants, seed);
        System.out.println("Write all files to " + (outDir !=null ? outDir : OutputSystem.getOutputDirectory()));
        return allReps;
    }

    /**
     * Simulate using the model defined by a lphy file, which may contain Macro.
     * It also includes some preprocessing, such as initiating {@link SimulatorListener},
     * creating {@link Sampler}.
     * @param lphyFile         input file
     * @param outputFilePrefix  output file prefix, if null, then use the input file prefix
     * @param numReplicates    number of replicates of simulations
     * @param constants    constants inputted by user using macro
     * @param seed         the seed value, if null then use a random seed.
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    public Map<Integer, List<Value>> simulate(File lphyFile, String outputFilePrefix, int numReplicates,
                                              String[] constants, Long seed) throws IOException {
        // ValueFileLoggerListener start() requires
        if (outputFilePrefix == null)
            outputFilePrefix = getLPhyFilePrefix(lphyFile);
        simulatorListener.start(numReplicates, outputFilePrefix);

        // TODO duplicate to maps in ValueFileLoggerListener
        Map<Integer, List<Value>> simResMap = new HashMap<>();

        // create Sampler given a lphy script file
        sampler = Sampler.createSampler(lphyFile, constants);

        long start = System.currentTimeMillis();

        for (int i = SimulatorListener.REPLICATES_START_INDEX; i < numReplicates; i++) {
            List<Value> values = sampler.sample(seed);
            // filter to RandomValue
            List<Value> namedRandomValueList = getNamedRandomValues(values);

            simulatorListener.replicate(i, namedRandomValueList);

            simResMap.put(i, namedRandomValueList);
        }
        simulatorListener.complete();

        long end = System.currentTimeMillis();
        System.out.println("Sampled " + lphyFile + " at " + numReplicates + (numReplicates >1?" times":" time") +
                " which takes " + (end - start) + " ms.");
        return simResMap;
    }

    /**
     * this does not have a clear view for the requirement,
     * and should be replaced by {@link #simulate(File, String, int, String[], Long)}
     * @param fileConfig   require lphyInputFile, numReplicates, and seed
     * @param constants    constants inputted by user using macro
     * @return             All simulation results in a map, key is the index of replicates.
     * @throws IOException
     */
    @Deprecated
    public Map<Integer, List<Value>> simulate(FileConfig fileConfig, String[] constants) throws IOException {

        File lphyFile = fileConfig.lphyInputFile;
        int numReplicates = fileConfig.numReplicates;
        Long seed = fileConfig.seed; // if null then random seed

        return simulate(lphyFile, null, numReplicates, constants, seed);
    }


    public static boolean isNamedRandomValue(Value value) {
        return value instanceof RandomVariable ||
                // random value but no anonymous
                (value.isRandom() && !value.isAnonymous());
    }

    public static List<Value> getNamedRandomValues(List<Value> values) {
        return values.stream()
                .filter(NamedRandomValueSimulator::isNamedRandomValue)
                .toList();
    }

}
