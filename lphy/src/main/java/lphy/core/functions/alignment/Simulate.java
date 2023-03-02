package lphy.core.functions.alignment;

import lphy.core.LPhyParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.MapValue;
import lphy.parser.REPL;
import lphy.system.PathVariables;
import lphy.system.UserDir;
import lphy.util.LoggerUtils;
import lphy.util.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulate data from a given lphy script.
 * Use {@link lphy.core.functions.Get} to get the value of simulated result.
 * @author Walter Xie
 */
public class Simulate extends DeterministicFunction<Map<String, Object>> {

    private static final String lphyScriptParamName = "lphy";
    private static final String seedParamName = "seed";

    private Map<String, Object> simResMap = new HashMap<>();

    public Simulate(@ParameterInfo(name = lphyScriptParamName,
            description = "the file path of the lphy script to simulate data.")
                    Value<String> filePathVal,
                    @ParameterInfo(name = seedParamName, description = "the seed (integer).")
                    Value<Integer> seedVal){
        if (filePathVal == null) throw new IllegalArgumentException("The lphy file path can't be null!");
        setInput(lphyScriptParamName, filePathVal);

        if (seedVal == null) throw new IllegalArgumentException("The seed must be an integer !");
        setInput(seedParamName, seedVal);
    }

    @GeneratorInfo(name = "simulate", verbClause = "is the map of", narrativeName = "simulation result",
            description = "The function to simulate data from a given lphy script.")
    public Value<Map<String, Object>> apply() {
        Value<String> filePathVal = getParams().get(lphyScriptParamName);
        File filePath = PathVariables.convertPathVar(filePathVal.value()).toFile();
        if (!filePath.exists())
            filePath = UserDir.getUserPath(filePathVal.value()).toFile();
        if (!filePath.exists())
            throw new IllegalArgumentException("Cannot locate the lphy file path : " + filePath);

        Integer seed = (Integer) getParams().get(seedParamName).value();
        RandomUtils.setSeed(seed);

        LoggerUtils.log.info("Simulate data from lphy script: " + filePath.getAbsolutePath() +
                " using seed " + seed);

        LPhyParser parser = new REPL();
        try {
            // this also samples values while parsing
            parser.source(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //so, DO NOT need to create Sampler to sample

        // save all named var
        Map<String, Value<?>> model = parser.getModelDictionary();
        for (Map.Entry<String, Value<?>> entry : model.entrySet()) {
//          if (entry.getValue().value() instanceof Alignment al)
            //TODO put Value or Value.value()
            simResMap.put(entry.getKey(), entry.getValue().value());
//          System.out.println(entry.getKey() + " => " + entry.getValue());
        }
        return new MapValue(null, simResMap, this);
    }

    public Map<String, Object> getSimResMap() {
        return simResMap;
    }
}
