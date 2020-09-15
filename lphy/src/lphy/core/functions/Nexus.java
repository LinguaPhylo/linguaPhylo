package lphy.core.functions;

import lphy.evolution.alignment.AbstractAlignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * data = nexus(file="primate-mtDNA.nex");
 */
public class Nexus extends DeterministicFunction<AbstractAlignment> {

    private final String fileParamName;
    private final String partsParamName;

    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "parts", description = "the names of selected partitions, " +
                         "if there is any extra defined in Nexus.", optional=true) Value<String[]> parts) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        partsParamName = getParamName(1);
        setParam(fileParamName, fileName);
        if (parts != null) setParam(partsParamName, parts);
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<AbstractAlignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String[]> parts = getParams().get(partsParamName);

        AbstractAlignment alignment = parseNexus(fileName, parts);
        return new Value<>(alignment, this);
    }

    private AbstractAlignment parseNexus(Value<String> fileName, Value<String[]> parts) {
        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);

        String[] partsArray = parts != null ? parts.value() : null;
        // if value is null, ignoring charset return single partition
        AbstractAlignment alignment = parser.getLPhyAlignment(partsArray);

        if (alignment.hasParts()) return (CharSetAlignment) alignment;
        return alignment;
    }
}
