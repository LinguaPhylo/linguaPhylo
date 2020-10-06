package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;

import java.util.*;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathTree extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rootAge;

    public BirthDeathTree(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                          @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                          @ParameterInfo(name = nParamName, description = "the number of taxa. optional.", optional = true) Value<Integer> n,
                          @ParameterInfo(name = taxaParamName, description = "a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.", optional = true) Value taxa,
                          @ParameterInfo(name = rootAgeParamName, description = "the age of the root.") Value<Number> rootAge) {

        super(n, taxa, null);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();

        checkTaxaParameters(true);
    }

    @GeneratorInfo(name = "BirthDeath", description = "A tree of only extant species, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on root age and on number of taxa.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();
        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);

        double lambda = doubleValue(birthRate);
        double mu = doubleValue(deathRate);

        double t = doubleValue(rootAge);

        double[] times = new double[activeNodes.size() - 1];

        for (int i = 0; i < times.length - 1; i++) {
            times[i] = birthDeathInternalQ(random.nextDouble(), lambda, mu, t);
        }
        times[times.length - 1] = t;
        Arrays.sort(times);

        for (int i = 0; i < times.length; i++) {
            TimeTreeNode a = drawRandomNode(activeNodes);
            TimeTreeNode b = drawRandomNode(activeNodes);
            TimeTreeNode parent = new TimeTreeNode(times[i], new TimeTreeNode[]{a, b});
            activeNodes.add(parent);
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private double birthDeathInternalQ(double p, double lambda, double mu, double rootHeight) {

        if (lambda == mu) return p * rootHeight / (1 + lambda * rootHeight * (1 - p));
        double h = lambda - mu;
        double e1 = Math.exp(-h * rootHeight);
        double a1 = lambda - mu * e1;
        double a2 = p * (1 - e1);
        return (1 / h) * Math.log((a1 - mu * a2) / (a1 - lambda * a2));
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(lambdaParamName, birthRate);
        map.put(muParamName, deathRate);
        map.put(rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case lambdaParamName:
                birthRate = value;
                break;
            case muParamName:
                deathRate = value;
                break;
            case rootAgeParamName:
                rootAge = value;
                break;
            default:
                super.setParam(paramName, value);
                break;
        }
    }
}
