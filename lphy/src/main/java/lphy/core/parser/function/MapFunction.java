package lphy.core.parser.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Narrative;
import lphy.core.model.NarrativeUtils;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.MapValue;
import lphy.core.parser.argument.ArgumentValue;

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

        Map<String, Object> map = new HashMap<>();
        Map<String, Value> valueMap = getParams();

        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value());
        }
        return new MapValue(null, map, this);
    }

    public String getInferenceNarrative(Value value, boolean unique, Narrative narrative) {

        StringBuilder builder = new StringBuilder();
        builder.append(NarrativeUtils.getValueClause(value, unique, narrative));

        Map<String, Value> valueMap = getParams();

        builder.append(" are ");
        int count = 0;
        for (Map.Entry<String, Value> entry : valueMap.entrySet()) {
            if (count > 0) {
                if (count == valueMap.size()-1) {
                    builder.append(" and ");
                } else builder.append(", ");
            }
            builder.append(entry.getKey());
            builder.append("=");
            if (entry.getValue().isAnonymous()) {
                builder.append(narrative.text(entry.getValue().codeString()));
            } else {
                builder.append(entry.getValue().getId());
            }
            count += 1;
        }
        builder.append(".");
        return builder.toString();
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
            if (entry.getValue().isAnonymous()) {
                builder.append(entry.getValue().codeString());
            } else {
                builder.append(entry.getValue().getId());
            }
            count += 1;
        }
        builder.append("}");
        return builder.toString();
    }
}
