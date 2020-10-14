package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.io.NexusOptions;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringValue;

import java.util.Map;

/**
 * use {@link ReadNexus}
 * @author Walter Xie
 */
@Deprecated
public class ReadTaxa extends DeterministicFunction<Taxa> {

    private final String fileParamName;
    private final String optionsParamName;

    public ReadTaxa(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "options", description = NexusOptions.OPT_DESC,
                         optional=true) Value<Map<String, String>> options) {

        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        optionsParamName = getParamName(1);

        setParam(fileParamName, fileName);
        if (options != null) setParam(optionsParamName, options);

    }

    @GeneratorInfo(name="readTaxa",description = "A function that parses an taxa from a Nexus file.")
    public Value<Taxa> apply() {
        String fileName = ((StringValue) getParams().get(fileParamName)).value();

        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = NexusOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = NexusOptions.getAgeRegxStr(optionsVal);

        NexusParser nexusParser = new NexusParser(fileName);
        Alignment a = nexusParser.getLPhyAlignment(false, ageDirectionStr, ageRegxStr);

        return new Value<>(a, this);
    }
}
