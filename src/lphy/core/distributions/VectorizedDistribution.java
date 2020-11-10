package lphy.core.distributions;

import lphy.graphicalModel.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static lphy.graphicalModel.VectorUtils.*;

public class VectorizedDistribution<T> implements GenerativeDistribution<T[]> {

    // the parameters (vectors)
    Map<String, Value> params;

    // the base types of the parameters in the base distribution
    // the keys are the argument names
    Map<String, Class> baseTypes = new TreeMap<>();

    List<GenerativeDistribution<T>> componentDistributions;

    public VectorizedDistribution(Constructor baseDistributionConstructor, List<ParameterInfo> pInfo, Object[] vectorArgs) {

        params = Generator.convertArgumentsToParameterMap(pInfo, vectorArgs);

        try {
            int size = getVectorSize(pInfo, vectorArgs);
            componentDistributions = new ArrayList<>(size);
            for (int component = 0; component < size; component++) {
                componentDistributions.add((GenerativeDistribution<T>)getComponentGenerator(baseDistributionConstructor, pInfo, vectorArgs, component));
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

    /**
     * This method is not thread safe!
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

        int vectorSize = getVectorSize(params, baseTypes);
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

        if (!isVectorizedParameter(paramName, value, baseTypes)) {
            for (GenerativeDistribution<T> baseDistribution : componentDistributions) {
                // not setInput because the base distributions are hidden from the graphical model
                baseDistribution.setParam(paramName, value);
            }
        } else {
            for (int i = 0; i < componentDistributions.size(); i++) {
                if (value instanceof CompoundVector) {
                    componentDistributions.get(i).setParam(paramName, ((CompoundVector) value).getComponentValue(i));
                } else  {
                    componentDistributions.get(i).setParam(paramName, new SliceValue(i,value));
                }
            }
        }
    }

    @Override
    public String getRichDescription(int index) {
        return componentDistributions.get(0).getRichDescription(0);
    }

    @Override
    public String getName() {
        return componentDistributions.get(0).getName();
    }

    public static void main(String[] args) {

        Value a = new Value<>("alpha", 2.0);
        Value b = new Value<>("beta", 2.0);

        Beta beta = new Beta(a, b);

        Map<String, Value> params = new HashMap<>();
        params.put("alpha", new Value<>("alpha", new Double[] {200.0, 200.0, 200.0, 3.0, 3.0, 3.0}));
        params.put("beta", new Value<>("beta", 2.0));
        Object[] initArgs = {params.get("alpha"),params.get("beta")};

        Constructor constructor = beta.getClass().getConstructors()[0];

        VectorizedDistribution<Double> v = new VectorizedDistribution<>(constructor, Generator.getParameterInfo(constructor), initArgs);

        RandomVariable<Double[]> rbeta = v.sample();

        System.out.println(Arrays.toString(rbeta.value()));
    }
}
