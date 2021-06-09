package lphy.core.functions;

import jebl.evolution.io.ImportException;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.io.MetaDataAlignment;
import lphy.evolution.io.MetaDataOptions;
import lphy.evolution.io.NexusParser;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.utils.IOUtils;
import lphy.utils.LoggerUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * D = readNexus(file="primate.nex");
 * D.charset("coding");
 * This does not involve partitioning.
 * @see MetaDataAlignment
 */
public class ReadNexus extends DeterministicFunction<Alignment> {

    private final String fileParamName = "file";
    private final String optionsParamName = "options";

    Value<String> fileName;
    Value<Map<String, String>> options;

    public ReadNexus(@ParameterInfo(name = fileParamName, narrativeName = "file name", description = "the name of Nexus file.") Value<String> fileName,
                     @ParameterInfo(name = optionsParamName, description = "the map containing optional arguments and their values for reuse.",
                             optional=true) Value<Map<String, String>> options ) {
        this.fileName = fileName;
        this.options = options;
    }

    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(fileParamName, fileName);
        if (options != null) map.put(optionsParamName, options);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(fileParamName)) fileName = value;
        else if (paramName.equals(optionsParamName)) options = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }


    @GeneratorInfo(name="readNexus",
            verbClause = "is read from",
            narrativeName = "Nexus file",
            description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        Path nexPath = IOUtils.getPath(fileName.value());

        //*** parsing ***//
        NexusParser nexusParser = new NexusParser(nexPath.toString());

        // "options" is optional, those getters can handle null
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(options);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(options);
        String spRegxStr = MetaDataOptions.getSpecieseRegex(options);

        MetaDataAlignment nexusData = null;
        try {
            // if ageDirectionStr = null, then assume forward
            nexusData = nexusParser.importNexus(ageDirectionStr);
        } catch (IOException | ImportException e) {
            LoggerUtils.logStackTrace(e);
        }
        // set age to Taxon
        if (ageRegxStr != null) {
            if (! Objects.requireNonNull(nexusData).isUltrametric())
                LoggerUtils.log.severe("Taxa ages had been imported from the nexus file ! " +
                        "It would be problematic to overwrite taxa ages from the command line !");

            nexusData.setAgesFromTaxaName(ageRegxStr, ageDirectionStr);
        }

        // set species to Taxon
        if (spRegxStr != null)
            Objects.requireNonNull(nexusData).setSpeciesFromTaxaName(spRegxStr);

        return new Value<>(null, nexusData, this);

    }

}
