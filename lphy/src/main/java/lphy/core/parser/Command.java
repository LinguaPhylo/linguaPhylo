package lphy.core.parser;

import lphy.core.model.Value;

import java.util.Map;

//TODO the data lines can be strings
@Deprecated
public interface Command {

    String getName();

    void execute(Map<String, Value<?>> params);

    default void execute(String commandString, LPhyParserDictionary parser) {
        throw new UnsupportedOperationException("This class is deprecated!");
    }

    default String getSignature() {
        return getName() + "()";
    }

}
