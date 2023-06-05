package lphy.core.parser;

import lphy.core.model.component.Value;

import java.util.Map;

public interface Command {

    String getName();

    void execute(Map<String, Value<?>> params);

    default void execute(String commandString, LPhyMetaParser parser) {
        throw new UnsupportedOperationException("This class is deprecated!");
    }

    default String getSignature() {
        return getName() + "()";
    }

}
