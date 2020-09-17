package lphy.core.functions;

import lphy.evolution.alignment.AbstractAlignment;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * data = nexus(file="primate.nex");
 * or data = nexus(file="primate.nex", charsets=["noncoding", "coding"]);
 */
public class Nexus extends DeterministicFunction<AbstractAlignment> {

    private final String fileParamName;
    private final String charsetsParamName;

    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "charsets", description = "the charset names of selected partitions in Nexus, " +
                         "if none then return a single-partition alignment.", optional=true) Value<String[]> charsets) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        charsetsParamName = getParamName(1);
        setParam(fileParamName, fileName);
        if (charsets != null) setParam(charsetsParamName, charsets);
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<AbstractAlignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String[]> charsets = getParams().get(charsetsParamName);

        AbstractAlignment alignment = parseNexus(fileName, charsets);
        return new Value<>(alignment, this);
    }

    private AbstractAlignment parseNexus(Value<String> fileName, Value<String[]> charsets) {
        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);

        String[] partsArray = charsets != null ? charsets.value() : null;
        // if value is null, ignoring charset return single partition
        return parser.getLPhyAlignment(partsArray);
    }
}
