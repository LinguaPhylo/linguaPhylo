package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.io.ExtNexusImporter;
import lphy.evolution.io.NexusOptions;
import lphy.evolution.io.NexusParser;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringArrayValue;
import lphy.graphicalModel.types.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * data = nexus(file="primate.nex");
 * or coding = nexus(file="primate.nex", charset="coding");
 * This involves partitions.
 * @see ReadTaxa
 */
public class ReadNexus extends DeterministicFunction {

    private final String fileParamName;
    private final String charsetParamName;
    private final String optionsParamName;
    private final String taxaParamName;
    private final String ignoreCharsetParamName;

    // 1 458-659 3-629\3
    private final String CHARSET_REGX = "^([0-9]+)$|^([0-9]+)\\-([0-9]+)(\\\\[0-9]+)*$";

    private NexusOptions nexusOptions;

    public ReadNexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                     @ParameterInfo(name = "charset", description = "the charset(s) defined by Nexus syntax, such as " +
                         "charset=[\"1\", \"458-659\"] or charset=[\"3-629\\3\", \"4-629\\3\", \"5-629\\3\"] " +
                         "or charset=\"1-629\\3\". If it doesn't match charset's definition, then check if the string matches " +
                         "a defined charset (name) in the nexus file. Otherwise it is an error.", optional=true)
                         Value charsets,
                     @ParameterInfo(name = "options", description = "the map containing optional arguments and their values for reuse.",
                         optional=true) Value<Map<String, String>> options,
                     @ParameterInfo(name = "taxa", description = "the taxa object, which cannot be used with 'options' together.",
                         optional=true) Value<Taxa> taxa,
                     @ParameterInfo(name = "parentAlignmnet", description = "Default to false. If true, " +
                         "then ignore the charsets in the nexus file, and return the full alignment.",
                         optional=true) Value<Boolean> parentAlignmnet ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        charsetParamName = getParamName(1);
        optionsParamName = getParamName(2);
        taxaParamName = getParamName(3);
        ignoreCharsetParamName = getParamName(4);

        setParam(fileParamName, fileName);

        if (charsets != null) setParam(charsetParamName, charsets);

        if (taxa != null && options != null)
            throw new IllegalArgumentException("Argument 'taxa' and 'options' cannot use together");
        if (options != null) setParam(optionsParamName, options);
        if (taxa != null) setParam(taxaParamName, taxa);

        if (parentAlignmnet != null) setParam(ignoreCharsetParamName, taxa);
    }

    public void setParam(String paramName, Value value) {
        validateStringArray(value);
        paramMap.put(paramName, value);
    }

    // "[3-629\3, 4-629\3, 5-629\3]" is invalid
    protected void validateStringArray(Value value) {
        if ( value instanceof StringValue ) {
            String str = value.value().toString();
            if (str.contains("[")) {
                throw new IllegalArgumentException("Invalid string array is detected, " +
                        "the valid format is charset=[\"3-629\\3\", \"4-629\\3\", \"5-629\\3\"], " +
                        "but find : \n" + str);
            }
        }
    }

    @GeneratorInfo(name="readNexus",description = "A function that parses an alignment from a Nexus file.")
    public Value apply() {

        Value<String> fileName = getParams().get(fileParamName);

        Value<Taxa> taxaVal = getParams().get(taxaParamName);
        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);

        Value charset = getParams().get(charsetParamName);
        Value<Boolean> parentAlignmnet = getParams().get(ignoreCharsetParamName);
        // Default (null) to false
        boolean ignoreCharset = parentAlignmnet != null && parentAlignmnet.value();

        nexusOptions = NexusOptions.getInstance();
        Alignment cachedAlignment = null;
        if (taxaVal != null) {
            Taxa taxa = taxaVal.value(); // TODO same to nexusOptions.getAlignment(fileName.value())
            // Alignment is cached here
            if (taxa instanceof Alignment)
                cachedAlignment = (Alignment) taxa;
            else {
                // in case taxa is not from readTaxa()
                cachedAlignment = nexusOptions.getAlignment(fileName.value(), ignoreCharset);
            }
        } else if (optionsVal != null) {
            Map<String, String> options = optionsVal.value();
            cachedAlignment = nexusOptions.getAlignment(fileName.value(), options, ignoreCharset);
        } else {
            // no taxa or options
            cachedAlignment = nexusOptions.getAlignment(fileName.value(), ignoreCharset);
        }

        // if Nexus file has no charsets
        if (charset == null) return new Value(cachedAlignment, this);

        //*** charsets or part names ***//

        // if Nexus file has charsets
        if (charset instanceof StringArrayValue) {
            String[] strs = ((StringArrayValue) charset).value();
            Alignment[] alignments = new Alignment[strs.length];

            for (int i = 0; i < strs.length; i++) {
                alignments[i] = getPartAlignment(Objects.requireNonNull(fileName).value(), cachedAlignment, strs[i]);
            }

            return new Value(alignments, this);

        } else {
            String str = ((StringValue) charset).value();

            Alignment partAlignment = getPartAlignment(Objects.requireNonNull(fileName).value(), cachedAlignment, str);

            return new Value(partAlignment, this);
        }



