package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;
import lphy.core.spi.LoaderManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Log the values of all named random variables into a or multiple file,
 * which is determined by the mode.
 */
public class  ValueFileLoggerListener implements SimulatorListener {

    /**
     * For ValuePerFile, the logging is processed in {@link #replicate(int, List)} in runtime,
     * so no values are required to store.
     */

    /**
     * For ValuePerLine, the key represents the value id and is used for the file name.
     * Each list of formatted value in string with the same value id is logged into a separate file,
     * with each formatted value on its own line.
     */
    Map<String, String[]> metadataById;
    Map<String, List<String>> linesById;

    /**
     * For ValuePerCell, the column names are constructed in valuesByRepColNamesBuilder,
     * all values from each replicate of the simulation are constructed into valuesByRepBuilder,
     * with each replicate occupying one line where values are seperated by tab as default.
     * These two StringBuilder will log into one file.
     */
//TODO    use RandomNumberLoggerListener
    StringBuilder valuesByRepColNamesBuilder;
    StringBuilder valuesByRepBuilder;

    // numReplicates, filePrefix
    FileConfig fileConfig;

    private static final ValueFormatResolver valueFormatResolver = LoaderManager.valueFormatResolver;

    public void setOutputDir(String dir) {
        OutputSystem.setOutputDirectory(dir);
    }

    public String getOutputDir() {
        return OutputSystem.getOrCreateOutputDirectory().getAbsolutePath();
    }

    /**
     * @param configs Options: 1) only contain {@link FileConfig};
     *                2) the 1st element is Integer numReplicates,
     *                and the 2nd is String filePrefix;
     *                3) the 1st element is Integer numReplicates,
     *                the 2nd is String filePrefix, 3rd is Long seed;
     * @see FileConfig
     */
    @Override
    public void start(Object... configs) {
        //TODO better code
        if (configs.length == 1 && configs[0] instanceof FileConfig fileConfig) {
            this.fileConfig = fileConfig;
        } else if (configs.length == 2 && configs[0] instanceof Integer numReplicates &&
                configs[1] instanceof String filePrefix) {
            // store numReplicates, filePrefix
            fileConfig = new FileConfig(numReplicates, filePrefix);
        } else if (configs.length == 3 && configs[0] instanceof Integer numReplicates &&
                configs[1] instanceof File lphyFile &&
                configs[2] instanceof Long seed ) {
            try {
                fileConfig = new FileConfig(numReplicates, lphyFile, seed);
            } catch (IOException e) {
                LoggerUtils.log.severe(e.getMessage());
                throw new RuntimeException(e);
            }
        } else
            throw new UnsupportedOperationException("Unsupported configs to start " +
                    "in ValueFileLoggerListener : " + Arrays.toString(configs) + " !");
    }


    /**
     * Must apply filter to the list of values before this.
     * @param index   the index of each replicates of a simulation,
     *                which starts from 0.
     * @param values  the list of {@link Value} from one replicate of the simulation.
     */
    @Override
    public void replicate(int index, List<Value> values) {
        if (metadataById == null)
            metadataById = new TreeMap<>(); // sort by value id
        else if (index < 1) // index starts from 0
            metadataById.clear();

        if (linesById == null)
            linesById = new TreeMap<>(); // sort by index
        else if (index < 1) // index starts from 0
            linesById.clear();

        if (index < 1) {
            valuesByRepBuilder = new StringBuilder();
            valuesByRepColNamesBuilder = new StringBuilder();
        }

        validate(index, fileConfig.numReplicates);

        // for Mode.VALUE_PER_CELL
        boolean firstColValuePerCell = true;

        for (int i = 0; i < values.size(); i++) {

            Value value = values.get(i);
            List<ValueFormatter> formatters = valueFormatResolver.getFormatter(value);

            // if it is array, then one ValueFormatter for one element
            for (int j = 0; j < formatters.size(); j++) {
                ValueFormatter formatter = formatters.get(j);

                if (formatter == null) {
                    LoggerUtils.log.warning("Cannot find formatter for " + value.getId() +
                            ", type is " + value.getType());

                } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_FILE) {
                    // e.g. Alignment
                    ValueFormatHandler.ValuePerFile.createFile(index, formatter,
                            fileConfig.getFilePrefix(), fileConfig.getNumReplicates());

                    ValueFormatHandler.ValuePerFile
                            .exportValuePerFile(index, value, formatter);

                } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_LINE) {
                    // process meta data given 1st value
                    if (index == 0)
                        ValueFormatHandler.ValuePerLine.processHeaderFooter(formatter,
                                metadataById, fileConfig.getFilePrefix());

                    // e.g. Trees
                    ValueFormatHandler.ValuePerLine.populateValues(index, value, formatter, linesById);

                } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_CELL) {
                    // add col names and parameters values
                    ValueFormatHandler.ValuePerCell.addColumnNamesAndLines(index, firstColValuePerCell,
                            value, formatter, valuesByRepColNamesBuilder, valuesByRepBuilder);
                    firstColValuePerCell = false;

                } else
                    throw new RuntimeException("Unrecognised formatter mode : " + formatter.getMode() + " !");
            } // end for j
        } // end for i
        // ValuePerCell each line finish here
        valuesByRepBuilder.append("\n");
    }

    @Override
    public void complete() {

        if (linesById != null)
            ValueFormatHandler.ValuePerLine.exportValuePerLine(linesById, metadataById);

        if (! isStringBuilderEmpty(valuesByRepBuilder))
            // e.g. .log
            ValueFormatHandler.ValuePerCell.export(valuesByRepColNamesBuilder, valuesByRepBuilder,
                    ".log", fileConfig.getFilePrefix());

    }

    public static boolean isStringBuilderEmpty(StringBuilder stringBuilder) {
        if (stringBuilder == null) return true;
        String string = stringBuilder.toString();
        // Remove white spaces and newline characters
        String trimmedString = string.replaceAll("\\s", "");
        return trimmedString.isEmpty();
    }

    private void validate(int index, int numReplicates) {
        if (index < 0 || index >= numReplicates)
            throw new RuntimeException("The replication index " + index + " (start from 0) " +
                    "must be smaller than number of Replicates " + numReplicates);
    }


//    private void validateIDs(int index, String[] ids, String[] valueIDs) {
//        boolean allMatch = true;
//
//        if (ids.length != valueIDs.length) {
//            allMatch = false;
//        } else {
//            for (int i = 0; i < ids.length; i++) {
//                if (!ids[i].equals(valueIDs[i])) {
//                    // Elements at index i don't match
//                    allMatch = false;
//                    break;
//                }
//            }
//        }
//        if (!allMatch)
//            throw new RuntimeException("The values id cannot be different between two replicates : index = " +
//                    index + ", ids = " + Arrays.toString(ids) + ", ids2 =  " + Arrays.toString(valueIDs));
//    }
//
//    private void validateIdValLen(int index, List<String> ids, List<Object> valueVals) {
//        if (ids.size() != valueVals.size())
//            throw new RuntimeException("The replication " + index + " Value IDs are not matching values size " +
//                    valueVals.size() + ", IDs = " + ids);
//    }

}