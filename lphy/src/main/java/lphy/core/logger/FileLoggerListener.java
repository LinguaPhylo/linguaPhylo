package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static lphy.core.io.OutputSystem.getOutputFile;
import static lphy.core.spi.LoaderManager.valueFormatResolver;

public class FileLoggerListener implements SimulatorListener {

    FileConfig fileConfig;
    BufferedWriter writer;

    // map file absolute paths to buffered writer
    Map<String, BufferedWriter> writerMap;

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

    @Override
    public void replicate(int index, List<Value> values) {
        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            Class type = value.getType();

            if (TextFileFormatted.class.isAssignableFrom(type)) {
                // extension formatter (one file per type?)
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
                boolean firstCol = true;
                for (int j = 0; j < formatters.size(); j++) {
                    ValueFormatter formatter = formatters.get(j);
                    if (formatter == null) {
                        // no formatter available
                        LoggerUtils.log.warning(String.format("No formatter for %s of type %s", value.getId(), value.getType()));
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_FILE) {
                        // one value per file (alignment file)
                        File file = getFile(formatter, index);
                        try {
                            file.createNewFile(); // create file
                            writer = new BufferedWriter(new FileWriter(file));
                            formatter.writeToFile(writer, value.value()); // write values to file
                            writer.close(); // close file
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_LINE) {
                        // one value per line (tree file)
                        File file = getFile(formatter, index);
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

                            if (index < fileConfig.numReplicates - 1) {
                                // write body
                                String rowName = formatter.getRowName(index);
                                writer.write(rowName); // row index
                                formatter.writeToFile(writer, value.value()); // write values
                                writer.write("\n");
                            } else if (index == fileConfig.numReplicates - 1) {
                                // write footer
                                String footer = formatter.footer();
                                writer.write(footer);
                                writer.write("\n");
                                writer.close(); // close file at last replicate
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else if (formatter.getMode() == ValueFormatter.Mode.VALUE_PER_CELL) {
                        // default log file
                        // column headers (Sample, var1, var1, ...)
                        String rowName = formatter.getRowName(index);
                        File file = getFile(formatter, index);
                        if (index == 0) {
                            String header = formatter.header();
                            String styledHeader = "Sample" + DELIMITER + header;
                            try {
                                if (firstCol) {
                                    file.createNewFile(); // create file
                                    writer = new BufferedWriter(new FileWriter(file));
                                    writerMap.put(file.getAbsolutePath(), writer);
                                    writer.write(styledHeader); // header
                                    // replicate number
                                    writer.write(rowName + DELIMITER);
                                    // value
                                    formatter.writeToFile(writer, value.value());
                                    firstCol = false;
                                } else {
                                    writer = writerMap.get(file.getAbsolutePath());
                                    writer.write(DELIMITER);
                                    formatter.writeToFile(writer, value.value());
                                }

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                // replicates do not need to write header
                                writer = writerMap.get(file.getAbsolutePath());
                                if (firstCol) {
                                    writer.write(rowName + DELIMITER);
                                    formatter.writeToFile(writer, value.value());
                                    firstCol = false;
                                } else {
                                    writer.write(DELIMITER);
                                    formatter.writeToFile(writer, value.value());
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                        }

                    }

                }
            }

        }
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
//        return new File(fileName);
    }

    @Override
    public void complete() {
        if (writerMap == null || writerMap.size() == 0) {
            return;
        }

        try {
            // close all remaining buffered writers
            for (BufferedWriter w : writerMap.values()) {
                w.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
