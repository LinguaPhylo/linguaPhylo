package lphy.core.functions;

import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.nexus.parser.NexusParser;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * data = nexus(file="primate-mtDNA.nex");
 */
public class Nexus extends DeterministicFunction<SimpleAlignment> {

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
    public Value<SimpleAlignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Path nexFile = Paths.get(fileName.value());

        Value<String[]> parts = getParams().get(partsParamName);

        SimpleAlignment alignment;
        if (parts != null) {
            alignment = parseNexus(nexFile, parts.value());
        } else {
            alignment = parseNexus(nexFile, null);
        }

        return new Value<>(alignment, this);
    }

    private SimpleAlignment parseNexus(Path nexFile, String[] value) {
        final NexusParser parser = new NexusParser(nexFile, value);
        SimpleAlignment alignment = parser.alignment;

        if (alignment.hasParts()) return (CharSetAlignment) alignment;
        return alignment;
    }
}
