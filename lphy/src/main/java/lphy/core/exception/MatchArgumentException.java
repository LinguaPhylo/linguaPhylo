package lphy.core.exception;

import lphy.core.model.component.Value;

import java.lang.reflect.Constructor;

public class MatchArgumentException extends RuntimeException {

    String message = "Cannot match the ";
    Constructor constructor;
    String arg;
    Value value;

    public MatchArgumentException(Constructor constructor, String arg, Value value) {
        this.constructor = constructor;
        this.arg = arg;
        this.value = value;
    }

//    public IIDMatchException(String message, Constructor constructor, List<Argument> arguments,
//                             Object[] initargs, Map<String, Value> params) {
//    }

    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder(message);
        msg.append(" line ");

        msg.append(" character ");


//        if (context != null) {
            msg.append("\n -> ");
            String text = "context.getText()";
            msg.append(text);
            msg.append("\n    ");
            msg.append("^".repeat(text.length()));
//        }
        return msg.toString();
    }
}
