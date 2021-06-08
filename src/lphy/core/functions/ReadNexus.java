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

/**
 * D = readNexus(file="primate.nex");
 * D.charset("coding");
 * This does not involve partitioning.
 * @see MetaDataAlignment
 */
public class ReadNexus extends DeterministicFunction<Alignment> {

    private final String fileParamName = "file";
    private final String optionsParamName = "options";

    public ReadNexus(@ParameterInfo(name = fileParamName, narrativeName = "file name", description = "the name of Nexus file.") Value<String> fileName,
                     @ParameterInfo(name = optionsParamName, description = "the map containing optional arguments and their values for reuse.",
                         optional=true) Value<Map<String, String>> options ) {


        if (fileName == null) throw new IllegalArgumentException("The file name can't be null!");
        setParam(fileParamName, fileName);

        if (options != null) setParam(optionsParamName, options);
    }


    @GeneratorInfo(name="readNexus",
            verbClause = "is read from",
            narrativeName = "Nexus file",
            description = "A function that parses an alignment from a Nexus file.")
    public Value<Alignment> apply() {

        String fileName = ((Value<String>) getParams().get(fileParamName)).value();

        Value<Map<String, String>> optionsVal = getParams().get(optionsParamName);
        String ageDirectionStr = MetaDataOptions.getAgeDirectionStr(optionsVal);
        String ageRegxStr = MetaDataOptions.getAgeRegxStr(optionsVal);
        String spRegxStr = MetaDataOptions.getSpecieseRegex(optionsVal);

        Path nexPath = IOUtils.getPath(fileName);

        //*** parsing ***//
        NexusParser nexusParser = new NexusParser(nexPath.toString());
        MetaDataAlignment nexusData = null;
            try {
                nexusData = nexusParser.importNexus(ageDirectionStr);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }
        // set age to Taxon
        if (ageRegxStr != null) {
            if (! Objects.requireNonNull(nexusData).isUltrametric())
                LoggerUtils.log.severe("Taxa ages were imported from the nexus file ! " +
                                "It would be problematic to overwrite ages in taxa !");

            nexusData.setAgesFromTaxaName(ageRegxStr, ageDirectionStr);
        }

        // set species to Taxon
        if (spRegxStr != null)
            Objects.requireNonNull(nexusData).setSpeciesFromTaxaName(spRegxStr);

        return new Value<>(null, nexusData, this);

    }

}
