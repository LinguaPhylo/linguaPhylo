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
    static List<TimeTreeNode> activeNodes;
    static List<String> nameList;
    public final String randomStemAgeName = "randomStemAge";

    public CPPTree(@ParameterInfo(name = BirthDeathConstants.lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                   @ParameterInfo(name = BirthDeathConstants.muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                   @ParameterInfo(name = BirthDeathConstants.rhoParamName, description = "sampling probability") Value<Number> rho,
                   @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "name for passed in taxa", optional = true) Value<String[]> taxa,
                   @ParameterInfo(name = DistributionConstants.nParamName, description = "the total number of taxa.", optional = true) Value<Integer> n,
                   @ParameterInfo(name = BirthDeathConstants.rootAgeParamName, description = "the root age to be conditioned on optional.", optional = true) Value<Number> rootAge,
                   @ParameterInfo(name = randomStemAgeName, description = "the age of stem of the tree root, default has no stem", optional = true)Value<Boolean> randomStemAge) {
        if (birthRate.value().doubleValue() <= deathRate.value().doubleValue()) {
            throw new IllegalArgumentException("The birth rate should be bigger than death rate!");
        }
        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rho = rho;
        this.n = n;
        this.taxa = taxa;
        this.randomStemAge = randomStemAge;
        this.rootAge = rootAge;
    }

    /*
        root conditioned cpp
     */
    @GeneratorInfo(name="CPP",
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
            rootAge = sampleTimes(birthRate, deathRate, rho, 1)[0];
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
        nameList = new ArrayList<>();

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
                ti = sampleTimes(birthRate, deathRate, rho, 0.0, Double.POSITIVE_INFINITY, 1)[0];
            } else {
                ti = sampleTimes(birthRate, deathRate, rho, 0.0, rootAge, 1)[0];
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

        TimeTree tree = mapCPPTree(t);
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

    /*
        main mathematical methods
     */
    public static double CDF(double b, double d, double rho, double t) {
        double p = rho * b * (1 - Math.exp(-(b - d) * t)) / (rho * b + (b * (1 - rho) - d) * Math.exp(-(b - d) * t));
        return p;
    }

    public static double inverseCDF(double b, double d, double rho, double conditionTime, double p) {
        double t = Math.log(1 + ((b - d) * p) / (b * rho * (1 - p))) / (b - d);
        return t;
    }

    public double densityBD(double b, double d, double rho, double time) {
        double density = rho * b * (b - d) * Math.exp(-(b - d) * time) / (rho * b + (b * (1 - rho) - d) * Math.exp(-(b - d) * time));
        return density;
    }

    public static double Qdist(double birthRate, double deathRate, double t, int nSims){
        double p = birthRate *( 1 - Math.exp(- (birthRate - deathRate) * t))/(birthRate - deathRate * Math.exp(-(birthRate - deathRate)* t));
        return Math.pow(p, nSims);
    }

    public static double transform(double p, double birthRate, double deathRate, int nSims) {
        double t = Math.log((deathRate * Math.pow(p, 1 / nSims) - birthRate) / birthRate * (Math.pow(p, 1 / nSims) - 1.0)) / (birthRate - deathRate);
        return t;
    }
    /*
        sampling methods
     */

    // time sampling methods (with condition time optional and lowerTail optional)
    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, double conditionTime, boolean lowerTail, int nSims) {
        // Calculate the CDF value (Q)
        double Q = CDF(birthRate, deathRate, samplingProbability, conditionTime);

        // Random number generator
        Random rand = new Random();

        // Array to store the result
        double[] results = new double[nSims];

        // Generate the samples based on the lowerTail flag
        for (int i = 0; i < nSims; i++) {
            double p;
            if (lowerTail) {
                p = rand.nextDouble() * Q;
            } else {
                p = rand.nextDouble() * (1 - Q) + Q;
            }
            results[i] = inverseCDF(birthRate, deathRate, samplingProbability, conditionTime, p);
        }

        return results;
    }

    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, double conditionTime, int nSims) {
        // Calculate the CDF value (Q)
        double Q = CDF(birthRate, deathRate, samplingProbability, conditionTime);

        // Random number generator
        Random rand = new Random();

        // Array to store the result
        double[] results = new double[nSims];

        // Generate the samples based on the lowerTail flag
        for (int i = 0; i < nSims; i++) {
            double p;
            // default sample from [0,Q], lowerTail=True
            p = rand.nextDouble() * Q;

            results[i] = inverseCDF(birthRate, deathRate, samplingProbability, conditionTime, p);
        }

        return results;
    }

    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, int nSims) {
        // Calculate the CDF value (Q)
        double conditionTime = Double.POSITIVE_INFINITY;

        return sampleTimes(birthRate, deathRate, samplingProbability, conditionTime, nSims);
    }

    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, double lowerTime, double upperTime, int nSims) {
        // Calculate the CDF values at lowerTime and upperTime
        double Qlower = CDF(birthRate, deathRate, samplingProbability, lowerTime);
        double Qupper = CDF(birthRate, deathRate, samplingProbability, upperTime);

        // Random number generator
        Random rand = new Random();

        // Array to store the result
        double[] times = new double[nSims];

        // Generate the samples
        for (int i = 0; i < nSims; i++) {
            // Generate a random probability between Qlower and Qupper
            double p = rand.nextDouble() * (Qupper - Qlower) + Qlower;
            // Use InverseCDF to get the sample time
            times[i] = inverseCDF(birthRate, deathRate, samplingProbability, lowerTime, p);
        }

        return times;
    }

    public static double simRandomStem(double birthRate, double deathRate, double greaterThan, int nTaxa){
        double Q = Qdist(birthRate, deathRate, greaterThan, nTaxa);
        double p = Math.random() * (1.0 - Q) + Q;
        double t = transform(p, birthRate, deathRate, nTaxa);
        return t;
    }

    /*
        tree mapping methods
     */
    public static int indexOfMin(List<Double> t) {
        int minIndex = 0;
        double minValue = t.get(0);
        for (int i = 1; i < t.size(); i++) {
            if (t.get(i) < minValue) {
                minValue = t.get(i);
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static TimeTree mapCPPTree(List<Double> t) {
        List<Double> nodeAges = new ArrayList<>(Collections.nCopies(t.size(), 0.0));

        // map all leaves into activeNodes
        for (String name : nameList){
            // all tips have age 0
            TimeTreeNode leaf = new TimeTreeNode(0.0);
            leaf.setId(name);
            activeNodes = new ArrayList<>();
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

    /*
        getting pass in parameters
     */
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
