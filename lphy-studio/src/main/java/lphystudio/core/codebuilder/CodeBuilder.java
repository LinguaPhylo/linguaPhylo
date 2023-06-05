package lphystudio.core.codebuilder;

import lphy.core.model.component.Generator;
import lphy.core.model.component.Value;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

}
