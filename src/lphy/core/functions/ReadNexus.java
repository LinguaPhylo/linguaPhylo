package lphy.core.functions;

import jebl.evolution.io.ImportException;
import lphy.evolution.alignment.AlignmentUtils;
import lphy.evolution.io.MetaData;
import lphy.evolution.io.MetaDataOptions;
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
 * @see MetaData
 * @see AlignmentUtils
 */
public class ReadNexus extends DeterministicFunction<MetaData> {

    private final String fileParamName;
    private final String optionsParamName;

    public ReadNexus(@ParameterInfo(name = "file", description = "the name of Nexus file.") Value<String> fileName,
                     @ParameterInfo(name = "options", description = "the map containing optional arguments and their values for reuse.",
                         optional=true) Value<Map<String, String>> options ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");

        fileParamName = getParamName(0);
        optionsParamName = getParamName(1);

        setParam(fileParamName, fileName);

        if (options != null) setParam(optionsParamName, options);
    }


    @GeneratorInfo(name="readNexus",description = "A function that parses an alignment from a Nexus file.")
    public Value<MetaData> apply() {

        String fileName = ((StringValue) getParams().get(fileParamName)).value();

        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(optionsVal);

        //*** parsing ***//
        NexusParser nexusParser = new NexusParser(fileName);
        MetaData nexusData = null;
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

}
