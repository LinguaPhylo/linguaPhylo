package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.PruneTree;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static lphy.evolution.birthdeath.BirthDeathConstants.*;

/**
 * A Birth-death tree generative distribution
 */
public class SimFBDAge implements GenerativeDistribution<TimeTree> {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> psiVal;
    private Value<Double> fracVal;
    private Value<Number> originAge;

    RandomGenerator random;

    private static final int MAX_ATTEMPTS = 1000;

    public SimFBDAge(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                     @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                     @ParameterInfo(name = fracParamName, description = "fraction of extant taxa sampled.") Value<Double> fracVal,
                     @ParameterInfo(name = psiParamName, description = "per-lineage sampling-through-time rate.") Value<Number> psiVal,
                     @ParameterInfo(name = originAgeParamName, description = "the age of the origin.") Value<Number> originAge) {


        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.fracVal = fracVal;
        this.psiVal = psiVal;
        this.originAge = originAge;

        random = Utils.getRandom();
    }

    @GeneratorInfo(name = "SimFBDAge", description = "A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>" +
            "Conditioned on origin age.")
    public RandomVariable<TimeTree> sample() {

        int nonNullLeafCount = 0;
        TimeTree sampleTree = null;

        int attempts = 0;

        while (nonNullLeafCount < 1 && attempts < MAX_ATTEMPTS) {
            FullBirthDeathTree birthDeathTree = new FullBirthDeathTree(birthRate, deathRate, null, originAge);
            RandomVariable<TimeTree> fullTree = birthDeathTree.sample();

            SimFossilsPoisson simFossilsPoisson = new SimFossilsPoisson(fullTree, psiVal);

            Value<TimeTree> fullTreeWithFossils = simFossilsPoisson.sample();

            sampleTree = new TimeTree(fullTreeWithFossils.value());

            List<TimeTreeNode> leafNodes = new ArrayList<>();

            for (TimeTreeNode node : sampleTree.getNodes()) {
                if (node.isLeaf() && node.getAge() == 0.0) {
                   leafNodes.add(node);
                }
            }

            int toNull = (int)Math.round(leafNodes.size()* (1.0-fracVal.value()));
            List<TimeTreeNode> nullList = new ArrayList<>();
            for (int i =0; i < toNull; i++) {
                nullList.add(leafNodes.remove(random.nextInt(leafNodes.size())));
            }
            for (TimeTreeNode node : nullList) {
                node.setId(null);
            }

            nonNullLeafCount = leafNodes.size();
            attempts += 1;
        }

        if (attempts == MAX_ATTEMPTS) throw new RuntimeException("Failed to simulate SimFBDAge after " + MAX_ATTEMPTS + " attempts.");

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
            put(fracParamName, fracVal);
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
            case fracParamName:
                fracVal = value;
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

    public Value<Double> getRho() {
        return fracVal;
    }

    public Value<Number> getPsi() {
        return psiVal;
    }
}