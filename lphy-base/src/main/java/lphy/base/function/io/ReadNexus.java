package lphy.base.function.io;

import jebl.evolution.io.ImportException;
import lphy.base.evolution.alignment.MetaDataAlignment;
import lphy.base.function.alignment.MetaDataOptions;
import lphy.base.parser.NexusParser;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.IOFunction;
import lphy.core.model.annotation.ParameterInfo;

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
@IOFunction(
        role = IOFunction.Role.dataInput,
        extensions = { ".nex", ".nexus",".nxs"},
        fileArgument = ReaderConst.FILE
)
public class ReadNexus extends DeterministicFunction<MetaDataAlignment> {

    Value<String> filePath;
    Value<Map<String, String>> options;

    public ReadNexus(@ParameterInfo(name = ReaderConst.FILE, narrativeName = "file name", description = "the name of Nexus file including path.") Value<String> filePath,
                     @ParameterInfo(name = ReaderConst.OPTIONS, description = "the map containing optional arguments and their values for reuse.",
                             optional=true) Value<Map<String, String>> options ) {
        this.filePath = filePath;
        this.options = options;
    }

    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(ReaderConst.FILE, filePath);
        if (options != null) map.put(ReaderConst.OPTIONS, options);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(ReaderConst.FILE)) filePath = value;
        else if (paramName.equals(ReaderConst.OPTIONS)) options = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }


    @GeneratorInfo(name="readNexus", verbClause = "is read from", narrativeName = "Nexus file",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            examples = {"simpleSerialCoalescentNex.lphy","twoPartitionCoalescentNex.lphy"},
            description = "A function that parses an alignment from a Nexus file.")
    public Value<MetaDataAlignment> apply() {

        Path nexPath = UserDir.getUserPath(filePath.value());

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

            nexusData.setAgesParsedFromTaxaName(ageRegxStr, ageDirectionStr);
        }

        // set species to Taxon
        if (spRegxStr != null)
            Objects.requireNonNull(nexusData).setSpeciesParsedFromTaxaName(spRegxStr);

        return new Value<>(null, nexusData, this);

    }

}
