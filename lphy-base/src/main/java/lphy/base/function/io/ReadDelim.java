package lphy.base.function.io;

import lphy.base.evolution.alignment.MetaDataAlignment;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.IOFunction;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.Table;
import lphy.core.model.datatype.TableValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 */
@IOFunction(
        role = IOFunction.Role.dataInput,
        extensions = { ".csv", ".tsv"},
        fileArgument = ReaderConst.FILE
)
public class ReadDelim extends DeterministicFunction<Table> {
//TODO provide column types ?
    public ReadDelim(@ParameterInfo(name = ReaderConst.FILE, description = "the file name including path.")
                     Value<String> filePath,
                     @ParameterInfo(name = ReaderConst.DELIMITER,
                             description = "the separator (delimiter) to separate values in each row.")
                     Value<String> delimiter,
                     @ParameterInfo(name = ReaderConst.HEADER, description = "If 'header' is true, as default, " +
                             "then use the 1st row as the map keys, otherwise it will create keys and load the values " +
                             "from the 1st row.", optional=true)
                     Value<Boolean> header,
                     @ParameterInfo(name = ReaderConst.COMMENT, description = "The comment character to " +
                             "ignore everything after it in one line. The default is #.", optional=true)
                     Value<String> commentChar) {


        if (filePath == null) throw new IllegalArgumentException("The file name can't be null!");
        setParam(ReaderConst.FILE, filePath);
        setParam(ReaderConst.DELIMITER, delimiter);

        // default to true
        if (header != null)
            setParam(ReaderConst.HEADER, header);
        else setParam(ReaderConst.HEADER, new Value<>(null, true));
        // default to #
        if (commentChar != null)
            setParam(ReaderConst.COMMENT, commentChar);
        else setParam(ReaderConst.COMMENT, new Value<>(null, "#"));
    }


    @GeneratorInfo(name="readDelim", verbClause = "is read from",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "A function that loads values from a data delimited file and returns a map.")
    public Value<Table> apply() {

        String filePath = ((Value<String>) getParams().get(ReaderConst.FILE)).value();
        String delimiter = ((Value<String>) getParams().get(ReaderConst.DELIMITER)).value();
        Boolean header = ((Value<Boolean>) getParams().get(ReaderConst.HEADER)).value();
//TODO  String commentChar = ((Value<String>) getParams().get(ReaderConst.COMMENT)).value();

        Table map = readDelim(filePath, delimiter, header);
        return new TableValue(null, map, this);
    }

    // TODO ignore "#"
    private Table readDelim(String filePath, String delimiter, boolean header) {
        Table dataMap = new Table();

        Path path = UserDir.getUserPath(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            String[] keys = null;

            String[] firstRowValues = null;
            if (header && (line = reader.readLine()) != null) {
                // 1st row is col names
                keys = line.split(delimiter);
            } else if (!header) {
                // 1st row is values, then create the default col names
                line = reader.readLine();
                if (line != null) {
                    firstRowValues = line.split(delimiter);
                    String[] defaultKeys = new String[firstRowValues.length];
                    for (int i = 0; i < defaultKeys.length; i++) {
                        defaultKeys[i] = "Column" + (i + 1);
                    }
                    keys = defaultKeys;
                }
            }

            int keyCount = keys != null ? keys.length : 0;

            if (keyCount > 0) {
                // put each column into a list
                for (String key : keys) {
                    dataMap.put(key, new ArrayList<>());
                }

                int l = 1;
                Class[] dataTypes = new Class[keyCount];

                // When header=false, add the first row values that were consumed for column counting
                if (firstRowValues != null && firstRowValues.length == keyCount) {
                    for (int i = 0; i < keyCount; i++) {
                        Object obj = Table.getValueGuessType(firstRowValues[i]);
                        dataTypes[i] = obj.getClass();
                        dataMap.get(keys[i]).add(obj);
                    }
                    l++;
                }

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(delimiter);

                    if (values.length == keyCount) {

                        for (int i = 0; i < keyCount; i++) {
                            // Convert String into guessed type
                            Object obj = Table.getValueGuessType(values[i]);
                            // record class type for the 1st row
                            if (l==1)
                                dataTypes[i] = obj.getClass();
                            else if (!obj.getClass().equals(dataTypes[i])) {
                                // Silently handle Integer/Double mismatches in both directions:
                                // - Integer column + Double value: promote column to Double (avoids truncation)
                                // - Double column + Integer value: widen Integer to Double (lossless)
                                if (dataTypes[i].equals(Integer.class) && obj instanceof Double) {
                                    dataTypes[i] = Double.class;
                                    // Convert all previously stored Integer values in this column to Double
                                    java.util.List<Object> col = dataMap.get(keys[i]);
                                    for (int k = 0; k < col.size(); k++) {
                                        if (col.get(k) instanceof Integer) {
                                            col.set(k, ((Integer) col.get(k)).doubleValue());
                                        }
                                    }
                                    // obj is already Double, no cast needed
                                } else if (dataTypes[i].equals(Double.class) && obj instanceof Integer) {
                                    // Lossless widening: Integer to Double
                                    obj = ((Integer) obj).doubleValue();
                                } else {
                                    LoggerUtils.log.warning("The column " + i + " in line " + l +
                                            " has a different data type with the 1st row ! Cast " + obj +
                                            " to " + dataTypes[i]);
                                    obj = castType(obj, dataTypes[i]);
                                }
                            }

                            // obj type is guessed, but if it is diff to the 1st one,
                            // then cast to the same type of 1st row.
                            dataMap.get(keys[i]).add(obj);
                        }


                    } else {
                        LoggerUtils.log.warning("Not match the number columns, skipping line : " + line);
                    }
                    l++;
                }
            } else {
                LoggerUtils.log.severe("File is empty !");
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            LoggerUtils.log.severe("File " + Path.of(filePath).toAbsolutePath() + " is not found !\n" +
                    "The current working dir = " + UserDir.getUserDir());
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
        }

        return dataMap;
    }

    public static Object castType(Object obj, Class<?> dataType) {
        Class<?> objClass = obj.getClass();

        // If the object is already of the desired type, return it directly
        if (objClass.equals(dataType))
            return obj;

        if (obj instanceof Number) {
            if (dataType.equals(Double.class)) // Handling Integer to Double conversion
                return ((Number) obj).doubleValue();
            else if (dataType.equals(Integer.class)) // Handling Double to Integer conversion
                return ((Number) obj).intValue();
        }

        // General case: Attempt to cast to the desired type
        try {
            return dataType.cast(obj);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The object " + obj + " cannot be cast to " + dataType, e);
        }
    }

}
