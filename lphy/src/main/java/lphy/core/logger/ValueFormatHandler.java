package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValueFormatHandler {

    public static final CharSequence DELIMITER = "\t";


    public static void createFile(String fileName) {
        OutputSystem.setOut(fileName);

        System.out.println("Create file : " + fileName +
                " in the directory " + OutputSystem.getOrCreateOutputDirectory());
    }


    final static int HEADER_ID = 0;
    final static int FOOTER_ID = 1;
    final static int FILE_NAME_ID = 2;

    public static class ValuePerFile {

        public static void createFile(int index, ValueFormatter formatter,
                                      String filePrefix, int numReplicates) {

            String fileExtension = formatter.getExtension();
            // If value is array, the id will be appended with index
            String id = formatter.getValueID();
            // e.g. 1 alignment per file
            // if maxId > 0, add postfix, e.g. _0.nexus
            String fileName = FileConfig
                    .getOutFileName(id, index, numReplicates, filePrefix, fileExtension);

            ValueFormatHandler.createFile(fileName);
        }


        public static void exportValuePerFile(int index, Value value, ValueFormatter formatter) {

            // here require the original id if value is array
            String header = formatter.header();
            // in case 1 header for multiple bodies
            if (header != null || !header.trim().isEmpty())
                OutputSystem.out.println(header);

            // overwrite to get indents
            String indent = formatter.getRowName(index);
            // here require the original value if value is array,
            // but return the formatted string at ith element
            String body = formatter.format(value.value());
            OutputSystem.out.println(indent + body);

            String footer = formatter.footer();
            if (footer != null || !footer.trim().isEmpty())
                OutputSystem.out.println(footer);

            OutputSystem.out.close();

        }

    }

    public static class ValuePerLine {

        // suppose call once per file
        public static void processHeaderFooter( ValueFormatter formatter,
                                          Map<String, String[]> metadataByValueID, String filePrefix) {
            // here require the original id if value is array
            String header = formatter.header();
            // If value is array, the id will be appended with index
            String id = formatter.getValueID();

            final int len = 3;
            String[] metadata = metadataByValueID.computeIfAbsent(id, k -> new String[len]);
            // here require the original id if value is array
            metadata[HEADER_ID] = header;
            // print footers once per file
            metadata[FOOTER_ID] = formatter.footer();

            String fileExtension = formatter.getExtension();
            // file name, e.g. _psi.trees
            metadata[FILE_NAME_ID] = FileConfig
                    .getOutFileName(id, filePrefix, fileExtension);
        }

        public static void populateValues(int index, Value value, ValueFormatter formatter,
                                          Map<String, List<String>> formattedLinesByValueID) {

            // If value is array, the id will be appended with index
            String id = formatter.getValueID();

            // here require the original value if value is array,
            // but return the formatted string at ith element
            String body = formatter.format(value.value());
            // overwrite for trees
            String rowName = formatter.getRowName(index);

            // for a value, one replicate per line,
            String line = rowName + body;
            List<String> formattedLines = formattedLinesByValueID
                    .computeIfAbsent(id, k -> new ArrayList<>());
            formattedLines.add(line);

        }


        public static void exportValuePerLine(Map<String, List<String>> formattedLinesByValueID,
                                              Map<String, String[]> metadataByValueID) {
            Objects.requireNonNull(formattedLinesByValueID).forEach((formattedValueId, formattedLines) -> {

                String[] metadata = metadataByValueID.get(formattedValueId);
                // e.g. _psi.trees
                String fileName = metadata[FILE_NAME_ID];

                createFile(fileName);

                // use same header per value
                String header = metadata[HEADER_ID];
                if (header != null || !header.trim().isEmpty())
                    OutputSystem.out.println(header);

                // paste rowName and body in one line
                formattedLines.stream()
                        .filter(line -> line != null && !line.isEmpty())
                        .forEach(line -> OutputSystem.out.println(line));

                // use same footer per value
                String footer = metadata[FOOTER_ID];
                if (footer != null || !footer.trim().isEmpty())
                    OutputSystem.out.println(footer);

                OutputSystem.out.close();

            });

        }

    }

    public static class ValuePerCell {

        public static void addColumnNamesAndLines(int repId, boolean firstColValuePerCell, Value value,
                                    ValueFormatter formatter, StringBuilder valuesByRepColNamesBuilder,
                                    StringBuilder valuesByRepBuilder) {
            // add col names
            if (repId == 0) {
                if (firstColValuePerCell)
                    valuesByRepColNamesBuilder.append("Sample");
                // here require the original id if value is array
                String header = formatter.header();

                valuesByRepColNamesBuilder.append(DELIMITER).append(header);
            }


            if (firstColValuePerCell) {
                // TODO first column has to add a row name
                String rowName = formatter.getRowName(repId);
                valuesByRepBuilder.append(rowName);
            }

            // here require the original value if value is array,
            // but return the formatted string at ith element
            String body = formatter.format(value.value());
            valuesByRepBuilder.append(DELIMITER).append(body);
        }


        public static void export(StringBuilder valuesByRepColNamesBuilder, StringBuilder valuesByRepBuilder,
                                  String fileExtension, String filePrefix ) {

//            String fileExtension = formatter.getExtension();
            String fileName = FileConfig.getOutFileName(filePrefix, fileExtension);
            ValueFormatHandler.createFile(fileName);

            OutputSystem.out.println(valuesByRepColNamesBuilder);
            OutputSystem.out.println(valuesByRepBuilder);
//TODO ignore footer at the moment

            OutputSystem.out.close();

        }

    }


}
