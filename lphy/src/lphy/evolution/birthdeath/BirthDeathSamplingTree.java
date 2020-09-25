package lphy.evolution.birthdeath;

import lphy.core.distributions.Utils;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * A Birth-death tree generative distribution
 */
public class BirthDeathSamplingTree implements GenerativeDistribution<TimeTree> {

    final static String birthRateParamName = "lambda";
    final static String deathRateParamName = "mu";
    final static String rhoParamName = "rho";
    final static String rootAgeParamName = "rootAge";
    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rho;
    private Value<Number> rootAge;

    RandomGenerator random;

    public BirthDeathSamplingTree(@ParameterInfo(name = birthRateParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                                  @ParameterInfo(name = deathRateParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                                  @ParameterInfo(name = rhoParamName, description = "the sampling proportion.") Value<Number> rho,
                                  @ParameterInfo(name = rootAgeParamName, description = "the age of the root of the tree.") Value<Number> rootAge) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
        this.rootAge = rootAge;
        this.random = Utils.getRandom();
    }


    @GeneratorInfo(name="BirthDeathSampling", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        FullBirthDeathTree birthDeathTree = new FullBirthDeathTree(birthRate, deathRate, rootAge);
        RandomVariable<TimeTree> fullTree = birthDeathTree.sample();

        RhoSampleTree rhoSampleTree = new RhoSampleTree(fullTree, rho);

        return rhoSampleTree.sample();
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(birthRateParamName, birthRate);
        map.put(deathRateParamName, deathRate);
        map.put(rhoParamName, rho);
        map.put(rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(birthRateParamName)) birthRate = value;
        else if (paramName.equals(deathRateParamName)) deathRate = value;
        else if (paramName.equals(rhoParamName)) rho = value;
        else if (paramName.equals(rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
