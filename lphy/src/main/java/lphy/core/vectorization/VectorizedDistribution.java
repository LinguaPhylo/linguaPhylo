package lphy.core.vectorization;

import lphy.core.model.components.*;
import lphy.core.narrative.Narrative;
import lphy.core.narrative.NarrativeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorizedDistribution<T> implements GenerativeDistribution<T[]> {

    // the parameters (vectors)
    Map<String, Value> params;

    // the base types of the parameters in the base distribution
    // the keys are the argument names
    Map<String, Class> baseTypes = new TreeMap<>();

    List<GenerativeDistribution<T>> componentDistributions;

    public VectorizedDistribution(Constructor baseDistributionConstructor, List<Argument> arguments, Object[] vectorArgs) {

        params = Generator.convertArgumentsToParameterMap(arguments, vectorArgs);

        try {
            int size = VectorUtils.getVectorSize(arguments, vectorArgs);
            componentDistributions = new ArrayList<>(size);
            for (int component = 0; component < size; component++) {
                componentDistributions.add((GenerativeDistribution<T>) VectorUtils.getComponentGenerator(baseDistributionConstructor, arguments, vectorArgs, component));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        componentDistributions.get(0).getParams().forEach((key, value) -> {
            if (value != null) baseTypes.put(key, value.getType());
        });
    }

    public String getInferenceStatement(Value value, Narrative narrative) {
        StringBuilder builder = new StringBuilder();

        if (value instanceof VectorizedRandomVariable) {

            VectorizedRandomVariable vrv = (VectorizedRandomVariable) value;

            builder.append(narrative.product("i", "0", getTo(vrv, narrative)));

            Generator generator = componentDistributions.get(0);
            Value v = vrv.getComponentValue(0);

            String componentStatement = generator.getInferenceStatement(v, narrative);

            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "0", narrative.subscript("i"));
            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "\\{0}", narrative.subscript("i"));

            builder.append(componentStatement);

            return builder.toString();

        } else if (value instanceof RandomVariable && value.value().getClass().isArray()) {
            Object[] arr = (Object[]) value.value();

            Generator generator = componentDistributions.get(0);
            Value v = new Value(null, arr[0]);

            builder.append(narrative.product("i", "0", Integer.toString((arr.length - 1))));

            String componentStatement = generator.getInferenceStatement(v, narrative);

            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "0", narrative.subscript("i"));
            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "\\{0}", narrative.subscript("i"));

            builder.append(componentStatement);

            return builder.toString();
        }
        throw new RuntimeException("Expected VectorizedRandomVariable!");
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
                    VectorUtils.getReplicatesValue(paramGenerator, componentDistributions.size());
                }
            }
        }
        return null;
    }

    public boolean isVectorizedParameter(String paramName) {
        return VectorUtils.isVectorizedParameter(paramName, getParams().get(paramName), baseTypes);
    }

    private String getTo(VectorizedRandomVariable value, Narrative narrative) {
        Value replicatesValue = getReplicatesValue();
        if (replicatesValue != null) return narrative.getId(replicatesValue, false) + " - 1";
        return (value.size() - 1) + "";
    }

    public String getInferenceNarrative(Value value, boolean unique, Narrative narrative) {
        StringBuilder builder = new StringBuilder();

        if (value instanceof VectorizedRandomVariable) {
            VectorizedRandomVariable vrv = (VectorizedRandomVariable) value;

            Generator generator = componentDistributions.get(0);
            Value v = vrv.getComponentValue(0);


            String inferenceNarrative = generator.getInferenceNarrative(v, unique, narrative);
            inferenceNarrative = inferenceNarrative.replace( narrative.subscript("0"), narrative.subscript("i"));

            builder.append(inferenceNarrative);

            return builder.toString();
//        } else if (value.getGenerator() != null && value.getGenerator() instanceof VectorizedDistribution vectorizedDistribution) {
        } else if (value instanceof RandomVariable && value.value().getClass().isArray()) {
            Object[] arr = (Object[]) value.value();
            Generator generator = componentDistributions.get(0);
            Value v = new Value(null, arr[0]);

            String inferenceNarrative = generator.getInferenceNarrative(v, unique, narrative);
            inferenceNarrative = inferenceNarrative.replace( narrative.subscript("0"), narrative.subscript("i"));

            builder.append(inferenceNarrative);

            return builder.toString();
        }
        throw new RuntimeException("Expected VectorizedRandomVariable!");
    }

    /**
     * @param value
     * @return the narrative name for the given value, being a parameter of this generator.
     */
    public String getNarrativeName(Value value) {
        String paramName = getParamName(value);
        Generator generator = componentDistributions.get(0);
        return generator.getNarrativeName(paramName);
    }

    /**
     * This method is not thread safe!
     *
     * @param component
     * @return
     */
    public GenerativeDistribution<T> getBaseDistribution(int component) {
        return componentDistributions.get(component);
    }

    public List<GenerativeDistribution<T>> getComponentDistributions() {
        return componentDistributions;
    }

    @Override
    public RandomVariable<T[]> sample() {

        int vectorSize = VectorUtils.getVectorSize(params, baseTypes);
        List<RandomVariable> componentVariables = new ArrayList<>();

        for (int i = 0; i < vectorSize; i++) {
            componentVariables.add(getBaseDistribution(i).sample());
        }
        return new VectorizedRandomVariable<>(null, componentVariables, this);
    }

    @Override
    public Map<String, Value> getParams() {
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {

        params.put(paramName, value);

        if (!VectorUtils.isVectorizedParameter(paramName, value, baseTypes)) {
            for (GenerativeDistribution<T> baseDistribution : componentDistributions) {
                // not setInput because the base distributions are hidden from the graphical model
                baseDistribution.setParam(paramName, value);
            }
        } else {
            for (int i = 0; i < componentDistributions.size(); i++) {
                if (value instanceof CompoundVector) {

                    Value componentValue = ((CompoundVector) value).getComponentValue(i);

                    if (componentValue.isAnonymous()) componentValue.setId(paramName + VectorUtils.INDEX_SEPARATOR + i);

                    componentDistributions.get(i).setInput(paramName, componentValue);


                } else {
                    SliceValue sv = new SliceValue(i, value);
                    componentDistributions.get(i).setInput(paramName, sv);
                }
            }
        }
    }

    public String getTypeName() {
        if (componentDistributions.size() > 1)
            return "vector of " + NarrativeUtils.pluralize(componentDistributions.get(0).getTypeName());
        return Generator.getReturnType(this.getClass()).getSimpleName();
    }

    @Override
    public String getRichDescription(int index) {
        return componentDistributions.get(0).getRichDescription(0);
    }

    @Override
    public String getName() {
        return componentDistributions.get(0).getName();
    }

//    public static void main(String[] args) {
//
//        Value a = new Value<>("alpha", 2.0);
//        Value b = new Value<>("beta", 2.0);
//
//        Beta beta = new Beta(a, b);
//
//        Map<String, Value> params = new HashMap<>();
//        params.put("alpha", new Value<>("alpha", new Double[]{200.0, 200.0, 200.0, 3.0, 3.0, 3.0}));
//        params.put("beta", new Value<>("beta", 2.0));
//        Object[] initArgs = {params.get("alpha"), params.get("beta")};
//
//        Constructor constructor = beta.getClass().getConstructors()[0];
//
//        VectorizedDistribution<Double> v = new VectorizedDistribution<>(constructor, Generator.getArguments(constructor), initArgs);
//
//        RandomVariable<Double[]> rbeta = v.sample();
//
//        System.out.println(Arrays.toString(rbeta.value()));
//    }
}
