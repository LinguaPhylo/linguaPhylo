package lphy.core.codebuilder;

import lphy.core.model.Generator;
import lphy.core.model.Value;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

}
