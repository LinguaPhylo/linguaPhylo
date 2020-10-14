package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.io.NexusOptions;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

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
        Value<String> fileName = getParams().get(fileParamName);
        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        Map<String, String> options = optionsVal == null ? null : optionsVal.value();

        NexusOptions nexusOptions = NexusOptions.getInstance();
        Alignment a = nexusOptions.getAlignment(fileName.value(), options, false);

        return new Value<Taxa>(a, this);
    }
}
