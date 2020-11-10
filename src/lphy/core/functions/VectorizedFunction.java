package lphy.core.functions;

import lphy.core.distributions.VectorizedDistribution;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.VectorValue;
import lphy.parser.ParserUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map;

import static lphy.graphicalModel.VectorUtils.*;

public class VectorizedFunction<T> extends DeterministicFunction<T[]> {

    // the parameters (vectors)
    Map<String, Value> params;

    // the base types of the parameters in the base distribution
    // the keys are the argument names
    Map<String, Class> baseTypes = new TreeMap<>();

    List<DeterministicFunction<T>> componentFunctions;

    public VectorizedFunction(Constructor baseDistributionConstructor, List<ParameterInfo> pInfo, Object[] vectorArgs) {

        params = Generator.convertArgumentsToParameterMap(pInfo, vectorArgs);

        try {
            int size = getVectorSize(pInfo, vectorArgs);
            componentFunctions = new ArrayList<>(size);
            for (int component = 0; component < size; component++) {
                componentFunctions.add((DeterministicFunction<T>)getComponentGenerator(baseDistributionConstructor, pInfo, vectorArgs, component));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        componentFunctions.get(0).getParams().forEach((key, value) -> {
            if (value != null) baseTypes.put(key, value.getType());
        });
    }

    /**
     * This method is not thread safe!
     * @param component
     * @return
     */
    public DeterministicFunction<T> getComponentFunction(int component) {
        return componentFunctions.get(component);
    }

    @Override
    public Value<T[]> apply() {

        int vectorSize = getVectorSize(params, baseTypes);
        Value<T> first = getComponentFunction(0).apply();

        T[] result = (T[]) Array.newInstance(first.value().getClass(), vectorSize);
        result[0] = first.value();

        for (int i =1; i < vectorSize; i++) {
            result[i] = getComponentFunction(i).apply().value();
        }
        return new VectorValue<>(null, result, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {

        params.put(paramName, value);

        if (!isVectorizedParameter(paramName, value, baseTypes)) {
            for (DeterministicFunction<T> componentFunction : componentFunctions) {
                // not setInput because the base distributions are hidden from the graphical model
                componentFunction.setParam(paramName, value);
            }
        } else {
            for (int i = 0; i < componentFunctions.size(); i++) {
                Object input = Array.get(value.value(), i);
                // not setInput because the base distributions are hidden from the graphical model
                componentFunctions.get(i).setParam(paramName, new Value(value.getId() + "." + i, input));
            }
        }
    }

    @Override
    public String getName() {
        return componentFunctions.get(0).getName();
    }

    public static void main(String[] args) {

        Value<Number> arg = new Value<>("arg", 1);

        Exp exp = new Exp(arg);

        Map<String, Value> params = new HashMap<>();
        params.put("arg", new Value<>("arg", new Number[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
        Object[] initArgs = {params.get("arg")};

        System.out.println(" vector match = " + ParserUtils.vectorMatch(Generator.getParameterInfo(exp.getClass(), 0), initArgs));

        Constructor constructor = exp.getClass().getConstructors()[0];

        VectorizedFunction<Double> v = new VectorizedFunction<>(constructor, Generator.getParameterInfo(constructor), initArgs);
        Value<Double[]> repValue = v.apply();

        Double[] rv = repValue.value();

        System.out.println(Arrays.toString(rv));
    }

    public List<DeterministicFunction<T>> getComponentFunctions() {
        return componentFunctions;
    }
}
