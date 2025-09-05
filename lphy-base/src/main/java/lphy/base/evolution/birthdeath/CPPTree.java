package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
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

        // double Q = CDF(birthRate, deathRate, rho, rootAge);
        // initialise a list for node ages
        List<Double> t = new ArrayList<>();
        double conditionAge = 0;

        // determine root age
        double rootAge = 0;
        if (getRootAge() == null) {
            rootAge = CPPUtils.sampleTimes(birthRate, deathRate, rho, 1)[0];
        } else {
            rootAge = getRootAge().value().doubleValue();
        }

        conditionAge = rootAge;

        // determine stem age
        if (getRandomStemAge() != null && getRandomStemAge().value()) { // if random stem age
            conditionAge = simRandomStem(birthRate, deathRate, rho, 1);
        }

        t.add(conditionAge);

        // determine number of taxa and names
        int n = 0;
        List<String> nameList = new ArrayList<>();

        if (getN() != null) {
            n = getN().value();
            if (getTaxa() != null) {
                // check the length
                if (getTaxa().value().length > n) {
                    throw new IllegalArgumentException("taxa must contain at most " + n + " elements");
                } else {
                    for (int i = 0; i < n; i++) {
                        nameList.add(getTaxa().value()[i]);
                    }
                    if (getTaxa().value().length < n) {
                        for (int i = 0; i < n - getTaxa().value().length; i++) {
                            nameList.add(String.valueOf(i));
                        }
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    nameList.add(String.valueOf(i));
                }
            }
        }

        // generate node ages
        int i = 1;
        while (i < n - 1){
            double ti;
            ti = CPPUtils.sampleTimes(birthRate, deathRate, rho, 0, rootAge, 1)[0];

            t.add(ti);
            i++;
        }

        // Insert the root age at a random position
        Random random = new Random();
        int after = random.nextInt(t.size()) + 1; // insert after, at next index
        t.add(after, rootAge);

        // check nTaxa is unassigned
        if (n == 0){
            n = t.size();
            for (int j = 0; j < n; j++) {
                nameList.add(String.valueOf(j));
            }
        }

        // shuffle nameList
        Collections.shuffle(nameList);

        TimeTree tree = mapCPPTree(nameList, t);
        return new RandomVariable<>("CPPTree", tree, this);
    }

    private TimeTree mapCPPTree(List<String> nameList, List<Double> t) {
        List<Double> nodeAges = new ArrayList<>(Collections.nCopies(t.size(), 0.0));

        List<TimeTreeNode> activeNodes = new ArrayList<>();
        // map all leaves into activeNodes
        for (String name : nameList){
            // all tips have age 0
            TimeTreeNode leaf = new TimeTreeNode(0.0);
            leaf.setId(name);
            activeNodes.add(leaf);
        }

        // map the tree
        while (activeNodes.size() > 1){
            // Find the index `j` of the minimum time (earliest event).
            int j = indexOfMin(t);

            // If only two events remain, set `j` to 1 (because when two nodes are left, we combine them into the root).
            if (t.size() == 2){
                j = 1;
            }
            // If the first time is the smallest then set j to 1 (avoid j-1 < 0)
            if (j == 0){
                j = 1;
            }

            // Calculate the branch lengths for the left and right branches of the current split.
            // The branch length is the difference in time between the current event `j` and the time of the nodes being merged.
            TimeTreeNode node1 = activeNodes.get(j-1);
            TimeTreeNode node2 = activeNodes.get(j);
            TimeTreeNode parent = new TimeTreeNode(t.get(j), new TimeTreeNode[]{node1, node2});

            activeNodes.remove(node1);
            activeNodes.remove(node2);
            activeNodes.add(parent);

            nodeAges.remove(node1.getAge());
            nodeAges.add(node2.getAge());
            nodeAges.add(parent.getAge());

            t.remove(t.get(j));
        }

        TimeTree tree = new TimeTree();
        tree.setRoot(activeNodes.get(0));

        // if tree has a stem
        if (t.size() > nodeAges.size()){
            // get a new node for the stem
            TimeTreeNode newRoot = new TimeTreeNode(t.get(0));
            // make the new root origin
            newRoot.addChild(tree.getRoot());
            tree.setRoot(newRoot);
        }
        return tree;
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
