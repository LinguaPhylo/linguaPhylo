package lphy.core.functions;

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
    private final String partParamName;

    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "partition", description = "the partition name inside Nexus.",
                         optional = true) Value<String> partName) {

        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        partParamName = getParamName(1);
        setParam(fileParamName, fileName);
        if (partName != null) setParam(partParamName, partName);
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<SimpleAlignment> apply() {
        //TODO NullPointerException if Value<SimpleAlignment> in Generator.hasRandomParameters(Generator.java:169)
//same Value<DataFrame>
        Value<String> fileName = getParams().get(fileParamName);
        Path nexFile = Paths.get(fileName.value());

        SimpleAlignment alignment = parseNexus(nexFile);

        return new Value<>(alignment, this);
    }


    private SimpleAlignment parseNexus(Path nexFile) {

        final NexusParser parser = new NexusParser(nexFile);

        SimpleAlignment alignment = parser.alignment;

        //TODO
        if (alignment.hasParts()) {

        }

        return alignment;
    }
}
