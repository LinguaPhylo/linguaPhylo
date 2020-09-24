package lphy.core.functions;

import lphy.evolution.alignment.AbstractAlignment;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.io.NexusParser;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * data = nexus(file="primate.nex");
 * or coding = nexus(file="primate.nex", charset="coding");
 */
public class Nexus extends DeterministicFunction<Alignment> {

    private final String fileParamName;
    private final String partParamName;
    private final String charsetParamName;
    private final String ageTypeParamName;

    // cache the partitions from one Nexus file
    private static AbstractAlignment cachedAlignment = null;
    private static String currentFileName = "";

    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "part", description = "the name of selected partition in Nexus, " +
                         "if none then return the full alignment.", optional=true) Value<String> part,
                 @ParameterInfo(name = "charset", description = "the charset defined by Nexus syntax, " +
                         "but cannot use with argument 'part' together.", optional=true) Value<String> charset,
                 @ParameterInfo(name = "tipcalibrations", description = "age type (i.e. forward, backward, age).",
                         optional=true) Value<String> ageType) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        partParamName = getParamName(1);
        charsetParamName = getParamName(2);
        ageTypeParamName = getParamName(3);
        setParam(fileParamName, fileName);
        if (part != null) setParam(partParamName, part);
        if (charset != null) setParam(charsetParamName, charset);
        if (ageType != null) setParam(ageTypeParamName, ageType);

        if (part != null && charset != null)
            throw new IllegalArgumentException("Argument 'part' and 'charset' cannnot use together");
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String> part = getParams().get(partParamName);
        Value<String> charset = getParams().get(charsetParamName);
        Value<String> ageType = getParams().get(ageTypeParamName);

        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);
        String type = ageType == null ? null : ageType.value();

        Alignment a;
        if (part != null) {
            // must be CharSetAlignment
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof CharSetAlignment) ) {
                cachedAlignment = parser.getLPhyAlignment(false, type);
                currentFileName = fileName.value();
            }
            a = ((CharSetAlignment)  cachedAlignment).getPartAlignment(part.value());

        } else if (charset != null) {
            // cache SimpleAlignment as parent alignment, and apply charset
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof SimpleAlignment) ) {
                // ignore charset in Nexus file
                cachedAlignment = parser.getLPhyAlignment(true, type);
                currentFileName = fileName.value();
            }
            List<CharSetBlock> charSetBlocks = parser.getImporter().getCharSetBlocks(charset.value());
            a = CharSetAlignment.getPartition(charSetBlocks, (SimpleAlignment) cachedAlignment);

        } else {
            // must be SimpleAlignment
            a = parser.getLPhyAlignment(true, type);
        }
        return new Value<>(a, this);
    }

}
