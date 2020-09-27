package lphy.core.functions;

import lphy.evolution.alignment.AbstractAlignment;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.io.NexusParser;
import lphy.evolution.io.TaxaData;
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
    private final String ageDirectionParamName;
    private final String regxParamName;

    // cache the partitions from one Nexus file
    private static Alignment cachedAlignment = null;
    private static String currentFileName = "";

    // TODO try overload ?
    public Nexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                 @ParameterInfo(name = "part", description = "the name of selected partition in Nexus, " +
                         "if none then return the full alignment.", optional=true) Value<String> part,
                 @ParameterInfo(name = "charset", description = "the charset defined by Nexus syntax, " +
                         "but cannot use with argument 'part' together.", optional=true) Value<String> charset,
                 @ParameterInfo(name = "ageDirection", description = "age direction which is either forward (dates) or backward (ages).",
                         optional=true) Value<String> ageDirection,
                 @ParameterInfo(name = "ageRegex", description = "Java regular expression to extract dates.",
                         optional=true) Value<String> regx ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        partParamName = getParamName(1);
        charsetParamName = getParamName(2);
        ageDirectionParamName = getParamName(3);
        regxParamName = getParamName(4);

        setParam(fileParamName, fileName);
        if (part != null) setParam(partParamName, part);
        if (charset != null) setParam(charsetParamName, charset);
        if (ageDirection != null) setParam(ageDirectionParamName, ageDirection);
        if (regx != null) setParam(regxParamName, regx);

        if (part != null && charset != null)
            throw new IllegalArgumentException("Argument 'part' and 'charset' cannot use together");
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String> part = getParams().get(partParamName);
        Value<String> charset = getParams().get(charsetParamName);
        Value<String> ageDirection = getParams().get(ageDirectionParamName);
        Value<String> regx = getParams().get(regxParamName);

        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);
        String tipCalibType = ageDirection == null ? null : ageDirection.value();

        //TODO implement Alignment ?

        Alignment a;
        if (part != null) {
            // must be CharSetAlignment
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof CharSetAlignment) ) {
                cachedAlignment = parser.getLPhyAlignment(false, tipCalibType);
                currentFileName = fileName.value();
            }
            a = ((CharSetAlignment)  cachedAlignment).getPartAlignment(part.value());

        } else if (charset != null) {
            // cache SimpleAlignment as parent alignment, and apply charset
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof SimpleAlignment) ) {
                // ignore charset in Nexus file
                cachedAlignment = parser.getLPhyAlignment(true, tipCalibType);
                currentFileName = fileName.value();
            }
            List<CharSetBlock> charSetBlocks = parser.getImporter().getCharSetBlocks(charset.value());
            a = CharSetAlignment.getPartition(charSetBlocks, (SimpleAlignment) cachedAlignment);

        } else {
            // must be SimpleAlignment
            a = parser.getLPhyAlignment(true, tipCalibType);
        }

        if (regx != null) {
            TaxaData taxaData = new TaxaData(a.getTaxaNames(), regx.value(), ageDirection.value());
            // TODO mv to constructor
            ((AbstractAlignment)  a).setTaxonMap(taxaData.getTaxonMap());
        }

        return new Value<>(a, this);
    }

}
