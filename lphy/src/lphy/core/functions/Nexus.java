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
import lphy.graphicalModel.types.StringArrayValue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                 // charset=["1-629\3", "2-629\3", "3-629\3"] or charset="1-629\3"
                 @ParameterInfo(name = "charset", description = "the charset(s) defined by Nexus syntax, " +
                         "but cannot use with argument 'part' together.", optional=true) Value<?> charsets,
                 @ParameterInfo(name = "ageDirection", description = "age direction which is either forward (dates) or backward (ages).",
                         optional=true) Value<String> ageDirection,
                 @ParameterInfo(name = "ageRegex", description = "Java regular expression to extract dates.",
                         optional=true) Value<String> regx) {
//                 @ParameterInfo(name = "options", description = "the map containing optional arguments and their values for reuse.",
//                         optional=true) Value<Map> options) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        partParamName = getParamName(1);
        charsetParamName = getParamName(2);
        ageDirectionParamName = getParamName(3);
        regxParamName = getParamName(4);

        setParam(fileParamName, fileName);

        // options overwrites other optional inputs
//        if (options != null) {
//            // convert from lphy map to java map
//            java.util.Map<String, String> optMap = (java.util.Map<String, String>) options.value().apply().value();
//
//            for (java.util.Map.Entry<String, String> entry : optMap.entrySet()) {
//                setParam(entry.getKey(), new Value(null, entry.getValue()));
//                //
//                if ( ! (entry.getKey().equals(ageDirectionParamName) || entry.getKey().equals(regxParamName) ) )
//                        throw new IllegalArgumentException("Invalid arguments in options ! " + entry.toString());
//            }
//        }

        if (part != null) setParam(partParamName, part);
        if (charsets != null) setParam(charsetParamName, charsets);
        if (ageDirection != null) setParam(ageDirectionParamName, ageDirection);
        if (regx != null) setParam(regxParamName, regx);

        if (part != null && charsets != null)
            throw new IllegalArgumentException("Argument 'part' and 'charset' cannot use together");
    }

    @GeneratorInfo(name="nexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        Value<String> fileName = getParams().get(fileParamName);
        Value<String> part = getParams().get(partParamName);
        Value<String> ageDirection = getParams().get(ageDirectionParamName);
        Value<String> regx = getParams().get(regxParamName);

        final Path nexFile = Paths.get(fileName.value());
        NexusParser parser = new NexusParser(nexFile);
        String ageDirectionStr = ageDirection == null ? null : ageDirection.value();

        List<List<CharSetBlock>> charsetsList = new ArrayList<>();
        Value<?> charset = getParams().get(charsetParamName);
        if (charset != null)
            charsetsList = parseCharsets(parser, charset);


        //TODO implement Alignment ?

        Alignment a;
        if (part != null) {
            // must be CharSetAlignment
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof CharSetAlignment) ) {
                cachedAlignment = parser.getLPhyAlignment(false, ageDirectionStr);
                currentFileName = fileName.value();
            }
            a = ((CharSetAlignment)  cachedAlignment).getPartAlignment(part.value());

        } else if (charsetsList.size() == 1) {
            // cache SimpleAlignment as parent alignment, and apply charset
            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof SimpleAlignment) ) {
                // ignore charset in Nexus file
                cachedAlignment = parser.getLPhyAlignment(true, ageDirectionStr);
                currentFileName = fileName.value();
            }
            a = CharSetAlignment.getPartition(charsetsList.get(0), (SimpleAlignment) cachedAlignment);

        } else if (charsetsList.size() > 1) {

            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
                    !(cachedAlignment instanceof CharSetAlignment) ) {
                // ignore charset in Nexus file

                Map<String, List<CharSetBlock>> charsetMap = new TreeMap<>();
                for (int i = 0; i < charsetsList.size(); i++)
                    charsetMap.put(Integer.toString(i+1), charsetsList.get(i));
                SimpleAlignment parent = (SimpleAlignment) parser.getLPhyAlignment(true, ageDirectionStr);
                cachedAlignment = new CharSetAlignment(charsetMap, parent);

                currentFileName = fileName.value();
            }

            a = cachedAlignment;

        } else {
            // must be SimpleAlignment
            a = parser.getLPhyAlignment(true, ageDirectionStr);
        }

        if (regx != null) {
            TaxaData taxaData = new TaxaData(a.getTaxaNames(), regx.value(), ageDirection.value());
            // TODO mv to constructor
            ((AbstractAlignment)  a).setTaxonMap(taxaData.getTaxonMap());
        }

        return new Value<>(a, this);
    }

    private List<List<CharSetBlock>> parseCharsets(final NexusParser parser, Object value) {
        List<List<CharSetBlock>> charsetsList = new ArrayList<>();

        if (value instanceof StringArrayValue) {
            String[] strs = ((StringArrayValue) value).value();
            for (int i = 0; i < strs.length; i++) {
                List<CharSetBlock> charSetBlocks = parser.getImporter().getCharSetBlocks(strs[i]);
                charsetsList.add(charSetBlocks);
            }

        } else { // String
            List<CharSetBlock> charSetBlocks = parser.getImporter().getCharSetBlocks(value.toString());
            charsetsList.add(charSetBlocks);
        }

        return charsetsList;
    }

}
