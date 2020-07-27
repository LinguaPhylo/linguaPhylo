package lphy.evolution.birthdeath;

import beast.core.BEASTInterface;
import beast.core.Distribution;
import beast.core.parameter.RealParameter;
import beast.evolution.speciation.BirthDeathGernhard08Model;
import beast.evolution.tree.Tree;
import beast.math.distributions.MRCAPrior;
import beast.math.distributions.Prior;
import lphy.beast.BEASTContext;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A Birth-death-sampling tree generative distribution
 */
public class BirthDeathSamplingTreeDT implements GenerativeDistribution<TimeTree> {

    private final String diversificationParamName;
    private final String turnoverParamName;

    private Value<Double> diversificationRate;
    private Value<Double> turnover;
    private Value<Double> rho;
    private Value<Double> rootAge;

    BirthDeathSamplingTree wrapped;

    public BirthDeathSamplingTreeDT(@ParameterInfo(name = "diversification", description = "diversification rate.") Value<Double> diversification,
                                    @ParameterInfo(name = "turnover", description = "turnover.") Value<Double> turnover,
                                    @ParameterInfo(name = "rho", description = "the sampling proportion.") Value<Double> rho,
                                    @ParameterInfo(name = "rootAge", description = "the number of taxa.") Value<Double> rootAge
                          ) {

        this.turnover = turnover;
        this.diversificationRate = diversification;

        diversificationParamName = getParamName(0);
        turnoverParamName = getParamName(1);
        setup();
    }


    @GeneratorInfo(name="BirthDeathSampling", description="The Birth-death-sampling tree distribution over tip-labelled time trees.<br>" +
            "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        setup();
        RandomVariable<TimeTree> tree = wrapped.sample();
        return new RandomVariable<>("\u03C8", tree.value(), this);
    }

    private void setup() {
        double denom = Math.abs(1.0 - turnover.value());
        double birth_rate = diversificationRate.value() / denom;
        double death_rate = (turnover.value() * diversificationRate.value()) / denom;

        wrapped =
                new BirthDeathSamplingTree(
                        new Value<>("birthRate", birth_rate),
                        new Value<>("deathRate", death_rate),
                        rho, rootAge);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(diversificationParamName, diversificationRate);
        map.put(turnoverParamName, turnover);
        map.put(wrapped.rhoParamName, rho);
        map.put(wrapped.rootAgeParamName, rootAge);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(diversificationParamName)) diversificationRate = value;
        else if (paramName.equals(turnoverParamName)) turnover = value;
        else if (paramName.equals(wrapped.rhoParamName)) rho = value;
        else if (paramName.equals(wrapped.rootAgeParamName)) rootAge = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<Double> getDiversificationRate() {
        return getParams().get(diversificationParamName);
    }

    public Value<Double> getTurnover() {
        return getParams().get(turnoverParamName);
    }

    public Value<Double> getRho() {
        return rho;
    }

    public String toString() {
        return getName();
    }

    public BEASTInterface toBEAST(BEASTInterface tree, BEASTContext context) {
        BirthDeathGernhard08Model beastBirthDeath = new BirthDeathGernhard08Model();
        beastBirthDeath.setInputValue("birthDiffRate", context.getBEASTObject(getDiversificationRate()));
        beastBirthDeath.setInputValue("relativeDeathRate", context.getBEASTObject(getTurnover()));
        beastBirthDeath.setInputValue("sampleProbability", context.getBEASTObject(getRho()));
        beastBirthDeath.setInputValue("type", "labeled");
        beastBirthDeath.setInputValue("conditionalOnRoot", true);
        beastBirthDeath.setInputValue("tree", tree);
        beastBirthDeath.initAndValidate();

        BEASTInterface beastRootAge = context.getBEASTObject(rootAge);
        BEASTInterface beastRootAgeGenerator = context.getBEASTObject(rootAge.getGenerator());

        if (beastRootAge instanceof RealParameter && beastRootAgeGenerator instanceof Prior) {
            RealParameter rootAgeParameter = (RealParameter)beastRootAge;
            Prior rootAgePrior = (Prior)beastRootAgeGenerator;

            MRCAPrior prior = new MRCAPrior();
            prior.setInputValue("distr", rootAgePrior.distInput.get());
            prior.setInputValue("tree", tree);
            prior.setInputValue("taxonset", ((Tree)tree).getTaxonset());
            prior.initAndValidate();
            context.addBEASTObject(prior);
            context.removeBEASTObject(beastRootAge);
            context.removeBEASTObject(beastRootAgeGenerator);
        } else {
            throw new RuntimeException("Can't map BirthDeathSamplingTree.rootAge prior to tree in BEAST conversion.");
        }

        return beastBirthDeath;
    }
}
