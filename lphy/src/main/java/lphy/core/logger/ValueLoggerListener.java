package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;
import lphy.core.spi.LoaderManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ValueLoggerListener implements SimulatorListener {

//    Map<ValueFormatter, Mode> modeMap;

    //TODO where and how to init Mode
//    public LPhySimulatorLoggerListener(Map<ValueFormatter, Mode> modeMap) {
//        this.modeMap = modeMap;
//    }

//    @Override
//    public void setFormatterMode(ValueFormatter valueFormatter, Mode mode) {
//        modeMap.put(valueFormatter, mode);
//    }
//
//    @Override
//    public Mode getFormatterMode(ValueFormatter valueFormatter) {
//        return modeMap.get(valueFormatter);
//    }

//    @Override
//    public Map<ValueFormatter, Mode> getModeMap() {
//        return modeMap;
//    }

    /**
     * For ValuePerLine, the key represents the value id and is used for the file name.
     * Each list of values is logged into a separate file, with each value on its own line.
     */
    Map<String, List<Value>> valuesById;
    /**
     * For ValuePerCell, the key represents the index of replicates.
     * All lists of values are logged into one file, with each list occupying one row.
     */
    Map<Integer, List<Value>> valuesByReplicates;

    // numReplicates, filePrefix
    FileConfig fileConfig;

    private static final ValueFormatResolver valueFormatResolver = LoaderManager.valueFormatResolver;


    public static boolean isValueLoggable(Value randomValue) {
        return randomValue instanceof RandomVariable ||
                // random value but no anonymous
                (randomValue.isRandom() && !randomValue.isAnonymous());
    }


    /**
     * @param configs Two options: 1) only contain {@link FileConfig};
     *                2) the 1st element is Integer numReplicates,
     *                and the 2nd is File lphyFile.
     */
    @Override
    public void start(List<Object> configs) {
        if (configs.get(0) instanceof FileConfig fileConfig) {
            this.fileConfig = fileConfig;
        } else if (configs.get(0) instanceof Integer numReplicates &&
                configs.get(1) instanceof File lphyFile) {
            // store numReplicates, lphyFile
            try {
                fileConfig = new FileConfig(numReplicates, lphyFile);
            } catch (IOException e) {
                LoggerUtils.log.severe(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void replicate(int index, List<Value> values) {
        if (valuesById == null)
            valuesById = new TreeMap<>(); // sort by value id
        else if (index < 1) // index starts from 0
            valuesById.clear();

        if (valuesByReplicates == null)
            valuesByReplicates = new TreeMap<>(); // sort by index
        else if (index < 1) // index starts from 0
            valuesByReplicates.clear();

        validate(index, fileConfig.numReplicates);

        // filter to RandomValue
        List<Value> loggableValues = values.stream()
                .filter(ValueLoggerListener::isValueLoggable)
                .toList();

        ValueFormatter formatter;
        for (Value value : loggableValues) {
            formatter = valueFormatResolver.getFormatter(value);

            if (formatter == null) {
                LoggerUtils.log.warning("Cannot find formatter for " + value.getId() + ", type is " + value.getType());
            } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_FILE)
                // e.g. Alignments
                ValueFormatHandler.ValuePerFile.exportValuePerFile(index, value, formatter, fileConfig);
            else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_LINE)
                // e.g. Trees
                ValueFormatHandler.ValuePerLine.populateValues(value, valuesById);
            else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_CELL) // e.g. parameters
                ValueFormatHandler.ValuePerCell.populateValues(index, value, valuesByReplicates);
            else
                throw new RuntimeException("Unrecognised formatter mode : " + formatter.getMode() + " !");
        }

    }

    @Override
    public void complete() {

        if (valuesById != null)
            ValueFormatHandler.ValuePerLine
                .exportValuePerLine(valuesById, fileConfig, valueFormatResolver);

        if (valuesByReplicates != null)
            ValueFormatHandler.ValuePerCell
                .exportAllValues(valuesByReplicates, fileConfig, valueFormatResolver);

    }

    private void validate(int index, int numReplicates) {
        if (index < 0 || index >= numReplicates)
            throw new RuntimeException("The replication index " + index + " (start from 0) " +
                    "must be smaller than number of Replicates " + numReplicates);
    }


}