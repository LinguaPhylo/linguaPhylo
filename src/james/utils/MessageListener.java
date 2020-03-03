package james.utils;

public interface MessageListener {

    void message(Message.Type type, String message, Object source);
}
