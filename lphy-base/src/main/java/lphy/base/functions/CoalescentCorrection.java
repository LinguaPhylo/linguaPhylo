package lphy.base.functions;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.components.*;

import java.util.SortedMap;
import java.util.TreeMap;

public class CoalescentCorrection extends DeterministicFunction<Double[][]> {

    private final String treeParamName;
    private final String thetaParamName;
    private Value<TimeTree> speciesTree;
    private Value<Double[]> theta;

    public CoalescentCorrection(@ParameterInfo(name = "speciesTree", description = "the species tree.") Value<TimeTree> speciesTree,
                                @ParameterInfo(name = "populationSizes", description = "the population sizes for each branch of the species tree.") Value<Double[]> theta) {

        this.speciesTree = speciesTree;
        this.theta = theta;

        if (speciesTree.value().n()*2-1 != theta.value().length) {
            throw new RuntimeException("Must have one population size for each branch of species tree.");
        }

        treeParamName = getParamName(0);
        thetaParamName = getParamName(1);
    }

    @GeneratorInfo(name = "coalescentCorrection",
            category = GeneratorCategory.COAL_TREE,
            description = "Constructs the expected variance-covariance matrix of a gene tree from given species tree and population sizes.")
    public Value<Double[][]> apply() {

        // TODO implement Fabio's coalescent correction
        return null;
    }

    public java.util.Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, speciesTree);
        map.put(thetaParamName, theta);
        return map;
    }

    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) speciesTree = value;
        else if (paramName.equals(thetaParamName)) theta = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

}