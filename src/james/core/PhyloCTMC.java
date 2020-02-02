package james.core;

import james.TimeTree;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCTMC implements GenerativeDistribution<Alignment> {

    Value<TimeTree> tree;
    Value<Double[][]> Q;
    Random random;

    String treeParamName;
    String QParamName;

    public PhyloCTMC(@ParameterInfo(name = "tree", description = "the time tree..") Value<TimeTree> tree,
                      @ParameterInfo(name = "Q", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
                      Random random) {
        this.tree = tree;
        this.Q = Q;
        this.random = random;

        treeParamName = getParamName(0);
        QParamName = getParamName(1);
    }

    public RandomVariable<Alignment> sample() {
        // TODO simulate an alignment
        return null;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(QParamName, Q);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(QParamName)) Q = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }
}
