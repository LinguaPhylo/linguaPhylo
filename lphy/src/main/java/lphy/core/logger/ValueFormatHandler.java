package lphy.core.logger;

import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.model.Value;
import lphy.core.vectorization.VectorUtils;
import lphy.core.vectorization.VectorizedRandomVariable;

import java.io.FileNotFoundException;
import java.util.*;

public class ValueFormatHandler {

    public static final CharSequence DELIMITER = "\t";

    public static class ValuePerFile {

        public static void exportValuePerFile(int index, Value value, ValueFormatter formatter,
                                              FileConfig fileConfig) {

            String[] ids = formatter.getValueID(value);
            String[] headers = formatter.header(value);
            String[] bodies = formatter.format(value);
            String[] footers = formatter.footer(value);
            assert ids.length == bodies.length;

            String fileExtension = formatter.getExtension();
            // 1 alignment per file
            for (int i = 0; i < bodies.length; i++) {
                // if maxId > 0, add postfix, e.g. _0.nexus
                String fileName = fileConfig.getOutFileName(ids[i], index, fileExtension);
                try {
                    OutputSystem.setOut(fileName);
                } catch (FileNotFoundException e) {
                    LoggerUtils.log.severe("Cannot find file " + fileName + " !");
                    e.printStackTrace();
                }

                // in case 1 header for multiple bodies
                String header = headers.length == 1 ? headers[0] : headers[i];
                OutputSystem.out.println(header);

                // overwrite to get indents
                String indent = formatter.getRowName(i);
                OutputSystem.out.println(indent + bodies[i]);

                String footer = footers.length == 1 ? footers[0] : footers[i];
                OutputSystem.out.println(footer);

                System.out.println("Output " + value.getId() + " to the file : " + fileName +
                        " in the directory " + OutputSystem.getOutputDirectory());

                OutputSystem.out.close();
            }
        }

    }

    public static class ValuePerLine {

        public static void populateValues(Value value, Map<String, List<Value>> valuesById) {

            if (value instanceof VectorizedRandomVariable vectRandVar) {
                // VectorizedRandomVariable
                for (int i = 0; i < vectRandVar.size(); i++) {
                    // make sure to populate the vectorized values into different List<Value>
                    String id = vectRandVar.getId() + VectorUtils.INDEX_SEPARATOR + i;
                    List<Value> values = valuesById.computeIfAbsent(id, k -> new ArrayList<>());
                    values.add(vectRandVar.getComponentValue(i));
                }
            } else if (value.getType().isArray()) {
                throw new RuntimeException("To use ValuePerLine, Vectorization must result to VectorizedRandomVariable ! " + value.getClass());
            } else {
                List<Value> values = valuesById.computeIfAbsent(value.getId(), k -> new ArrayList<>());
                values.add(value);
            }

        }


        public static void exportValuePerLine(Map<String, List<Value>> valuesById, FileConfig fileConfig,
                                              ValueFormatResolver valueFormatResolver) {
            Objects.requireNonNull(valuesById).forEach((valId, valueList) -> {

                Value firstVal = valueList.get(0);
                if ( ! valId.contains(firstVal.getId()) )
                    throw new RuntimeException("Value id " + firstVal.getId() + " in the value list in ValuePerLine " +
                            "is not matching its key in the map " + valId);

                // all Values in the list should be the same type
                ValueFormatter formatter = valueFormatResolver.getFormatter(firstVal);

                String fileExtension = formatter.getExtension();
                // e.g. _psi.trees
                String fileName = fileConfig.getOutFileName(valId, fileExtension);
                try {
                    OutputSystem.setOut(fileName);
                } catch (FileNotFoundException e) {
                    LoggerUtils.log.severe("Cannot find file " + fileName + " !");
                    e.printStackTrace();
                }

                // print header once per file
                String[] headers = formatter.header(firstVal);
                Arrays.stream(headers)
                        .filter(str -> str != null && !str.isEmpty())
                        .forEach(OutputSystem.out::println);

                // 1 Value Id per file, but each replicate (tree) per line in a file
                // the i should match the index of replicates
                for (int i = 0; i < valueList.size(); i++) {
                    Value value = valueList.get(i);

                    if ( ! firstVal.getClass().equals(value.getClass()) )
                        throw new RuntimeException("all values in the list for ValuePerLine should be the same type " +
                                firstVal.getClass() + " ! But find " + value.getClass());
                    else if ( ! firstVal.getId().equals(value.getId()) )
                        throw new RuntimeException("Value id " + value.getId() + " in the value list in ValuePerLine " +
                                "should be same as " + firstVal.getId());
                    else {
                        // should no vectorized values in this stage
                        // print body pre line
                        String[] bodies = formatter.format(value);
                        // overwrite for trees
                        String rowName = formatter.getRowName(i);
                        // paste same rowName to multiple bodies for one value,
                        // 1 body per line
                        Arrays.stream(bodies)
                                .filter(line -> line != null && !line.isEmpty())
                                .forEach(line -> OutputSystem.out.println(rowName + line));
                    }
               } // end for i

                // print footers once per file
                String[] footers = formatter.footer(firstVal);
                Arrays.stream(footers)
                        .filter(str -> str != null && !str.isEmpty())
                        .forEach(OutputSystem.out::println);

                OutputSystem.out.close();

                System.out.println("Output " + valId + " to the file : " + fileName +
                        " in the directory " + OutputSystem.getOutputDirectory());
            });

        }

    }

