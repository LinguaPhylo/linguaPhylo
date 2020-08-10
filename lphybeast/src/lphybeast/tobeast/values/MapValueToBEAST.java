package lphybeast.tobeast.values;

import beast.core.parameter.RealParameter;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.*;

public class MapValueToBEAST implements ValueToBEAST<Map<String, Double>, RealParameter> {

    @Override
    public RealParameter valueToBEAST(Value<Map<String, Double>> value, BEASTContext context) {

        Map<String, Double> map = value.value();

        RealParameter parameter = new RealParameter();

        SortedMap<String, Double> sortedMap = null;
        if (map instanceof SortedMap) {
            sortedMap = (SortedMap<String, Double>)map;
        } else {
            sortedMap = new TreeMap<>();
            sortedMap.putAll(map);
        }

        String[] keys = new String[sortedMap.size()];
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
            keys[values.size()] = entry.getKey();
            values.add(entry.getValue());
        }

        StringBuilder builder = new StringBuilder();
        builder.append(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            builder.append(" ");
            builder.append(keys[i]);
        }


        parameter.setInputValue("value", values);
        parameter.setInputValue("keys", builder.toString());
        parameter.initAndValidate();
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());

        return parameter;
    }

    @Override
    public Class getValueClass() {
        return java.util.Map.class;
    }

    @Override
    public Class<RealParameter> getBEASTClass() {
        return RealParameter.class;
    }

}
