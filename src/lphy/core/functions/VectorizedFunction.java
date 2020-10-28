package lphy.core.functions;

import lphy.graphicalModel.*;
import lphy.parser.ParserUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class VectorizedFunction<T> extends DeterministicFunction<T[]> {

    // the generative distribution to be vectorized
    DeterministicFunction<T> baseDistribution;

    // the parameters (vectors)
    Map<String, Value> params;

    // the base types of the parameters in the base distribution
    Map<String, Class> baseTypes = new TreeMap<>();

    public VectorizedFunction(DeterministicFunction<T> baseDistribution, Map<String, Value> params) {
        this.baseDistribution = baseDistribution;

        this.params = params;

        baseDistribution.getParams().forEach((key, value) -> baseTypes.put(key, value.getType()));
    }

    @Override
    public Value<T[]> apply() {

        int size = 1;
        for (Map.Entry<String, Value> entry : params.entrySet()) {
            String name = entry.getKey();
            Value v = entry.getValue();
            if (isArrayOfType(v, baseTypes.get(name))) {
                int vectorSize = Array.getLength(v.value());
                if (size == 1) {
                    size = vectorSize;
                } else if (size != vectorSize) {
                    throw new RuntimeException("Vector sizes do not match!");
                }
                Object input = Array.get(v.value(), 0);
                baseDistribution.setParam(name, new Value(null, input));
            } else {
                baseDistribution.setParam(name, v);
            }
        }
        Value<T> first = baseDistribution.apply();

        T[] result = (T[]) Array.newInstance(first.value().getClass(), size);
        result[0] = first.value();
        for (int i = 1; i < result.length; i++) {
            for (Map.Entry<String, Value> entry : params.entrySet()) {
                String name = entry.getKey();
                Value v = entry.getValue();
                if (isArrayOfType(v, baseTypes.get(name))) {
                    Object input = Array.get(v.value(), i);
                    baseDistribution.setParam(name, new Value(null, input));
                }
            }
            result[i] = baseDistribution.apply().value();
        }
        return new Value<>(null, result, this);
    }

    /**
     * @param maybeArray
     * @param ofType
     * @return
     */
    static boolean isArrayOfType(Value maybeArray, Class ofType) {

        if (maybeArray.value().getClass().isArray()) {
            Class componentClass = maybeArray.value().getClass().getComponentType();
            return componentClass.isAssignableFrom(ofType);
        }
        return false;
    }

    @Override
    public Map<String, Value> getParams() {
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        params.put(paramName, value);
    }

    @Override
    public String getName() {
        return baseDistribution.getName();
    }

    public static void main(String[] args) {

        Value<Number> arg = new Value<>("arg", 1);

        Exp exp = new Exp(arg);

        Map<String, Value> params = new HashMap<>();
        params.put("arg", new Value<>("arg", new Number[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
        Object[] initArgs = {params.get("arg")};

        System.out.println(" vector match = " + ParserUtils.vectorMatch(Generator.getParameterInfo(exp.getClass(), 0), initArgs));

        VectorizedFunction<Double> v = new VectorizedFunction<>(exp, params);

        Value<Double[]> repValue = v.apply();

        Double[] rv = repValue.value();

        System.out.println(Arrays.toString(rv));
    }
}
