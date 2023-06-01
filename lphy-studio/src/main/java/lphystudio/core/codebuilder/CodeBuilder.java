package lphystudio.core.codebuilder;

import lphy.core.model.components.Generator;
import lphy.core.model.components.Value;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

}
