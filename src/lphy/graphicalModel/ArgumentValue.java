package lphy.graphicalModel;

import lphy.core.LPhyParser;

public class ArgumentValue {

    public ArgumentValue(String name, Value value, LPhyParser parser, LPhyParser.Context context) {
        this.name = name;
        this.value = value;
        if (context == LPhyParser.Context.data) {
            parser.getDataValues().add(value);
        } else {
            parser.getModelValues().add(value);
        }
    }

    private String name;
    private Value value;

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }
}