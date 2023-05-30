package lphy.core.cmd;

import lphy.core.graphicalmodel.components.Value;
import lphy.core.parser.LPhyMetaParser;

import java.util.Map;

@Deprecated
public interface Command {

    String getName();

    void execute(Map<String, Value<?>> params);

    default void execute(String commandString, LPhyMetaParser parser) {
        throw new UnsupportedOperationException("This class is deprecated!");
    }

    default String getSignature() {
        return getName() + "()";
    }

    class CommandUtils {

        public static boolean isLiteral(String expression) {

            if (expression.startsWith("\"") && expression.endsWith("\"")) {
                // is string
                return true;
            }

            if (expression.startsWith("[") && expression.endsWith("]")) {
                // is list
                return true;
            }

            if (isDouble(expression)) return true;

            if (isInteger(expression)) return true;

            return (isBoolean(expression));
        }

        private static boolean isInteger(String s) {
            try {
                Integer intVal = Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private static boolean isDouble(String s) {
            try {
                Double doubleVal = Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private static boolean isBoolean(String s) {
            return s.equals("true") || s.equals("false");
        }



    }
}
