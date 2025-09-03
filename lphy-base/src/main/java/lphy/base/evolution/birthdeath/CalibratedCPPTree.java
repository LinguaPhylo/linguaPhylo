package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;
import static lphy.base.evolution.birthdeath.CPPUtils.*;

public class CalibratedCPPTree extends TaxaConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {

    Value<Number> rootAge;
    Value<Number> birthRate;
    Value<Number> deathRate;
    Value<Number[]> cladeMRCAAge;
    Value<String[][]> cladeTaxa;
    Value<Number> stemAge;
    Value<Number> rho;
    Value<String[]> otherNames;

    List<String> nameList;
    public static final String cladeMRCAAgeName = "cladeMRCAAge";
    public static final String cladeTaxaName = "cladeTaxa";
    public static final String stemAgeName = "stemAge";
    public static final String otherTaxaNames = "otherNames";

    public CalibratedCPPTree(@ParameterInfo(name = BirthDeathConstants.lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                             @ParameterInfo(name = BirthDeathConstants.muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                             @ParameterInfo(name = BirthDeathConstants.rhoParamName, description = "sampling probability") Value<Number> rho,
                             @ParameterInfo(name = DistributionConstants.nParamName, description = "the total number of taxa.") Value<Integer> n,
                             @ParameterInfo(name = cladeTaxaName, description = "a string array of taxa id or a taxa object for clade taxa (e.g. dataframe, alignment or tree)") Value<String[][]> cladeTaxa,
                             @ParameterInfo(name = cladeMRCAAgeName, description = "an array of ages for clade most recent common ancestor, ages should be correspond with clade taxa array.") Value<Number[]> cladeMRCAAge,
                             @ParameterInfo(name = otherTaxaNames, description = "a string array of taxa names for non-calibrated tips", optional = true) Value<String[]> otherNames,
                             @ParameterInfo(name = BirthDeathConstants.rootAgeParamName, description = "the root age to be conditioned on optional.", optional = true) Value<Number> rootAge,
                             @ParameterInfo(name = stemAgeName, description = "the age of stem of the tree root", optional = true) Value<Number> stemAge) {
        super(n, null, null);
        // check legal params
        if (cladeTaxa == null) throw new IllegalArgumentException("The clade taxa shouldn't be null, otherwise please use CPP");
        if (cladeMRCAAge == null) throw new IllegalArgumentException("The clade mrca age shouldn't be null!");
        if (cladeTaxa.value().length != cladeMRCAAge.value().length) {
            throw new IllegalArgumentException("The clade mrca age should correspond to the clade taxa!");
        }
        if (rootAge != null & stemAge != null) {
            LoggerUtils.log.warning("Stem age will be ignored when root age is provided.");
        }
        for (int i = 0; i < cladeMRCAAge.value().length; i++) {
            if (rootAge != null && rootAge.value().doubleValue() < cladeMRCAAge.value()[i].doubleValue()) {
                throw new IllegalArgumentException("The clade MRCA age shouldn't be smaller than the tree root age!");
            }
        }
        this.rho = rho;
        this.cladeTaxa = cladeTaxa;
        this.cladeMRCAAge = cladeMRCAAge;
        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rootAge = rootAge;
        this.otherNames = otherNames;
        this.stemAge = stemAge;
    }

    @GeneratorInfo(name = "CalibratedCPP", examples = {"CalibratedCPPTree.lphy"},
            description = "The CalibratedCCP method accepts one or more clade taxa and generates a tip-labelled time tree. If a root age is provided, the method conditions the tree generation on this root age.")
    @Override
    public RandomVariable<TimeTree> sample() {
        // obtain pass in parameters
        double birthRate = getBirthRate().value().doubleValue();
        double deathRate = getDeathRate().value().doubleValue();
        double samplingProb = getSamplingProb().value().doubleValue();
        int n = getN().value().intValue();
        String[][] cladeTaxaNames = getCladeTaxa().value();
        Number[] cladeAges = getCladeMRCAAge().value();

        // initialise params
        double rootAge = 0;
        TimeTree tree = new TimeTree();
        boolean rootConditioned = false;
        List<String> backUpNames = new ArrayList<>();

        // step1: get valid clade calibrations
        TreeMap<Double, String[]> cladeCalibrations = new TreeMap<>();
        for (int i = 0; i< cladeTaxaNames.length; i++) {
            cladeCalibrations.put(cladeAges[i].doubleValue(), cladeTaxaNames[i]);
        }

        // if root age given, then make it rootConditioned
        if (getRootAge() != null) {
            rootAge = getRootAge().value().doubleValue();
            rootConditioned = true;
        }

        // if root calibration is already in clade calibration
        if (cladeCalibrations.lastEntry().getValue().length == n){
            rootConditioned = true;
            // if calibration conflict with rootAge
            if (rootAge != 0 &&  cladeCalibrations.lastEntry().getKey() != rootAge){
                throw new IllegalArgumentException("The calibrated root age should be the same as the root age!");
            } else {
                // if only one root calibration, then return cpp
                if (cladeCalibrations.size() == 1){
                    CPPTree cpp = new CPPTree(getBirthRate(), getDeathRate(), getSamplingProb(),
                            new Value<>("", cladeCalibrations.lastEntry().getValue()), getN(), new Value<>("", cladeCalibrations.lastKey()), null);
                    tree = cpp.sample().value();
                    return new RandomVariable<>("", tree, this);
                } else {
                    // else specify rootAge and remove the root calibration from cladeCalibrations
                    rootAge = cladeCalibrations.lastKey();
                    for (String name : cladeCalibrations.lastEntry().getValue()) {
                        backUpNames.add(name);
                    }
                    cladeCalibrations.remove(cladeCalibrations.lastEntry().getKey());
                }
            }
        }

        // step2: get all maximal calibration
        TreeMap<Double, String[]> maximalCalibrations = getMaximalCalibrations(cladeCalibrations);

        // map the taxa names for calibration clades
        int index = 0;
        int cladeSizes = 0;
        String[][] taxaNames = new String[maximalCalibrations.size()][];
        for (Map.Entry<Double, String[]> entry : maximalCalibrations.entrySet()) {
            taxaNames[index] = entry.getValue();
            cladeSizes += taxaNames[index].length;
            index++;
        }

        // calculate the number of nodes
        // m = non-clade tips + clade roots
        int m = n - cladeSizes + maximalCalibrations.size();

        /* initialise the lists
            A : holding the indices of inactive nodes (wait for assign)
            l : holding the indices of active nodes (has assigned)
            times : holding the times of internal nodes, the first element is root or stem age
            nodeAges : holding the times for each node
            nodeList : holding all nodes
         */
        List<Integer> A = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            A.add(i);
        }
        int[] l = new int[m];
        List<Double> times = new ArrayList<>(Collections.nCopies(m, 0.0));
        List<Double> nodeAges = new ArrayList<>(Collections.nCopies(m, 0.0));
        List<TimeTreeNode> nodeList = new ArrayList<>((Collections.nCopies(A.size(), null)));

        // step3: calculate condition age (root or stem age)
        // if rootConditioned, then condition on root
        // if !rootConditioned, then use stem age or sample one
        double conditionAge = 0.0;
        if (rootConditioned) {
            int ind;
            ind = random.nextInt(m - 1) + 1; // [1, m-1]
            if (m == 2){
                ind = 1;
            }
            times.set(ind, rootAge);
            conditionAge = rootAge;
        } else {
            if (getStemAge()!= null) {
                conditionAge = getStemAge().value().doubleValue();
            } else {
                conditionAge = simRandomStem(birthRate, deathRate, maximalCalibrations.lastEntry().getKey(), n);
            }
        }

        // step4: build clades for each maximalCalibrations
        List<Map.Entry<Double, String[]>> maximalCalibrationsEntries = new ArrayList<>(maximalCalibrations.entrySet());
        List<Map.Entry<Double, String[]>> cladeCalibrationsEntries = new ArrayList<>(cladeCalibrations.entrySet());
        nameList = new ArrayList<>(n);

        // set name list
        // TODO: check if name provided overlap the automatic names make them have prefix
        for (int i = 0; i< maximalCalibrations.size(); i++) {
            String[] uniqueNames = new String[maximalCalibrationsEntries.get(i).getValue().length];
            for (int j = 0; j< maximalCalibrationsEntries.get(i).getValue().length; j++) {
                //String newName = "clade" + i + "_" + maximalCalibrationsEntries.get(i).getValue()[j];
                String newName = maximalCalibrationsEntries.get(i).getValue()[j];

                nameList.add(newName);
                uniqueNames[j] = newName;
                backUpNames.remove(uniqueNames[j]);
            }
            maximalCalibrations.put(maximalCalibrationsEntries.get(i).getKey(), uniqueNames);
        }

        // loop through all maximalCalibrations
        for (int i = 0; i < maximalCalibrations.size(); i++) {
            // step1: get subclades
            // get the clade at index i
            TreeMap<Double, String[]> clade = new TreeMap<>();
            clade.put(maximalCalibrationsEntries.get(i).getKey(), maximalCalibrationsEntries.get(i).getValue());
            // get nested clades
            TreeMap<Double, String[]> subClades = getNestedClades(clade, cladeCalibrations);
            List<Map.Entry<Double, String[]>> subCladeEntries = new ArrayList<>(subClades.entrySet());

            // step2: get sampled element
            // calculate weights
            double w = CDF(birthRate, deathRate, samplingProb, conditionAge) -
                    CDF(birthRate, deathRate, samplingProb, cladeCalibrationsEntries.get(i).getKey());
            // calculate score s for each node
            double[] s = calculateScore(A, m, times);
            // calculate weight for each node
            double[] weights = getWeights(s, w);
            if (A.size() == 1){
                l[i] = A.get(0);
            } else {
                // sample one element from A with probability weights
                l[i] = sampleElement(A, weights);
            }

            // step3: construct subtrees
            // construct calibration clade leaf names
            String[][] cladeNames = new String[subClades.size()][];
            Double[] cladeMRCAAges = new Double[subClades.size()];
            for (int j = 0; j < subClades.size(); j++) {
                String[] taxa = subCladeEntries.get(j).getValue();
                cladeNames[j] = new String[taxa.length];
                for (int k = 0; k < taxa.length; k++) {
                    cladeNames[j][k] = taxa[k];
                }
                cladeMRCAAges[j] = subCladeEntries.get(j).getKey();
            }

            // simulate a tree for these clades, only offer calibrations
            CalibratedCPPTree calibratedCPPTree = new CalibratedCPPTree(getBirthRate(),
                    getDeathRate(), getSamplingProb(),
                    new Value<>("", maximalCalibrationsEntries.get(i).getValue().length),
                    new Value<>("", cladeNames),
                    new Value<>("", cladeMRCAAges), null, null, null);

            // put clade mrca into a list waiting for assign
            TimeTree subTree = calibratedCPPTree.sample().value();
            nodeList.set(l[i], subTree.getRoot());

            // step4: assign unresolved node times for l[i]
            // once done, remove l[i] from list A
            // deal with the nodes have l[i] still 0
            if (times.get(l[i]) == 0) {
                double time = sampleTimes(birthRate, deathRate, samplingProb, maximalCalibrationsEntries.get(i).getKey(), conditionAge, 1)[0];
                times.set(l[i],time);
            }

            if (l[i] < m -1  && times.get(l[i] + 1) == 0 ) {
                double time = sampleTimes(birthRate, deathRate, samplingProb, maximalCalibrationsEntries.get(i).getKey(), conditionAge, 1)[0];
                times.set(l[i] + 1, time);
            }

            nodeAges.set(l[i], maximalCalibrationsEntries.get(i).getKey());

            // remove corresponding node in A
            A.remove(Integer.valueOf(l[i]));
        }

        // step5: organise times
        // after calibrations, sample times for remaining unassigned nodes
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) == 0) {
                times.set(i, sampleTimes(birthRate, deathRate, samplingProb, 0, conditionAge, 1)[0]);
            }
        }

        // set the first node to be the max, make it the root
        if (times.size() > 1) {
            double max = times.get(1);
            for (int i = 1; i < times.size() ; i++) {
                if (times.get(i) > max) {
                    max = times.get(i);
                }
            }
            times.set(0, max); // set this the largest
        }

        // or if not root conditioned, set it to stem age
        if (!rootConditioned){
            times.set(0, conditionAge);
        }

        // step6: fill in nodelist
        // get non-clade taxa
        List<String> nonCladeTaxa = new ArrayList<>();
        if (getOtherNames() != null){
            String[] otherNames = getOtherNames().value();
            for (int i = 0; i < otherNames.length; i++) {
                nameList.add(otherNames[i]);
                nonCladeTaxa.add(otherNames[i]);
            }
        } else {
            if (backUpNames.size() > 0){
                for (String name : backUpNames) {
                    nameList.add(name);
                    nonCladeTaxa.add(name);
                }
            }
            int nameListSize = nameList.size();
            for (int i = 0; i < n - nameListSize; i++) {
                nameList.add(String.valueOf(i));
                nonCladeTaxa.add(String.valueOf(i));
            }
        }
        // get random order for non-clade taxa
        Collections.shuffle(nonCladeTaxa);

        // Assign remaining uncalibrated taxa names to available node positions
        int ind = 0;
        for (int i = 0; i < nodeList.size() && ind < nonCladeTaxa.size(); i++) {
            if (nodeList.get(i) == null) {
                TimeTreeNode tip = new TimeTreeNode(0);
                tip.setId(nonCladeTaxa.get(ind));
                nodeList.set(i, tip);
                ind++;
            }
        }

        // step7: coalesce
        // combine sub-CPPs into the final tree
        while (nodeList.size() > 1) {
            // start from the youngest node
            int j = indexOfMin(times);
            if (times.size() == 2) {
                j = 1; // make it being the second one if there are only 2 nodes
            }

            // build relationship
            TimeTreeNode child_left = nodeList.get(j-1);
            TimeTreeNode child_right = nodeList.get(j);

            TimeTreeNode parent = new TimeTreeNode(times.get(j));
            parent.addChild(child_left);
            parent.addChild(child_right);

            child_left.setParent(parent);
            child_right.setParent(parent);

            // give ages
            child_left.setAge(nodeAges.get(j-1));
            child_right.setAge(nodeAges.get(j));

            // adjust indices
            nodeList.set(j-1, parent);
            nodeList.remove(j);

            // set parent node to time of current node
            nodeAges.set(j-1, times.get(j));

            // remove the time and age of the second node
            times.remove(j);
            nodeAges.remove(j);
        }

        tree.setRoot(nodeList.get(0), true);
        return new RandomVariable<>("CPPTree", tree, this);
    }

    /*
       Functions
    */
    private static double[] calculateScore(List<Integer> A, int m, List<Double> times) {
        double[] s = new double[A.size()];
        for (int j = 0; j < A.size(); j++) {
            int nodeIndex = A.get(j); // or A[idx] if it's an array
            int count = 0;
            // Check if i < m and i+1 is within bounds
            if (nodeIndex < m - 1 && times.get(nodeIndex + 1) == 0) {
                count++;
            }
            // Check if nodeIndex is within bounds and times[i] == 0
            if (times.get(nodeIndex) == 0) {
                count++;
            }
            s[j] = count;
        }
        return s;
    }


    private static double[] getWeights(double[] s, double w) {
        double sumOfWeights = 0;
        double[] weights = new double[s.length];

        for (int k = 0; k < s.length; k++) {
            weights[k] = Math.pow(w, s[k]);
            sumOfWeights += weights[k];
        }

        // normalise weights
        for (int k = 0; k < s.length; k++) {
            weights[k] /= sumOfWeights;
        }

        return weights;
    }


    public TreeMap<Double, String[]> getNestedClades(TreeMap<Double, String[]> clade, TreeMap<Double, String[]> cladeCalibrations) {
        boolean[] isNested = isSuperSetOf(clade,cladeCalibrations);
        List<Integer> indices = checkTrues(isNested);
        TreeMap<Double, String[]> subClades = new TreeMap<>();
        int pointer = 0;

        for (Map.Entry<Double, String[]> entry : cladeCalibrations.entrySet()){
            if (indices.contains(pointer)) {
                subClades.put(entry.getKey(), entry.getValue());
            }
            pointer ++;
        }
        return subClades;
    }

    public TreeMap<Double, String[]> getMaximalCalibrations(TreeMap<Double, String[]> cladeCalibrations) {
        TreeMap<Double, String[]> maximalCalibrations = new TreeMap<>();

        List<Map.Entry<Double, String[]>> entries = new ArrayList<>(cladeCalibrations.entrySet());

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Double, String[]> current = entries.get(i);
            TreeMap<Double, String[]> currentMap = new TreeMap<>();
            currentMap.put(current.getKey(), current.getValue());

            // check if there's a subset of the calibrations
            boolean[] results = isSubsetOf(currentMap, cladeCalibrations);
            if (checkTrues(results).size() == 1) {
                maximalCalibrations.put(current.getKey(), current.getValue());
            }
        }

        return maximalCalibrations;
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(BirthDeathConstants.lambdaParamName, birthRate);
        map.put(BirthDeathConstants.muParamName, deathRate);
        map.put(BirthDeathConstants.rhoParamName, rho);
        map.put(DistributionConstants.nParamName, n);
        map.put(cladeMRCAAgeName, cladeMRCAAge);
        map.put(cladeTaxaName, cladeTaxa);
        if (rootAge != null) map.put(BirthDeathConstants.rootAgeParamName, rootAge);
        if (stemAge != null) map.put(stemAgeName, stemAge);
        if (otherNames != null) map.put(otherTaxaNames, otherNames);
        return map;
    }

    public void setParam(String paramName, Value value){
        if (paramName.equals(BirthDeathConstants.lambdaParamName)) birthRate = value;
        else if (paramName.equals(BirthDeathConstants.muParamName)) deathRate = value;
        else if (paramName.equals(BirthDeathConstants.rhoParamName)) rho = value;
        else if (paramName.equals(DistributionConstants.nParamName)) n = value;
        else if (paramName.equals(BirthDeathConstants.rootAgeParamName)) rootAge = value;
        else if (paramName.equals(cladeTaxaName)) cladeTaxa = value;
        else if (paramName.equals(cladeMRCAAgeName)) cladeMRCAAge = value;
        else if (paramName.equals(otherTaxaNames)) otherNames = value;
        else if (paramName.equals(stemAgeName)) stemAge = value;
        else super.setParam(paramName, value);
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

    public Value<Number> getSamplingProb(){
        return getParams().get(BirthDeathConstants.rhoParamName);
    }

    public Value<String[][]> getCladeTaxa(){
        return getParams().get(cladeTaxaName);
    }

    public Value<Number[]> getCladeMRCAAge(){
        return getParams().get(cladeMRCAAgeName);
    }

    public Value<Number> getStemAge(){return getParams().get(stemAgeName);}

    public Value<Number> getRootAge(){
        return getParams().get(BirthDeathConstants.rootAgeParamName);
    }

    public Value<String[]> getOtherNames(){
        return getParams().get(otherTaxaNames);
    }
}

