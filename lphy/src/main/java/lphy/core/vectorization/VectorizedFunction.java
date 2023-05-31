package lphy.core.vectorization;

import lphy.core.graphicalmodel.components.*;
import lphy.core.narrative.Narrative;
import lphy.core.narrative.NarrativeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorizedFunction<T> extends DeterministicFunction<T[]> {

    // the parameters (vectors)
    Map<String, Value> params;

    // the base types of the parameters in the base distribution
    // the keys are the argument names
    Map<String, Class> baseTypes = new TreeMap<>();

    List<DeterministicFunction<T>> componentFunctions;

    public VectorizedFunction(Constructor baseDistributionConstructor, List<Argument> argInfo, Object[] vectorArgs) {

        params = Generator.convertArgumentsToParameterMap(argInfo, vectorArgs);

        try {
            int size = VectorUtils.getVectorSize(argInfo, vectorArgs);
            componentFunctions = new ArrayList<>(size);
            for (int component = 0; component < size; component++) {
                componentFunctions.add((DeterministicFunction<T>) VectorUtils.getComponentGenerator(baseDistributionConstructor, argInfo, vectorArgs, component));
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
     *
     * @param component
     * @return
     */
    public DeterministicFunction<T> getComponentFunction(int component) {
        return componentFunctions.get(component);
    }

    @Override
    public Value<T[]> apply() {

        int vectorSize = VectorUtils.getVectorSize(params, baseTypes);
        List<Value> componentValues = new ArrayList<>();

        for (int i = 0; i < vectorSize; i++) {
            componentValues.add(getComponentFunction(i).apply());
        }
        return new CompoundVectorValue<>(null, componentValues, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {

        params.put(paramName, value);

        if (!VectorUtils.isVectorizedParameter(paramName, value, baseTypes)) {
            for (DeterministicFunction<T> componentFunction : componentFunctions) {
                // not setInput because the base distributions are hidden from the graphical model
                componentFunction.setParam(paramName, value);
            }
        } else {
            for (int i = 0; i < componentFunctions.size(); i++) {
                if (value instanceof CompoundVector) {
                    componentFunctions.get(i).setInput(paramName, ((CompoundVector) value).getComponentValue(i));
                } else {
                    SliceValue<?> sv = new SliceValue<>(i, value);
                    componentFunctions.get(i).setInput(paramName, sv);
                }
            }
        }
    }

    public String getTypeName() {
        if (componentFunctions.size() > 1)
            return "vector of " + NarrativeUtils.pluralize(componentFunctions.get(0).getTypeName());
        return Generator.getReturnType(this.getClass()).getSimpleName();
    }


    @Override
    public String getName() {
        return componentFunctions.get(0).getName();
    }

    public String codeString() {
        return Func.codeString(componentFunctions.get(0), getParams());
    }

    public Value getReplicatesValue() {
        for (Map.Entry<String, Value> entry : getParams().entrySet()) {
            Generator paramGenerator = entry.getValue().getGenerator();
            if (paramGenerator != null && isVectorizedParameter(entry.getKey())) {
                if (paramGenerator instanceof IID) {
                    return ((IID) (entry.getValue().getGenerator())).getReplicates();
                } else if (paramGenerator instanceof VectorizedDistribution) {
                    Value replicatesValue = ((VectorizedDistribution)paramGenerator).getReplicatesValue();
                    if (replicatesValue != null) return replicatesValue;
                } else if (paramGenerator instanceof VectorizedFunction) {
                    Value replicatesValue = ((VectorizedFunction)paramGenerator).getReplicatesValue();
                    if (replicatesValue != null) return replicatesValue;
                } else {
                    VectorUtils.getReplicatesValue(paramGenerator, componentFunctions.size());
                }
            }
        }
        return null;
    }

    public boolean isVectorizedParameter(String paramName) {
        return VectorUtils.isVectorizedParameter(paramName, getParams().get(paramName), baseTypes);
    }

    public List<DeterministicFunction<T>> getComponentFunctions() {
        return componentFunctions;
    }

    public String getInferenceNarrative(Value value, boolean unique, Narrative narrative) {

        if (value instanceof CompoundVectorValue) {
            CompoundVectorValue vrv = (CompoundVectorValue) value;

            StringBuilder builder = new StringBuilder();

            Generator generator = componentFunctions.get(0);
            Value v = vrv.getComponentValue(0);

            String inferenceNarrative = generator.getInferenceNarrative(v, unique, narrative);
            inferenceNarrative = inferenceNarrative.replaceAll(VectorUtils.INDEX_SEPARATOR + "0", narrative.subscript("i"));
            inferenceNarrative = inferenceNarrative.replaceAll(VectorUtils.INDEX_SEPARATOR + "\\{0}", narrative.subscript("i"));

            builder.append(inferenceNarrative);
            return builder.toString();
        }
        throw new RuntimeException("Expected CompoundVectorValue!");
    }

    /**
     * @param value
     * @return the narrative name for the given value, being a parameter of this generator.
     */
    public String getNarrativeName(Value value) {
        String paramName = getParamName(value);
        Generator generator = componentFunctions.get(0);
        return generator.getNarrativeName(paramName);
    }

//    public static void main(String[] args) {
//
//        Value<Number> arg = new Value<>("arg", 1);
//
//        Exp exp = new Exp(arg);
//
//        Map<String, Value> params = new HashMap<>();
//        params.put("arg", new Value<>("arg", new Number[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
//        Object[] initArgs = {params.get("arg")};
//
//        System.out.println(" vector match = " + ParserUtils.vectorMatch(Generator.getArguments(exp.getClass(), 0), initArgs));
//
//        Constructor constructor = exp.getClass().getConstructors()[0];
//
//        VectorizedFunction<Double> v = new VectorizedFunction<>(constructor, Generator.getArguments(constructor), initArgs);
//        Value<Double[]> repValue = v.apply();
//
//        Double[] rv = repValue.value();
//
//        System.out.println(Arrays.toString(rv));
//    }
}
