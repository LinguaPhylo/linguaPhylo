package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;
import static lphy.base.evolution.birthdeath.CPPUtils.*;

// TODO: needs validation
public class CalibratedCPPTree extends TaxaConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {

    Value<Number> rootAge;
    Value<Number> birthRate;
    Value<Number> deathRate;
    Value<Number[]> cladeMRCAAge;
    Value<String[][]> cladeTaxa;
    Value<Number> stemAge;
    Value<Number> rho;
    Value<String[]> otherNames;
    TimeTree tree;


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
        tree = new TimeTree();

        String[][] cladeTaxaNames = getCladeTaxa().value();
        Number[] cladeAges = getCladeMRCAAge().value();

        // get valid clade calibrations
        TreeMap<Double, String[]> cladeCalibrations = new TreeMap<>();
        for (int i = 0; i< cladeTaxaNames.length; i++) {
            cladeCalibrations.put(cladeAges[i].doubleValue(), cladeTaxaNames[i]);
        }

        // if there's only one calibration and it's the root
        if (cladeTaxaNames.length == 1 && cladeTaxaNames[0].length == n){
            // return to the normal CPP tree with fixed rootAge
            if (getRootAge() != null && getRootAge().value().doubleValue() == getCladeMRCAAge().value()[0].doubleValue()) {
                CPPTree cpp = new CPPTree(getBirthRate(), getDeathRate(), getSamplingProb(),
                        new Value<>("", getCladeTaxa().value()[0]), getN(), getRootAge(), null);
                tree = cpp.sample().value();
                return new RandomVariable<>("", tree, this);
            } else {
                throw new IllegalArgumentException("The root age should be the same as the clade mrca age when calibrating the root!");
            }

        } else if (cladeTaxaNames.length > 1){
            cladeCalibrations.remove(cladeCalibrations.lastEntry().getKey());
        } else if (cladeTaxaNames.length == 0) {
            throw new IllegalArgumentException("The clade taxa shouldn't be empty!");
        }

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

