package lphystudio.core.codebuilder;

import lphy.core.model.components.Generator;
import lphy.core.model.components.RandomVariable;
import lphy.core.model.components.Value;

public class LaTexCodeBuilder implements CodeBuilder {


    static String variableColor = "green";
    static String constantColor = "magenta";
    static String otherColor = "black";

    public String valueToCodeString(Value value) {

        StringBuilder builder = new StringBuilder();

        Generator generator = value.getGenerator();

        if (!value.isAnonymous()) {
            builder.append("\\textcolor{");
            if (value instanceof RandomVariable) {
                builder.append(variableColor);
            } else if (value.getGenerator() == null) {
                builder.append(constantColor);
            } else {
                builder.append(otherColor);
            }
            builder.append("}{");
            builder.append(value.getId());
            builder.append("} ");
            builder.append(generator.generatorCodeChar());
            builder.append(" ");
            builder.append(generatorToCodeString(generator));
            return builder.toString();
        } else return value.codeString();
    }

    public String generatorToCodeString(Generator generator) {
        return generator.codeString();
    }
}
