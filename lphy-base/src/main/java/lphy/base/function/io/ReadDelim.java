package lphy.base.function.io;

import lphy.base.evolution.alignment.MetaDataAlignment;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.Table;
import lphy.core.model.datatype.TableValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * D = readFasta(file="h3n2_2deme.fna");
 * @see MetaDataAlignment
 */
public class ReadDelim extends DeterministicFunction<Table> {

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


        Table map = readDelim(filePath, delimiter, header);
        return new TableValue(null, map, this);
    }

    private Table readDelim(String filePath, String delimiter, boolean header) {
        Table dataMap = new Table();

        Path path = UserDir.getUserPath(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            String[] keys = null;

            if (header && (line = reader.readLine()) != null) {
                // 1st row is col names
                keys = line.split(delimiter);
            } else if (!header) {
                // 1st row is values, then create the default col names
                line = reader.readLine();
                if (line != null) {
                    String[] defaultKeys = new String[line.split(delimiter).length];
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

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(delimiter);

                    if (values.length == keyCount) {
                        for (int i = 0; i < keyCount; i++) {
                            // strings only currently
                            dataMap.get(keys[i]).add(values[i]);
                        }
                    } else {
                        LoggerUtils.log.warning("Not match the number columns, skipping line : " + line);
                    }
                }
            } else {
                LoggerUtils.log.severe("File is empty !");
            }
        } catch (FileNotFoundException e) {
            LoggerUtils.log.severe("File " + Path.of(filePath).toAbsolutePath() + " is not found !\n" +
                    "The current working dir = " + UserDir.getUserDir());
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
        }

        return dataMap;
    }

}
