package lphy.core.vectorization;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.component.*;
import lphy.core.narrative.Narrative;
import lphy.core.narrative.NarrativeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class IID<T> implements GenerativeDistribution<T[]> {

    // the parameters
    Map<String, Value> params;

    GenerativeDistribution<T> baseDistribution;

    public static final String REPLICATES_PARAM_NAME = "replicates";

    /**
     *
     * @param baseDistributionConstructor the constructor for the base distribution
     * @param initArgs the initial argument values for the base distribution
     * @param params the full param map including replicates parameter
     */
    public IID(Constructor baseDistributionConstructor, Object[] initArgs, Map<String, Value> params) {

        this.params = params;

        try {
            Map<String, Value> elementParams = new HashMap<>();
            params.forEach((key, value) -> {
                if (!key.equals(REPLICATES_PARAM_NAME)) elementParams.put(key, value);
            });

            baseDistribution = (GenerativeDistribution<T>) baseDistributionConstructor.newInstance(initArgs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            LoggerUtils.log.severe("Cannot create instance of " + baseDistributionConstructor.getName() +
                    ", check if parameters are valid : " + Arrays.toString(initArgs));
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param arguments
     * @param initargs
     * @return true if the initargs matches the list of arguments with the addition of a valid "replicates" argument
     */
    public static boolean match(Constructor constructor, List<Argument> arguments, Object[] initargs, Map<String, Value> params) {

        // the base distribution must be a generative distribution
        if (!(GenerativeDistribution.class.isAssignableFrom(constructor.getDeclaringClass()))) return false;

        // there must be a valid value in replicates parameter
        if (!hasValidReplicatesParam(constructor.getDeclaringClass().getSimpleName(), params))
            return false;

        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            Value argValue = (Value) initargs[i];

            // if one of the arguments of the base distribution is replicates than you can't use IID on this base distribution
            if (argument.name.equals(IID.REPLICATES_PARAM_NAME)) {
                return false;
            }

            // it is not a match unless all required arguments are provided
            if (argValue == null && !argument.optional) {
                return false;

            } else
                // types must match
                if (argValue !=null && !argument.type.isAssignableFrom(argValue.value().getClass())) {
                    return false;
                }

        }
        return true;
    }

    // validate if the argument "replicates" is assigned correctly
    private static boolean hasValidReplicatesParam(String constructorName, Map<String, Value> params) {
        Value value = params.get(REPLICATES_PARAM_NAME);
        if (value == null)
            return false;
        if (value.getType() != Integer.class)
            throw new IllegalArgumentException("The parameter '" + REPLICATES_PARAM_NAME +
                    "' must be an integer in " + constructorName +
                    " ! But it is " + (value == null ? value : value.value() + " (" + value.getType() + ")") );
        else if (((Integer) value.value()) < 0){
            throw new IllegalArgumentException("The parameter '" + REPLICATES_PARAM_NAME +
                    "' must >= 0 in " + constructorName + " ! But it is " + value.value());
        }
        return true;
    }

    public int size() {
        return (Integer) params.get(REPLICATES_PARAM_NAME).value();

    }

    public String getInferenceStatement(Value value, Narrative narrative) {

        if (value instanceof VectorizedRandomVariable) {

            VectorizedRandomVariable vrv = (VectorizedRandomVariable) value;
            StringBuilder builder = new StringBuilder();

            builder.append(narrative.product("i", "0", getReplicatesTo(narrative)));

            Generator generator = baseDistribution;

            Value v;
            if (vrv.size() < 1)
                v = vrv;
            else
                v = vrv.getComponentValue(0);

            String componentStatement = generator.getInferenceStatement(v, narrative);

            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "0", narrative.subscript("i"));
            componentStatement = componentStatement.replaceAll(VectorUtils.INDEX_SEPARATOR + "\\{0}", narrative.subscript("i"));

            builder.append(componentStatement);

            return builder.toString();
        }
        throw new RuntimeException("Expected VectorizedRandomVariable!");
    }

    public String getInferenceNarrative(Value value, boolean unique, Narrative narrative) {

        if (value instanceof VectorizedRandomVariable) {
            VectorizedRandomVariable vrv = (VectorizedRandomVariable) value;

            StringBuilder builder = new StringBuilder();

            Generator generator = baseDistribution;

            Value v;
            if (vrv.size() < 1)
                return ""; //TODO narrative for empty array
            else
                v = vrv.getComponentValue(0);

            String inferenceNarrative = generator.getInferenceNarrative(v, unique, narrative);

            if (size() > 1)
                inferenceNarrative = inferenceNarrative.replace(narrative.subscript("0"), narrative.subscript("i"));
            if (inferenceNarrative.endsWith(".")) {
                inferenceNarrative = inferenceNarrative.substring(0, inferenceNarrative.lastIndexOf('.'));
            }

            builder.append(inferenceNarrative);

            if (size() > 1)
                builder.append(getReplicatesNarrativeClause(narrative));

            builder.append(".");
            return builder.toString();
        }
        throw new RuntimeException("Expected VectorizedRandomVariable!");
    }

    public String getReplicatesTo(Narrative narrative) {
        Value replicatesValue = getParams().get(REPLICATES_PARAM_NAME);
        String replicateToString = "";

        if (replicatesValue.isAnonymous()) {
            replicateToString = (size() - 1) + "";
        } else {
            replicateToString = narrative.getId(replicatesValue, false) + " - 1";
        }
        return replicateToString;
    }

    public String getReplicatesNarrativeClause(Narrative narrative) {
        return ", for i in 0 to " + getReplicatesTo(narrative);
    }

    /**
     * @param value
     * @return the narrative name for the given value, being a parameter of this generator.
     */
    public String getNarrativeName(Value value) {
        String paramName = getParamName(value);
        if (paramName.equals(REPLICATES_PARAM_NAME)) return "number of replicates";
        return baseDistribution.getNarrativeName(paramName);
    }

    public GenerativeDistribution<T> getBaseDistribution() {
        return baseDistribution;
    }

    @Override
    public RandomVariable<T[]> sample() {

        int size = size();
        List<RandomVariable> componentVariables = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            componentVariables.add(baseDistribution.sample());
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

        if (!paramName.equals(REPLICATES_PARAM_NAME)) {
            // not setInput because the base distributions are hidden from the graphical model
            baseDistribution.setParam(paramName, value);
        }
    }

    public String getTypeName() {
        if (size() > 1)
            return "vector of " + NarrativeUtils.pluralize(baseDistribution.getTypeName());
        return Generator.getReturnType(this.getClass()).getSimpleName();
    }

    @Override
    public String getRichDescription(int index) {
        return baseDistribution.getRichDescription(index);
    }

    @Override
    public String getName() {
        return baseDistribution.getName();
    }

    public Value<Integer> getReplicates() {
        return getParams().get(REPLICATES_PARAM_NAME);
    }


//    public static void main(String[] args) {
//
//        Value a = new Value<>("alpha", 2.0);
//        Value b = new Value<>("beta", 2.0);
//
//        Beta beta = new Beta(a, b);
//
//        Map<String, Value> params = new HashMap<>();
//        params.put("alpha", new Value<>("alpha", 2.0));
//        params.put("beta", new Value<>("beta", 2.0));
//        params.put(replicatesParamName, new IntegerValue(null, 3));
//        Object[] initArgs = {params.get("alpha"), params.get("beta")};
//
//        Constructor constructor = beta.getClass().getConstructors()[0];
//
//        IID<Double> v = new IID<>(constructor,  initArgs, params);
//
//        RandomVariable<Double[]> rbeta = v.sample();
//
//        System.out.println(Arrays.toString(rbeta.value()));
//    }
}
