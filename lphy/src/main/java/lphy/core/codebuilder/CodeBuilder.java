package lphy.core.codebuilder;

import lphy.core.graphicalmodel.components.Generator;
import lphy.core.graphicalmodel.components.Value;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

}
