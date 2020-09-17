package lphy.core.functions;

import lphy.evolution.alignment.AbstractAlignment;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * data = nexus(file="primate.nex");
 * or coding = nexus(file="primate.nex", charset="coding");
 */
public class Nexus extends DeterministicFunction<Alignment> {

    private final String fileParamName;
    private final String charsetParamName;

    // cache the partitions from one Nexus file
    private static CharSetAlignment charSetAlignment = null;
    private static String currentFileName = "";

    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "charset", description = "the charset name of selected partition in Nexus, " +
                         "if none then return the full alignment.", optional=true) Value<String> charset) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        charsetParamName = getParamName(1);
        setParam(fileParamName, fileName);
        if (charset != null) setParam(charsetParamName, charset);
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String> charset = getParams().get(charsetParamName);

        Alignment a;
        if (charset != null) {
            // must be CharSetAlignment
            if (charSetAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value())) {
                charSetAlignment = (CharSetAlignment) parseNexus(fileName, false);
                currentFileName = fileName.value();
            }
            a = charSetAlignment.getPartAlignment(charset.value());
        } else {
            // must be Alignment
            a = (Alignment) parseNexus(fileName, true);
        }
        return new Value<>(a, this);
    }

    // if value is null, ignoring charset return single partition
    private AbstractAlignment parseNexus(Value<String> fileName, boolean ignoreCharset) {
        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);

        return parser.getLPhyAlignment(ignoreCharset);
    }
}
