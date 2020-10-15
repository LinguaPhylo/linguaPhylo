package lphy.core.functions;

import jebl.evolution.io.ImportException;
import lphy.evolution.alignment.AlignmentUtils;
import lphy.evolution.io.NexusData;
import lphy.evolution.io.NexusOptions;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringValue;

import java.io.IOException;
import java.util.Map;

/**
 * D = readNexus(file="primate.nex");
 * D.charset("coding");
 * This does not involve partitioning.
 * @see NexusData
 * @see AlignmentUtils
 */
public class ReadNexus extends DeterministicFunction<NexusData> {

    private final String fileParamName;
//    private final String charsetParamName;
    private final String optionsParamName;
//    private final String taxaParamName;
//    private final String ignoreCharsetParamName;

    public ReadNexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
//                     @ParameterInfo(name = "charset", description = "the charset(s) defined by Nexus syntax, such as " +
//                         "charset=[\"1\", \"458-659\"] or charset=[\"3-629\\3\", \"4-629\\3\", \"5-629\\3\"] " +
//                         "or charset=\"1-629\\3\". If it doesn't match charset's definition, then check if the string matches " +
//                         "a defined charset (name) in the nexus file. Otherwise it is an error.", optional=true)
//                         Value charsets,
                     @ParameterInfo(name = "options", description = "the map containing optional arguments and their values for reuse.",
                         optional=true) Value<Map<String, String>> options ) {
//                     @ParameterInfo(name = "taxa", description = "the taxa object, which cannot be used with 'options' together.",
//                         optional=true) Value<Taxa> taxa ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
//        charsetParamName = getParamName(1);
        optionsParamName = getParamName(1);
//        ignoreCharsetParamName = getParamName(3);
//        taxaParamName = getParamName(4);

        setParam(fileParamName, fileName);

//        if (charsets != null) setParam(charsetParamName, charsets);

//        if (taxa != null && options != null)
//            throw new IllegalArgumentException("Argument 'taxa' and 'options' cannot use together");
        if (options != null) setParam(optionsParamName, options);
//        if (taxa != null) setParam(taxaParamName, taxa);

//        if (parentAlignmnet != null) setParam(ignoreCharsetParamName, parentAlignmnet);
    }


    @GeneratorInfo(name="readNexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<NexusData> apply() {

        String fileName = ((StringValue) getParams().get(fileParamName)).value();

//        Value<Taxa> taxaVal = getParams().get(taxaParamName);
        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = NexusOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = NexusOptions.getAgeRegxStr(optionsVal);

//        Value charset = getParams().get(charsetParamName);
//        Value<Boolean> parentAlignmnet = getParams().get(ignoreCharsetParamName);
        // Default (null) to false
//        boolean ignoreCharset = parentAlignmnet != null && parentAlignmnet.value();

        //*** parsing ***//
        NexusParser nexusParser = new NexusParser(fileName);
        NexusData nexusData = null;
            try {
                nexusData = nexusParser.importNexus(ageDirectionStr);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }
        if (ageRegxStr != null) {
            nexusData.setAgesFromTaxaName(ageRegxStr, ageDirectionStr);
        }
        return new Value<>(null, nexusData, this);

    }


//    public void setParam(String paramName, Value value) {
//        validateStringArray(value);
//        paramMap.put(paramName, value);
//    }
//
//    // "[3-629\3, 4-629\3, 5-629\3]" is invalid
//    protected void validateStringArray(Value value) {
//        if ( value instanceof StringValue ) {
//            String str = value.value().toString();
//            if (str.contains("[")) {
//                throw new IllegalArgumentException("Invalid string array is detected, " +
//                        "the valid format is charset=[\"3-629\\3\", \"4-629\\3\", \"5-629\\3\"], " +
//                        "but find : \n" + str);
//            }
//        }
//    }

}
