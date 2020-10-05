package lphy.core.functions;

import lphy.graphicalModel.*;

import java.util.HashMap;

public class MapFunction extends DeterministicFunction<java.util.Map<String,Object>> {

    public MapFunction(ArgumentValue... argumentValues) {

        for (ArgumentValue argumentValue : argumentValues) {
            setParam(argumentValue.getName(), argumentValue.getValue());
        }
    }

    @GeneratorInfo(name="map",description = "A map defined by the argumentName=value pairs of its arguments.")
    public Value<java.util.Map<String, Object>> apply() {

        HashMap<String, Object> map = new HashMap<>();
        java.util.Map<String, Value> valueMap = getParams();

        for (java.util.Map.Entry<String, Value> entry : valueMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value());
        }
        return new Value<>(null, map, this);
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();

        builder.append("map(");
        java.util.Map<String, Value> valueMap = getParams();
        int count = 0;
        for (java.util.Map.Entry<String, Value> entry : valueMap.entrySet()) {
            if (count > 0) builder.append(", ");
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue().codeString());
            count += 1;
        }
        builder.append(")");
        return builder.toString();
    }
}
