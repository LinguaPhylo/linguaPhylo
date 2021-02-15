package lphy.graphicalModel.code;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

public class CanonicalCodeBuilder implements CodeBuilder {

    public String valueToCodeString(Value value) {
        return value.codeString();
    }

    public String generatorToCodeString(Generator generator) {
        return generator.codeString();
    }
}
