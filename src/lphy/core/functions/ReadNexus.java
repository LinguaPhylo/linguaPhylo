package lphy.core.functions;

import jebl.evolution.io.ImportException;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.io.NexusData;
import lphy.evolution.io.NexusOptions;
import lphy.evolution.io.NexusParser;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringArrayValue;
import lphy.graphicalModel.types.StringValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * data = nexus(file="primate.nex");
 * or coding = nexus(file="primate.nex", charset="coding");
 * This involves partitions.
 * @see ReadTaxa
 */
public class ReadNexus extends DeterministicFunction<NexusData> {

    private final String fileParamName;
    private final String charsetParamName;
    private final String optionsParamName;
//    private final String taxaParamName;
    private final String ignoreCharsetParamName;

    public ReadNexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                     @ParameterInfo(name = "charset", description = "the charset(s) defined by Nexus syntax, such as " +
                         "charset=[\"1\", \"458-659\"] or charset=[\"3-629\\3\", \"4-629\\3\", \"5-629\\3\"] " +
                         "or charset=\"1-629\\3\". If it doesn't match charset's definition, then check if the string matches " +
                         "a defined charset (name) in the nexus file. Otherwise it is an error.", optional=true)
                         Value charsets,
                     @ParameterInfo(name = "options", description = "the map containing optional arguments and their values for reuse.",
                         optional=true) Value<Map<String, String>> options,
                     @ParameterInfo(name = "parentAlignmnet", description = "Default to false. If true, " +
                         "then ignore the charsets in the nexus file, and return the full alignment.",
                         optional=true) Value<Boolean> parentAlignmnet ) {
//                     @ParameterInfo(name = "taxa", description = "the taxa object, which cannot be used with 'options' together.",
//                         optional=true) Value<Taxa> taxa ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        charsetParamName = getParamName(1);
        optionsParamName = getParamName(2);
        ignoreCharsetParamName = getParamName(3);
//        taxaParamName = getParamName(4);

        setParam(fileParamName, fileName);

        if (charsets != null) setParam(charsetParamName, charsets);

//        if (taxa != null && options != null)
//            throw new IllegalArgumentException("Argument 'taxa' and 'options' cannot use together");
        if (options != null) setParam(optionsParamName, options);
//        if (taxa != null) setParam(taxaParamName, taxa);

        if (parentAlignmnet != null) setParam(ignoreCharsetParamName, parentAlignmnet);
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
    public Value<NexusData> apply() {

        String fileName = ((StringValue) getParams().get(fileParamName)).value();

//        Value<Taxa> taxaVal = getParams().get(taxaParamName);
        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = NexusOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = NexusOptions.getAgeRegxStr(optionsVal);

//        Value charset = getParams().get(charsetParamName);
        Value<Boolean> parentAlignmnet = getParams().get(ignoreCharsetParamName);
        // Default (null) to false
        boolean ignoreCharset = parentAlignmnet != null && parentAlignmnet.value();

        //*** parsing ***//
        NexusParser nexusParser = new NexusParser(fileName);
        NexusData nexusData = null;
        try {
            nexusData = nexusParser.importNexus();
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }
        if (nexusData == null)
            throw new RuntimeException("Fail to parse file ! ");

        //*** ages ***//

        if (ageRegxStr != null) { // ages from taxon names, so ignore TIPCALIBRATION in Nexus
            // extract dates from names
            nexusData.setAgeMapFromTaxa(ageRegxStr);
        }
        if (nexusData.hasAges()) {
            // ageStringMap is filled in from either TIPCALIBRATION or taxon names
            nexusData.assignAges(ageDirectionStr);  // forward backward
        }

        //*** charset ***//
        if (nexusData.hasCharsets()) {
            final Map<String, List<CharSetBlock>> charsetMap = nexusData.getCharsetMap();
            if (ignoreCharset)
                System.out.println("Ignore charsets in the nexus file, charsetMap = " + charsetMap);
            else {
                SimpleAlignment parent = nexusData.getSimpleAlignment();
                nexusData.setAlignment(new CharSetAlignment(charsetMap, parent)); // this imports all charsets
            }
        }

        return new Value<>(nexusData, this);

//        Alignment cachedAlignment = nexusData.getAlignment();

//        if (taxaVal != null) {
//            Taxa taxa = taxaVal.value();
//            // Alignment is cached here
//            if (taxa instanceof Alignment)
//                cachedAlignment = (Alignment) taxa;
//            else {
//                // in case taxa is not from readTaxa()
//                cachedAlignment = nexusOptions.getAlignment(fileName.value(), ignoreCharset);
//            }
//        } else


//        if (optionsVal != null) {
//            Map<String, String> options = optionsVal.value();
//            cachedAlignment = nexusOptions.getAlignment(fileName.value(), options, ignoreCharset);
//        } else {
//            // no taxa or options
//            cachedAlignment = nexusOptions.getAlignment(fileName.value(), ignoreCharset);
//        }
//
//        // if Nexus file has no charsets
//        if (charset == null) return new Value(cachedAlignment, this);
//
//        //*** charsets or part names ***//
//
//        // if Nexus file has charsets
//        if (charset instanceof StringArrayValue) {
//            String[] strs = ((StringArrayValue) charset).value();
//            Alignment[] alignments = new Alignment[strs.length];
//
//            for (int i = 0; i < strs.length; i++) {
//                alignments[i] = getPartAlignment(Objects.requireNonNull(fileName).value(), cachedAlignment, strs[i]);
//            }
//
//            return new Value(alignments, this);
//
//        } else {
//            String str = ((StringValue) charset).value();
//
//            Alignment partAlignment = getPartAlignment(Objects.requireNonNull(fileName).value(), cachedAlignment, str);
//
//            return new Value(partAlignment, this);
//        }




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






//    protected Alignment getPartAlignment(String fileName, Alignment cachedAlignment, String str) {
//
//        Alignment partAlignment = null;
//        if (CharSetBlock.Utils.isValid(str)) {
//           // give charset, ignore charsets in file
//            cachedAlignment = nexusData.getAlignment();
//            SimpleAlignment parent = (SimpleAlignment) cachedAlignment;
//
//            List<CharSetBlock> charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
//            partAlignment = CharSetAlignment.getPartition(charSetBlocks, parent);
//
//        } else if (cachedAlignment instanceof CharSetAlignment) {
//            // str is name, check if it is in file
//            partAlignment = ((CharSetAlignment) cachedAlignment).getPartAlignmentNoValidation(str);
//
//        }
//        if (partAlignment == null)
//            throw new IllegalArgumentException("The string is neither charset or name in in the nexus file ! " + str);
//        return partAlignment;
//    }


    private List<List<CharSetBlock>> parseCharsets(Value value) {
        List<List<CharSetBlock>> charsetsList = new ArrayList<>();

        if (value instanceof StringArrayValue) {
            String[] strs = ((StringArrayValue) value).value();
            for (int i = 0; i < strs.length; i++) {
                List<CharSetBlock> charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(strs[i]);
                charsetsList.add(charSetBlocks);
            }

        } else { // String
            List<CharSetBlock> charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(value.toString());
            charsetsList.add(charSetBlocks);
        }

        return charsetsList;
    }

}