        // create A hodling indices of nodes as inactive nodes
        List<Integer> A = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            A.add(i);
        }

        // initialise l store indices of nodes as activated nodes
        int[] l = new int[m];
        List<Double> times = new ArrayList<>(Collections.nCopies(m, null));

        double conditionAge = 0.0;

        if (rootAge != null) {
            int ind = random.nextInt(m - 1) + 1;

            if (m == 1){ // the second number
                ind = 1;
            }
            conditionAge = getRootAge().value().doubleValue();
            times.set(ind, conditionAge);

        } else {
            if (getStemAge()!= null) {
                conditionAge = getStemAge().value().doubleValue();
            } else {
                conditionAge = CPPUtils.simRandomStem(birthRate, deathRate, maximalCalibrations.firstEntry().getKey(), n);
            }
        }

        List<Double> nodeAges = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            nodeAges.add(0.0);
        }

        List<Map.Entry<Double, String[]>> maximalCalibrationsEntries = new ArrayList<>(maximalCalibrations.entrySet());
        List<Map.Entry<Double, String[]>> cladeCalibrationsEntries = new ArrayList<>(cladeCalibrations.entrySet());
        nameList = new ArrayList<>(n);

        // set name list
        for (int i = 0; i< maximalCalibrations.size(); i++) {
            String[] uniqueNames = new String[maximalCalibrationsEntries.get(i).getValue().length];
            for (int j = 0; j< maximalCalibrationsEntries.get(i).getValue().length; j++) {
                //String newName = "clade" + i + "_" + maximalCalibrationsEntries.get(i).getValue()[j];
                String newName = maximalCalibrationsEntries.get(i).getValue()[j];

                nameList.add(newName);
                uniqueNames[j] = newName;
            }
            maximalCalibrations.put(maximalCalibrationsEntries.get(i).getKey(), uniqueNames);
        }

        // pre-fill the list with nulls so it has actual elements at each index
        List<TimeTreeNode> nodeList = new ArrayList<>((Collections.nCopies(A.size(), null)));

        for (int i = 0; i < maximalCalibrations.size(); i++) {
            TreeMap<Double, String[]> clade = new TreeMap<>();
            clade.put(maximalCalibrationsEntries.get(i).getKey(), maximalCalibrationsEntries.get(i).getValue());
            TreeMap<Double, String[]> subClades = getNestedClades(clade, cladeCalibrations);

            if (subClades.size() == 0){
                subClades = clade;
            }

            // calculate weight w
            double w = CDF(getBirthRate().value().doubleValue(), getDeathRate().value().doubleValue(), getSamplingProb().value().doubleValue(), conditionAge) -
                    CDF(getBirthRate().value().doubleValue(), getDeathRate().value().doubleValue(), getSamplingProb().value().doubleValue(), cladeCalibrationsEntries.get(i).getKey());

            // calculate score s for each node
            double[] s = calculateScore(A, m, times);

            // calculate weight for each node
            double[] weights = getWeights(s, w);

            if (A.size() == 1){
                l[i] = A.get(0);
            } else {
                // sample one element from A with probability weights
                CPPUtils.sampleElement(A, weights, l, i);
            }

            if (subClades.size() != 0) {
                // simulate a tree for this clade
                CalibratedCPPTree calibratedCPPTree = new CalibratedCPPTree(getBirthRate(), getDeathRate(), getSamplingProb(),
                        new Value<>("", maximalCalibrationsEntries.get(i).getValue().length),
                        new Value<>("", subClades.values().toArray(new String[0][])),
                        new Value<>("", subClades.keySet().toArray(new Number[0])), null,
                        new Value<>("", maximalCalibrationsEntries.get(i).getKey()),null);

                // put clade mrca into a list waiting for assign
                TimeTree subTree = calibratedCPPTree.sample().value();
                nodeList.set(i, subTree.getRoot());
            }

            // deal with the nodes have l[i] still 0
            if (times.get(l[i]) == null) {
                double time = CPPUtils.sampleTimes(birthRate, deathRate, samplingProb, maximalCalibrationsEntries.get(i).getKey(), conditionAge, 1)[0];
                times.set(l[i],time);
            }
            if ((l[i] < m) && (times.get(l[i] + 1) == null)) {
                times.set(l[i] + 1, CPPUtils.sampleTimes(birthRate, deathRate, samplingProb, maximalCalibrationsEntries.get(i).getKey(), conditionAge, 1)[0]);
            }

            nodeAges.set(l[i], maximalCalibrationsEntries.get(i).getKey());

            // remove corresponding node in A
            A.remove(Integer.valueOf(l[i]));
        }

        // after calibrations, sample times for remaining unassigned nodes
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) == null) {
                times.set(i, CPPUtils.sampleTimes(birthRate, deathRate, samplingProb, 0, conditionAge, 1)[0]);
            }
        }

        // set the first node to be the max, make it the root
        if (times.size() > 1) {
            double max = times.get(0);
            for (int i = 0; i < times.size() ; i++) {
                if (times.get(i) > max) {
                    max = times.get(i);
                }
            }
        }

        if (rootAge != null) {
            times.set(0, conditionAge); // Override if not root-conditioned
        }

        // get non-clade taxa
        List<String> nonCladeTaxa = new ArrayList<>();
        int nameListSize = nameList.size();
        if (getOtherNames() != null){
            String[] otherNames = getOtherNames().value();
            for (int i = 0; i < otherNames.length; i++) {
                nameList.add(otherNames[i]);
                nonCladeTaxa.add(otherNames[i]);
            }
        } else {
            for (int i = 0; i < n - nameListSize; i++) {
                nameList.add(String.valueOf(i));
                nonCladeTaxa.add(String.valueOf(i));
            }
        }
        // get random order for non-clade taxa
        Collections.shuffle(nonCladeTaxa);

        // Assign remaining uncalibrated taxa names to available node positions
        for (int j = 0; j < nonCladeTaxa.size(); j++) {
            for (int i = 0; i < nodeList.size(); i++) {
                if (nodeList.get(i) == null){
                    TimeTreeNode tip = new TimeTreeNode(0);
                    tip.setId(nonCladeTaxa.get(j));
                    nodeList.set(A.get(j), tip);
                }
            }
        }

        // combine sub-CPPs into the final tree
        while (nodeList.size() > 1) {
            // start from the youngest node
            int j = indexOfMin(times);
            if (times.size() == 2) {
                j = 1; // make it being the second one
            }

            if (j == 0 && times.size() > 1) {
                j = 1; // force to index 1
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
            if (nodeIndex < m && (nodeIndex + 1) < times.size() && times.get(nodeIndex + 1) == null) {
                count++;
            }
            // Check if nodeIndex is within bounds and times[i] == 0
            if (nodeIndex < times.size() && times.get(nodeIndex) == null) {
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
            // normalise
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

