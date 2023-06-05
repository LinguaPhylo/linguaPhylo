package lphy.core.model.component;

import lphy.core.model.GraphicalModel;

public class ArgumentValue {

    public ArgumentValue(String name, Value value, GraphicalModel model, GraphicalModel.Context context) {
        this.name = name;
        this.value = value;
        if (context == GraphicalModel.Context.data) {
            model.getDataValues().add(value);
        } else {
            model.getModelValues().add(value);
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