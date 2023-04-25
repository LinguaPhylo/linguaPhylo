package lphy.graphicalModel.code;

import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

}
