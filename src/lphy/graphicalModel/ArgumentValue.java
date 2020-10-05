package lphy.graphicalModel;

public class ArgumentValue {

    public ArgumentValue(String name, Value value) {
        this.name = name;
        this.value = value;
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