package lphy.parser.functions;

import lphy.graphicalModel.*;

import java.util.HashMap;
import java.util.Map;

public class MapFunction extends DeterministicFunction<Map<String,Object>> {

    public MapFunction(ArgumentValue... argumentValues) {

        for (ArgumentValue argumentValue : argumentValues) {
            setParam(argumentValue.getName(), argumentValue.getValue());
        }
    }

    @GeneratorInfo(name="map",description = "A map defined by the argumentName=value pairs of its arguments.")
    public Value<Map<String, Object>> apply() {

        HashMap<String, Object> map = new HashMap<>();
        Map<String, Value> valueMap = getParams();

        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value());
        }
        return new Value<>(null, map, this);
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        Map<String, Value> valueMap = getParams();
        int count = 0;
        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            if (count > 0) builder.append(", ");
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue().codeString());
            count += 1;
        }
        builder.append("}");
        return builder.toString();
    }
}
