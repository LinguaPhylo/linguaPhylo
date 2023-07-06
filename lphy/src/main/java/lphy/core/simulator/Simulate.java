package lphy.core.simulator;

import lphy.core.io.FileConfig;
import lphy.core.io.PathVariables;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.MapValue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 * Simulate data from a given lphy script.
 * Use lphy base function Get to get the value of simulated result.
 *
 * @author Walter Xie
 */
public class Simulate extends DeterministicFunction<Map<String, Object>> {

    private static final String lphyScriptParamName = "lphy";
    private static final String seedParamName = "seed";
    private static final String outParamName = "outDir";

    private Map<String, Object> simResMap;

    private static Simulator simulator = new Simulator();

    public Simulate(@ParameterInfo(name = lphyScriptParamName,
            description = "the file path of the lphy script to simulate data.")
                    Value<String> filePathVal,
                    @ParameterInfo(name = seedParamName, description = "the seed (integer).")
                    Value<Integer> seedVal,
                    @ParameterInfo(name = outParamName,
                            description = "the directory to output the simulated values. " +
                                    "Default to the parent directory of the given lphy script.",
                            optional = true) Value<String> outVal) {
        if (filePathVal == null) throw new IllegalArgumentException("The lphy file path can't be null!");
        setInput(lphyScriptParamName, filePathVal);

        if (seedVal == null) throw new IllegalArgumentException("The seed must be an integer !");
        setInput(seedParamName, seedVal);

        if (outVal != null)
            setInput(outParamName, outVal);
    }

    @GeneratorInfo(name = "simulate", verbClause = "is the map of", narrativeName = "simulation result",
            description = "The function to simulate data from a given lphy script.")
    public Value<Map<String, Object>> apply() {
        Value<String> filePathVal = getParams().get(lphyScriptParamName);
        File infile = PathVariables.convertPathVar(filePathVal.value()).toFile();
        if (!infile.exists())
            infile = UserDir.getUserPath(filePathVal.value()).toFile();

        // it must be provided because of deterministic func
        Long seed = ((Integer) getParams().get(seedParamName).value()).longValue();

        Value<String> outVal = getParams().get(outParamName);
        File outDir;
        if (outVal != null)
            outDir = PathVariables.convertPathVar(outVal.value()).toFile();
        else
            // If outVal = null as default, assign to the input file directory by default.
            outDir = Objects.requireNonNull(infile.getAbsoluteFile()).getParentFile();

        List<Value> values = null;
        try {
            // only sample 1 time
            FileConfig fileConfig = FileConfig.Utils
                    .createSimulationFileConfig(infile, outDir, SimulatorListener.REPLICATES_START_INDEX, seed);

            values = simulator.simulateAndSaveResults(fileConfig)
                    .get(SimulatorListener.REPLICATES_START_INDEX);

        } catch (IOException e) {
            LoggerUtils.log.severe("Cannot parse LPhy script file ! " + infile.getAbsolutePath());
            LoggerUtils.logStackTrace(e);
        }

        simResMap = new TreeMap<>();
        for (Value<?> v : Objects.requireNonNull(values)) {
            if (v.isRandom())
                simResMap.put(v.getId(), v.value());
        }

        return new MapValue(null, simResMap, this);
    }

    public Map<String, Object> getSimResMap() {
        return simResMap;
    }
}
