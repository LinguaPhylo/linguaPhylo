package lphy.core.logger;

import lphy.core.model.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implement this to make the value to be loggable into a file.
 * This can be used lphy extension developers to create their own format to log.
 * It supposes to replace {@link ValueFormatter} in future.
 */
public interface TextFileFormatted {

    List<String> getTextForFile();

    String getFileType();

    static List<Value> getLoggableValues(List<Value> allValues, Class clsMatched) {
        List<Value> values = new ArrayList<>();
        for (Value<?> v : allValues) {
            if (clsMatched.isAssignableFrom(v.getType())) {
                // TODO data clampping ?
//                if (! parser.isClampedVariable(v))
                values.add(v);
            }
        }
        values.sort(Comparator.comparing(Value::getId));
        return values;
    }

}
