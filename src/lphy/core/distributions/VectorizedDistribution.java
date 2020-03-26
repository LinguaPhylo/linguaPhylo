package lphy.core.distributions;

import lphy.graphicalModel.*;

import java.lang.reflect.Array;
import java.util.*;

public class VectorizedDistribution<T> implements GenerativeDistribution<T[]> {

    GenerativeDistribution<T> baseDistribution;

    Map<String, Value> params;

    Map<String, Class> baseTypes = new TreeMap<>();

//    public VectorizedDistribution(Class<GenerativeDistribution<T>> baseDistributionClass, Map<String, Value> params) {
//
//
//        List<Object> initargs = new ArrayList<>();
//        Constructor constructor = getConstructorByArguments(arguments, genDistClass, initargs);
//
//        if (constructor != null) {
//            GenerativeDistribution dist = (GenerativeDistribution) constructor.newInstance(initargs.toArray());
//            for (String parameterName : arguments.keySet()) {
//                Value value = arguments.get(parameterName);
//
//                dist.setInput(parameterName, value);
//            }
//            return dist;
//        }
//
//        this.params = params;
//
//        baseDistribution.getParams().forEach((key, value) -> {
//            baseTypes.put((String) key, ((Value) value).getType());
//        });
//    }

    public VectorizedDistribution(GenerativeDistribution<T> baseDistribution, Map<String, Value> params) {
        this.baseDistribution = baseDistribution;

        this.params = params;

        baseDistribution.getParams().forEach((key, value) -> {
            baseTypes.put((String) key, ((Value) value).getType());
        });
    }

    @Override
    public RandomVariable<T[]> sample() {

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
        Value<T> first = baseDistribution.sample();

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
            result[i] = baseDistribution.sample().value();
        }
        return new RandomVariable<>(null, result, this);
    }

    static boolean isArrayOfType(Value maybeArray, Class ofType) {


        Class arrayClass = Array.newInstance(ofType, 0).getClass();
        boolean isArray = arrayClass == maybeArray.value().getClass();

        //System.out.println("isArrayOfType(" + maybeArray + ", " + ofType + ") = " + isArray );

        return isArray;
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
        Beta beta = new Beta(new Value<Double>("alpha", 2.0), new Value<Double>("beta", 2.0));

        Map<String, Value> params = new HashMap<>();
        params.put("alpha", new Value<>("alpha", new Double[] {200.0, 200.0, 200.0, 3.0, 3.0, 3.0}));
        params.put("beta", new Value<>("beta", 2.0));

        VectorizedDistribution<Double> v = new VectorizedDistribution<>(beta, params);

        RandomVariable<Double[]> rbeta = v.sample();

        System.out.println(Arrays.toString(rbeta.value()));
    }
}
