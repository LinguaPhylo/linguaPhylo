package lphy.core.simulator;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.MapValue;
import lphy.core.system.PathVariables;
import lphy.core.system.UserDir;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static lphy.core.simulator.SimulateUtils.simulateLPhyScriptOutToFile;


/**
 * Simulate data from a given lphy script.
 * Use lphy base function Get to get the value of simulated result.
 * @author Walter Xie
 */
public class Simulate extends DeterministicFunction<Map<String, Object>> {

    private static final String lphyScriptParamName = "lphy";
    private static final String seedParamName = "seed";
    private static final String outParamName = "outDir";

    private Map<String, Object> simResMap;

    public Simulate(@ParameterInfo(name = lphyScriptParamName,
            description = "the file path of the lphy script to simulate data.")
                    Value<String> filePathVal,
                    @ParameterInfo(name = seedParamName, description = "the seed (integer).")
                    Value<Integer> seedVal,
                    @ParameterInfo(name = outParamName,
                            description = "the directory to output the simulated values. " +
                                    "Default to the parent directory of the given lphy script.",
                    optional = true) Value<String> outVal){
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
        File filePath = PathVariables.convertPathVar(filePathVal.value()).toFile();
        if (!filePath.exists())
            filePath = UserDir.getUserPath(filePathVal.value()).toFile();
        // it must be provided because of deterministic func
        Long seed = ((Integer) getParams().get(seedParamName).value()).longValue();

        File outDir = Objects.requireNonNull(filePath.getAbsoluteFile()).getParentFile();
        Value<String> outVal = getParams().get(outParamName);
        if (outVal != null) {
            outDir = PathVariables.convertPathVar(outVal.value()).toFile();
        }

        Sampler sampler = null;
        try {
            // only sample 1 time
            sampler = simulateLPhyScriptOutToFile(filePath, Sampler.REP_START, seed, outDir);
        } catch (IOException e) {
            LoggerUtils.log.severe("Cannot parse LPhy script file ! " + filePath.getAbsolutePath());
            LoggerUtils.logStackTrace(e);
        }

        List<Value<?>> values = Objects.requireNonNull(sampler).getValuesAllRepsMap().get(Sampler.REP_START);
        // simResMap stores v.value(), and key is id
        for (Value<?> v : values) {
            if (v.isRandom())
                simResMap.put(v.getId(), v.value());
        }

        return new MapValue(null, simResMap, this);
    }

    public Map<String, Object> getSimResMap() {
        return simResMap;
    }
}
