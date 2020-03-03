package james.utils;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public enum Type {
        ERROR,
        WARNING,
        INFO
    }

    private static List<MessageListener> listeners = new ArrayList<>();

    public static void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public static void message(Type type, String message, Object source) {
        switch (type) {
            case INFO: System.out.println("INFO: " + message); break;
            case WARNING: System.out.println("WARNING: " + message); break;
            case ERROR: error(message, source);
        }
    }

    public static void error(String message, Object source) {
        System.err.println("ERROR: " + message);
        notifyListeners(Type.ERROR, message, source);
    }

    private static void notifyListeners(Type type, String message, Object source) {
        for (MessageListener listener : listeners) {
            listener.message(type, message, source);
        }
    }
}
