package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;
import static lphy.base.evolution.birthdeath.CPPUtils.*;

@Citation(value = "Lambert, A., & Stadler, T. (2013). Birth-death models and coalescent point processes: the shape and probability of reconstructed phylogenies. Theoretical population biology, 90, 113–128. https://doi.org/10.1016/j.tpb.2013.10.002",
        title = "Birth–death models and coalescent point processes: The shape and probability of reconstructed phylogenies",
        DOI = "10.1016/j.tpb.2013.10.002", authors = {"Lambert","Stadler"}, year = 2013)
// TODO: needs validation
public class CPPTree implements GenerativeDistribution<TimeTree>{
    Value<Number> rootAge;
    Value<Number> birthRate;
    Value<Number> deathRate;
    Value<Number> rho;
    Value<Integer> n;
    Value<String[]> taxa;
    Value<Boolean> randomStemAge;
    public final String randomStemAgeName = "randomStemAge";

    public CPPTree(@ParameterInfo(name = BirthDeathConstants.lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                   @ParameterInfo(name = BirthDeathConstants.muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                   @ParameterInfo(name = BirthDeathConstants.rhoParamName, description = "sampling probability") Value<Number> rho,
                   @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "name for passed in taxa", optional = true) Value<String[]> taxa,
                   @ParameterInfo(name = DistributionConstants.nParamName, description = "the total number of taxa.", optional = true) Value<Integer> n,
                   @ParameterInfo(name = BirthDeathConstants.rootAgeParamName, description = "the root age to be conditioned on optional.", optional = true) Value<Number> rootAge,
                   @ParameterInfo(name = randomStemAgeName, description = "the age of stem of the tree root, default has no stem", optional = true)Value<Boolean> randomStemAge) {

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
        this.n = n;
        this.taxa = taxa;
        this.randomStemAge = randomStemAge;
        this.rootAge = rootAge;
    }

    @GeneratorInfo(name="CPP", examples = {"CPPTree.lphy"},
        description = "Generate a tree with coalescent point processing progress with node ages drawn i.i.d and factorised. If a root age is provided, the method conditions the tree generation on this root age.")
    @Override
    public RandomVariable<TimeTree> sample() {
        double birthRate = getBirthRate().value().doubleValue();
        double deathRate = getDeathRate().value().doubleValue();
        double rho = getSamplingProbability().value().doubleValue();

        // initialise a list for node ages
        List<Double> t = new ArrayList<>();

        // determine root age
        double rootAge = 0;
        if (getRootAge() == null) {
            rootAge = CPPUtils.sampleTimes(birthRate, deathRate, rho, 1)[0];
        } else {
            rootAge = getRootAge().value().doubleValue();
        }

        // double Q = CDF(birthRate, deathRate, rho, rootAge);

        // determine stem age
        double stemAge = 0;
        if (getRandomStemAge() != null) {
            stemAge = simRandomStem(birthRate, deathRate, rootAge, 1);
            t.add(stemAge);
        } else {
            t.add(rootAge);
        }

        // determine number of taxa and names
        int n = 0;
        List<String> nameList = new ArrayList<>();

        if (getN() != null) {
            n = getN().value().intValue();
            if (getTaxa() != null) {
                for (int i = 0; i < n; i++) {
                    nameList.add(getTaxa().value()[i]);
                }
            } else {
                for (int i = 0; i < n; i++) {
                    nameList.add(String.valueOf(i));
                }
            }
        } else {
            n = (int) Double.POSITIVE_INFINITY;
        }

        // generate node ages
        int i = 1;
        while (i < n - 1){
            double ti;
            if (n == (int) Double.POSITIVE_INFINITY){
                ti = CPPUtils.sampleTimes(birthRate, deathRate, rho, 0.0, Double.POSITIVE_INFINITY, 1)[0];
            } else {
                ti = CPPUtils.sampleTimes(birthRate, deathRate, rho, 0.0, rootAge, 1)[0];
            }

            if (n == (int) Double.POSITIVE_INFINITY && ti > rootAge) {
                break;
            }

            t.add(ti);
            i++;
        }

        // Insert the root age at a random position
        Random random = new Random();
        int after = random.nextInt(t.size()) + 1; // insert after, at next index
        t.add(after, rootAge);

        // check nTaxa is infinite
        if (n == (int) Double.POSITIVE_INFINITY){
            n = t.size();
            for (int j = 0; j < n; j++) {
                nameList.add(String.valueOf(j));
            }
        }

        // shuffle nameList
        Collections.shuffle(nameList);

        TimeTree tree = CPPUtils.mapCPPTree(nameList, t);
        return new RandomVariable<>("CPPTree", tree, this);
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = new TreeMap<>();
        map.put(BirthDeathConstants.lambdaParamName, birthRate);
        map.put(BirthDeathConstants.muParamName, deathRate);
        map.put(BirthDeathConstants.rhoParamName, rho);
        if (rootAge != null) map.put(BirthDeathConstants.rootAgeParamName, rootAge);
        if (n != null) map.put(DistributionConstants.nParamName,n);
        if (taxa != null) map.put(TaxaConditionedTreeGenerator.taxaParamName,taxa);
        if (randomStemAge != null) map.put(randomStemAgeName, randomStemAge);
        return map;
    }

    public void setParam(String paramName, Value value){
        if (paramName.equals(BirthDeathConstants.lambdaParamName)) birthRate = value;
        else if (paramName.equals(BirthDeathConstants.muParamName)) deathRate = value;
        else if (paramName.equals(BirthDeathConstants.rhoParamName)) rho = value;
        else if (paramName.equals(BirthDeathConstants.rootAgeParamName)) rootAge = value;
        else if (paramName.equals(DistributionConstants.nParamName)) n = value;
        else if (paramName.equals(TaxaConditionedTreeGenerator.taxaParamName)) taxa = value;
        else if (paramName.equals(randomStemAgeName)) randomStemAge = value;
        else setParam(paramName, value);
    }

    public Value<Integer> getN(){
        return getParams().get(DistributionConstants.nParamName);
    }
    public Value<Number> getBirthRate(){
        return getParams().get(BirthDeathConstants.lambdaParamName);
    }
    public Value<Number> getDeathRate(){
        return getParams().get(BirthDeathConstants.muParamName);
    }
    public Value<Number> getSamplingProbability(){
        return getParams().get(BirthDeathConstants.rhoParamName);
    }
    public Value<Number> getRootAge(){
        return getParams().get(BirthDeathConstants.rootAgeParamName);
    }
    public Value<Boolean> getRandomStemAge(){
        return getParams().get(randomStemAgeName);
    }
    public Value<String[]> getTaxa(){
        return getParams().get(TaxaConditionedTreeGenerator.taxaParamName);
    }
}