//        List<List<CharSetBlock>> charsetsList = new ArrayList<>();
//        if (charset != null)
//            charsetsList = parseCharsets(parser, charset);
//
//        //*** alignments ***//
//
//        Alignment a;
//        if (part != null) {
//            // must be CharSetAlignment
//            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
//                    !(cachedAlignment instanceof CharSetAlignment) ) {
//                cachedAlignment = parser.getLPhyAlignment(false, ageDirectionStr, dateRegxStr);
//                currentFileName = fileName.value();
//            }
//            a = ((CharSetAlignment)  cachedAlignment).getPartAlignment(part.value());
//
//        } else if (charsetsList.size() == 1) {
//            // cache SimpleAlignment as parent alignment, and apply charset
//            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
//                    !(cachedAlignment instanceof SimpleAlignment) ) {
//                // ignore charset in Nexus file
//                cachedAlignment = parser.getLPhyAlignment(true, ageDirectionStr, dateRegxStr);
//                currentFileName = fileName.value();
//            }
//            a = CharSetAlignment.getPartition(charsetsList.get(0), (SimpleAlignment) cachedAlignment);
//
//        } else if (charsetsList.size() > 1) {
//
//            if (cachedAlignment == null || !currentFileName.equalsIgnoreCase(fileName.value()) ||
//                    !(cachedAlignment instanceof CharSetAlignment) ) {
//                // ignore charset in Nexus file
//
//                Map<String, List<CharSetBlock>> charsetMap = new TreeMap<>();
//                for (int i = 0; i < charsetsList.size(); i++)
//                    charsetMap.put(Integer.toString(i+1), charsetsList.get(i));
//                SimpleAlignment parent = (SimpleAlignment) parser.getLPhyAlignment(true, ageDirectionStr, dateRegxStr);
//                cachedAlignment = new CharSetAlignment(charsetMap, parent);
//
//                currentFileName = fileName.value();
//            }
//
//            Alignment[] alignments = ((CharSetAlignment) cachedAlignment).getPartAlignments();
//            return new Value(alignments, this);
//
//        } else {
//            // must be SimpleAlignment
//            a = parser.getLPhyAlignment(true, ageDirectionStr, dateRegxStr);
//        }
//
//        return new Value(a, this);
    }

    protected Alignment getPartAlignment(String fileName, Alignment cachedAlignment, String str) {

        Alignment partAlignment = null;
        if (Pattern.matches(CHARSET_REGX, str)) {
           // give charset, ignore charsets in file
            cachedAlignment = nexusOptions.getAlignment(fileName, true);
            SimpleAlignment parent = (SimpleAlignment) cachedAlignment;

            List<CharSetBlock> charSetBlocks = ExtNexusImporter.getCharSetBlocks(str);
            partAlignment = CharSetAlignment.getPartition(charSetBlocks, parent);

        } else if (cachedAlignment instanceof CharSetAlignment) {
            // str is name, check if it is in file
            partAlignment = ((CharSetAlignment) cachedAlignment).getPartAlignmentNoValidation(str);

        }
        if (partAlignment == null)
            throw new IllegalArgumentException("The string is neither charset or name in in the nexus file ! " + str);
        return partAlignment;
    }


    private List<List<CharSetBlock>> parseCharsets(final NexusParser parser, Value value) {
        List<List<CharSetBlock>> charsetsList = new ArrayList<>();

        if (value instanceof StringArrayValue) {
            String[] strs = ((StringArrayValue) value).value();
            for (int i = 0; i < strs.length; i++) {
                List<CharSetBlock> charSetBlocks = ExtNexusImporter.getCharSetBlocks(strs[i]);
                charsetsList.add(charSetBlocks);
            }

        } else { // String
            List<CharSetBlock> charSetBlocks = ExtNexusImporter.getCharSetBlocks(value.toString());
            charsetsList.add(charSetBlocks);
        }

        return charsetsList;
    }

}
