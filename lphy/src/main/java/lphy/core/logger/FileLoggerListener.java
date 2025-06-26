package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static lphy.core.io.OutputSystem.getOutputFile;
import static lphy.core.spi.LoaderManager.valueFormatResolver;

/**
 * FileLoggerListener is for logging alignments and files by command line or slphy
 */
public class FileLoggerListener implements SimulatorListener {

    FileConfig fileConfig;
    BufferedWriter writer;

    // map file absolute paths to buffered writer
    Map<String, BufferedWriter> writerMap = new HashMap<>();

    String defaultLogPath = "";
    ValueFormatter defaultValueFormatter;
    List<String> headerList = new ArrayList<>(); // header for default logger
    List<String> rowValues = new ArrayList<>(); // values for first row
    int col = 0; // column number for default logger

    public final String DELIMITER = "\t";

    @Override
    public void start(Object... configs) {
        // TODO: tidy up this code (copied from previous version)
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
        } else {
            throw new UnsupportedOperationException("Unsupported configs to start in ValueFileLoggerListener : " + Arrays.toString(configs) + " !");
        }
    }

    /**
     * 
     * @param index   the index of each replicates of a simulation,
     *                which starts from 0.
     * @param values  the list of {@link Value} from one replicate of the simulation.
     */
    @Override
    public void replicate(int index, List<Value> values) {
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            Class type = value.getType();

            if (TextFileFormatted.class.isAssignableFrom(type)) {
                // formatter for extensions
                TextFileFormatted fileFormatted = (TextFileFormatted) value.value();
                String fileExtension = fileFormatted.getFileType();
                String canonicalId = value.getCanonicalId();
                String fileName = FileConfig.getOutFileName(canonicalId, index,
                        fileConfig.getNumReplicates(), fileConfig.getFilePrefix(), fileExtension);
                File file = getOutputFile(fileName, true);

                try {
                    file.createNewFile(); // create file
                    writer = new BufferedWriter(new FileWriter(file));
                    fileFormatted.writeToFile(writer); // write output to file
                    writer.close(); // close file
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } else {
                // else default lphy core formatters
                List<ValueFormatter> formatters = valueFormatResolver.getFormatter(value);
                for (int j = 0; j < formatters.size(); j++) {
                    ValueFormatter formatter = formatters.get(j);
                    if (formatter == null) {
                        // no formatter available
                        LoggerUtils.log.warning(String.format("No formatter for %s of type %s", value.getId(), value.getType()));
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_FILE) {
                        writeValuePerFile(formatter, value, index);
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_LINE) {
                        writeValuePerLine(formatter, value, index);
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_CELL) {
                        writeValuePerCell(formatter, value, index);
                    }
                }
            }
        }

        if (fileConfig.getNumReplicates() == 1) { // write values for replicates = 1
            writeFirstRow(defaultValueFormatter);
        }
    }

    private void writeValuePerCell(ValueFormatter formatter, Value value, int sampleIndex) {
        // default log file with column headers (Sample, var1, var1, ...)
        File file = getFilePerCell(formatter);
        defaultLogPath = file.getAbsolutePath();
        defaultValueFormatter = formatter;
        if (sampleIndex == 0) {
            String header = formatter.header();
            headerList.add(header);
            rowValues.add(formatter.format(value.value()));
            try {
                if (writerMap.containsKey(file.getAbsolutePath())) {
                    writer = writerMap.get(file.getAbsolutePath());
                    writer.write(DELIMITER);
                    writer.write(header);
                } else {
                    file.createNewFile(); // create file
                    writer = new BufferedWriter(new FileWriter(file));
                    writerMap.put(defaultLogPath, writer);
                    writer.write("Sample" + DELIMITER + header);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                col++;
                // replicates do not need to write header
                writer = writerMap.get(file.getAbsolutePath());
                if (sampleIndex == 1 && col == 1) {
                    writeFirstRow(formatter);
                }
                if (formatter.header().equals(headerList.get(0))) {
                    // write replicate number for first column
                    String rowName = formatter.getRowName(sampleIndex); // TODO: rowName is the same as sampleIndex?
                    writer.write(rowName + DELIMITER);
                    formatter.writeToFile(writer, value.value());
                } else {
                    writer.write(DELIMITER);
                    formatter.writeToFile(writer, value.value());
                }
                if (col == headerList.size()) {
                    writer.write("\n");
                    col = 0;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void writeFirstRow(ValueFormatter formatter) {
        try {
            writer = writerMap.get(defaultLogPath);
            if (writer == null) {
                LoggerUtils.log.warning("Default logger not in writerMap, results may be unexpected!");
                return;
            }
            writer.write("\n");
            String rowName = formatter.getRowName(0);
            writer.write(rowName + DELIMITER);
            writer.write(String.join(DELIMITER, rowValues));
            writer.write("\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void writeValuePerFile(ValueFormatter formatter, Value value, int index) {
        // one value per file (alignment file)
        File file = getFile(formatter, index);
        try {
            file.createNewFile(); // create file
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(formatter.header());
            formatter.writeToFile(writer, value.value()); // write values to file
            writer.write(formatter.footer());
            writer.close(); // close file
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeValuePerLine(ValueFormatter formatter, Value value, int index) {
        // one value per line (tree file)
        File file = getFilePerLine(formatter);
        try {
            if (index == 0) {
                file.createNewFile(); // create file
                String header = formatter.header(); // file header
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(header + "\n");
                writerMap.put(file.getAbsolutePath(), writer);
            } else {
                writer = writerMap.get(file.getAbsolutePath());
            }

            if (index <= fileConfig.numReplicates - 1) {
                // write body
                String rowName = formatter.getRowName(index);
                writer.write(rowName); // row index
                formatter.writeToFile(writer, value.value()); // write values
                writer.write("\n\n");
            }

            if (index == fileConfig.numReplicates - 1) {
                // also write footer
                String footer = formatter.footer();
                writer.write(footer);
                writer.write("\n");
                writer.close(); // close file at last replicate
                writerMap.remove(file.getAbsolutePath()); // remove from writing list
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private File getFilePerCell(ValueFormatter formatter) {
        String filePrefix = fileConfig.getFilePrefix();
        String fileExtension = formatter.getExtension();
        String fileName = FileConfig.getOutFileName(filePrefix, fileExtension);
        return getOutputFile(fileName, true);
    }

    private File getFilePerLine(ValueFormatter formatter) {
        String filePrefix = fileConfig.getFilePrefix();
        String fileExtension = formatter.getExtension();
        String id = formatter.getValueID();
        String fileName = FileConfig.getOutFileName(id, filePrefix, fileExtension);
        return getOutputFile(fileName, true);
    }

    public void setOutputDir(String dir) {
        OutputSystem.setOutputDirectory(dir);
    }

    private File getFile(ValueFormatter formatter, int index) {
        String filePrefix = fileConfig.getFilePrefix();
        int numReplicates = fileConfig.getNumReplicates();
        String fileExtension = formatter.getExtension();
        String id = formatter.getValueID();
        String fileName = FileConfig.getOutFileName(id, index, numReplicates, filePrefix, fileExtension);

        return getOutputFile(fileName, true);
    }

    @Override
    public void complete() {
        if (writerMap == null || writerMap.size() == 0) {
            return;
        }

        try {
            // close all remaining buffered writers
            for (String key: writerMap.keySet()) {
                BufferedWriter w = writerMap.get(key);
                w.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
