package james.graphicalModel;

public interface Loggable<U> {

    String[] getLogTitles(Value<U> value);

    String[] getLogValues(Value<U> value);
}