    public static class ValuePerCell {

        public static void populateValues(int index, Value value, Map<Integer, List<Value>> valuesByReplicates) {
            List<Value> rowValues = valuesByReplicates.computeIfAbsent(index, k -> new ArrayList<>());

            if (value instanceof VectorizedRandomVariable vectRandVar) {
                // VectorizedRandomVariable
                for (int i = 0; i < vectRandVar.size(); i++) {
                    // make sure to populate the vectorized values into different List<Value>
                    rowValues.add(vectRandVar.getComponentValue(i));
                }
//            } else if (value.getType().isArray()) {
//                throw new RuntimeException("To use ValuePerCell, Vectorization must result to VectorizedRandomVariable ! " + value.getClass());
            } else {
                rowValues.add(value);
            }
        }


        public static int exportAllValues(Map<Integer, List<Value>> valuesByReplicates, FileConfig fileConfig,
                                          ValueFormatResolver valueFormatResolver) {

            List<Value> firstVals = Objects.requireNonNull(valuesByReplicates).values()
                    .stream().findFirst().orElse(null);
            if (firstVals == null || firstVals.size() < 1)
                throw new RuntimeException("The map valuesByReplicates is empty in ValuePerCell!");
            ValueFormatter formatter = valueFormatResolver.getFormatter(firstVals.get(0));

            // they should be same, e.g. .log
            String fileExtension = formatter.getExtension();
            String fileName = fileConfig.getOutFileName(fileExtension);
            try {
                OutputSystem.setOut(fileName);
            } catch (FileNotFoundException e) {
                LoggerUtils.log.severe("Cannot find file " + fileName + " !");
                e.printStackTrace();
            }

            // Start at 1st Value id, if 1d or 2d array,
            // then the id is composed into an array of id with index
            List<String> ids = new ArrayList<>(Arrays.asList(formatter.getValueID(firstVals.get(0))));
            // Build column names
            for (int i = 1; i < firstVals.size(); i++) {
                Value firstVal = firstVals.get(i);
                // all Values in the list should be the same type
                formatter = valueFormatResolver.getFormatter(firstVal);
                ids.addAll(Arrays.stream(formatter.getValueID(firstVal)).toList());
            }
            // an additional column name
            ids.add(0, "Sample");
            // write col names
            OutputSystem.out.println(String.join(DELIMITER, ids));

            // Build bodies line by line
            int sampleCount = 0;
            List<Value> lastValueList = null;
            for (Map.Entry<Integer, List<Value>> col : valuesByReplicates.entrySet()) {
                int index = col.getKey();
                if ( index != sampleCount )
                    throw new RuntimeException("The row index " + sampleCount + " in ValuePerCell " +
                            "is not matching its key in the map " + index);
                final List<Value> valueList = col.getValue();
//                System.out.println("Key: " + valId + ", Value: " + valueList);

                //TODO ignore header, footer at the moment

                StringBuilder lineBuilder = new StringBuilder();
                // write each row
                for (int i = 0; i < valueList.size(); i++) {
                    Value currentVal = valueList.get(i);
                    // all Values in the list should be the same type
                    formatter = valueFormatResolver.getFormatter(currentVal);

                    String[] bodies =  formatter.format(currentVal);
                    if (lastValueList != null &&
                            !currentVal.getId().equals(lastValueList.get(i).getId()))
                        throw new RuntimeException("");

                    if (i == 0) {
                        // TODO first column has to add a row name
                        String rowName = String.valueOf(sampleCount);
                        lineBuilder.append(rowName);
                    }
                    for (String b : bodies)
                        lineBuilder.append(DELIMITER).append(b);

                } // end for i

                OutputSystem.out.println(lineBuilder);
                lastValueList = new ArrayList<>(valueList);
                sampleCount++;
            }

            OutputSystem.out.close();
            System.out.println("Output values to the file : " + fileName +
                    " in the directory " + OutputSystem.getOutputDirectory());

            return sampleCount;
        }

    }


}
