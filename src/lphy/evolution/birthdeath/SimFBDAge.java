package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.PruneTree;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.evolution.birthdeath.BirthDeathConstants.*;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Birth-death tree generative distribution
 */
public class SimFBDAge implements GenerativeDistribution<TimeTree> {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> psiVal;
    private Value<Number> rhoVal;
    private Value<Number> originAge;

    RandomGenerator random;

    public SimFBDAge(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                     @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                     @ParameterInfo(name = rhoParamName, description = "proportion of extant taxa sampled.") Value<Number> rhoVal,
                     @ParameterInfo(name = psiParamName, description = "per-lineage sampling-through-time rate.") Value<Number> psiVal,
                     @ParameterInfo(name = originAgeParamName, description = "the age of the origin.") Value<Number> originAge) {


        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rhoVal = rhoVal;
        this.psiVal = psiVal;
        this.originAge = originAge;

        random = Utils.getRandom();
    }

    @GeneratorInfo(name = "SimFBDAge", description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        int nonNullLeafCount = 0;
        TimeTree sampleTree = null;

        while (nonNullLeafCount < 2) {
            FullBirthDeathTree birthDeathTree = new FullBirthDeathTree(birthRate, deathRate, null, originAge);
            RandomVariable<TimeTree> fullTree = birthDeathTree.sample();

            SimFossilsPoisson simFossilsPoisson = new SimFossilsPoisson(fullTree, psiVal);

            Value<TimeTree> fullTreeWithFossils = simFossilsPoisson.sample();

            sampleTree = new TimeTree(fullTreeWithFossils.value());
            double p = doubleValue(rhoVal);

            for (TimeTreeNode node : sampleTree.getNodes()) {
                if (node.isLeaf() && node.getAge() == 0.0) {

                    // in later prune tree step null id tips will be pruned.
                    if (random.nextDouble() > p) {
                        node.setId(null);
                    }
                }
            }
            nonNullLeafCount = 0;
            for (TimeTreeNode node : sampleTree.getNodes()) {
                if (node.isLeaf() && node.getId() != null) {
                    nonNullLeafCount += 1;
                }
            }
        }
        PruneTree pruneTree = new PruneTree(new Value<>(null, sampleTree));

        TimeTree tree = pruneTree.apply().value();
        return new RandomVariable<>(null, tree, this);
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(lambdaParamName, birthRate);
            put(muParamName, deathRate);
            put(rhoParamName, rhoVal);
            put(psiParamName, psiVal);
            put(originAgeParamName, originAge);
        }};
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
            case rhoParamName:
                rhoVal = value;
                break;
            case psiParamName:
                psiVal = value;
                break;
            case originAgeParamName:
                originAge = value;
                break;
            default:
                throw new RuntimeException("Unexpected parameter " + paramName);
        }
    }

    public Value<Number> getBirthRate() {
        return birthRate;
    }

    public Value<Number> getDeathRate() {
        return deathRate;
    }

    public Value<Number> getRho() {
        return rhoVal;
    }

    public Value<Number> getPsi() {
        return psiVal;
    }
}