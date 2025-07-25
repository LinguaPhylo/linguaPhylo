package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;

import java.util.*;

    /*
       The mathematical and sampling methods for CPP
    */
public class CPPUtils {
    // ****** mathematical methods ******
    public static double CDF(double b, double d, double rho, double t) {
        double p = rho * b * (1 - Math.exp(-(b - d) * t)) / (rho * b + (b * (1 - rho) - d) * Math.exp(-(b - d) * t));
        return p;
    }

    public static double inverseCDF(double b, double d, double rho, double p) {
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
        double t = Math.log((deathRate * Math.pow(p, 1.0 / nSims) - birthRate)
                / (birthRate * (Math.pow(p, 1.0 / nSims) - 1.0)))
                / (birthRate - deathRate);
        return t;
    }


    // ****** time sampling methods ******
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
            results[i] = inverseCDF(birthRate, deathRate, samplingProbability,p);
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

            results[i] = inverseCDF(birthRate, deathRate, samplingProbability, p);
        }

        return results;
    }

    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, int nSims) {
        // Calculate the CDF value (Q)
        return sampleTimes(birthRate, deathRate, samplingProbability, 0, Double.POSITIVE_INFINITY, nSims);
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
            times[i] = inverseCDF(birthRate, deathRate, samplingProbability, p);
        }

        return times;
    }

    public static void sampleElement(List<Integer> A, double[] weights, int[] l, int i) {
        Random random = new Random();
        double prob = random.nextDouble(); // random number between 0 and 1
        double cumulative = 0.0;
        for (int j = 0; j < A.size(); j++) {
            cumulative += weights[j];
            if (prob <= cumulative) {
                l[i] = A.get(j);
                break;
            }
        }
    }

//    // TODO: what's returning here, what's calculating?
//    public Object probCalibrations(double b, double d, double rho, List<TimeTreeNode> calibrations, Taxa taxa) {
//        int nCalibrations = calibrations.size();
//        int nCalibratedTaxa = 0;
//        for (TimeTreeNode calibration : calibrations) {
//            nCalibratedTaxa += calibration.getAllLeafNodes().size();
//        }
//        int n = taxa.ntaxa() - nCalibratedTaxa + nCalibrations;
//        int k = nCalibrations;
//
//        double totalSum = sumOverSubsets(n,k);
//        return null;
//    }

    /**
     * Get the number of given taxa
     * @param n length of the whole set
     * @param k the length of each subset
     * @return the total number of all subsets
     */
    private double sumOverSubsets(int n, int k) {
        int[][] subsets = permutations(n,k);
        double totalSum = 0;
        for (int[] subset : subsets) {
            totalSum += f(subset);
        }
        return totalSum;
    }

    // TODO: what is f()
    private double f(int[] subset) {
        return 0.0;
    }

    public static int[][] permutations(int n, int k) {
        // Use default values from the R function:
        // v = 1:n, set = true, repeats.allowed = false
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = i + 1;
        }
        boolean set = true;
        boolean repeatsAllowed = false;

        // Input validation
        if (n < 1) {
            throw new IllegalArgumentException("bad value of n");
        }
        if (k < 1) {
            throw new IllegalArgumentException("bad value of k");
        }
        if (k > n && !repeatsAllowed) {
            throw new IllegalArgumentException("k > n and repeats.allowed=false");
        }

        // Generate permutations without repeats
        return generateWithoutRepeats(n, k, v);
    }

    // Recursive function for permutations without repeats
    private static int[][] generateWithoutRepeats(int n, int r, int[] v) {
        if (r == 1) {
            int[][] result = new int[n][1];
            for (int i = 0; i < n; i++) {
                result[i][0] = v[i];
            }
            return result;
        } else if (n == 1) {
            int[][] result = new int[1][r];
            result[0][0] = v[0];
            return result;
        } else {
            List<int[]> resultList = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                // Create a new array without element at index i
                int[] newV = new int[n - 1];
                int idx = 0;
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        newV[idx++] = v[j];
                    }
                }

                // Recursive call
                int[][] subPermutations = generateWithoutRepeats(n - 1, r - 1, newV);

                // Combine current element with sub-permutations
                for (int[] subPerm : subPermutations) {
                    int[] newRow = new int[r];
                    newRow[0] = v[i];
                    System.arraycopy(subPerm, 0, newRow, 1, r - 1);
                    resultList.add(newRow);
                }
            }

            // Convert List to array
            int[][] result = new int[resultList.size()][r];
            for (int i = 0; i < resultList.size(); i++) {
                result[i] = resultList.get(i);
            }
            return result;
        }
    }

    // ****** check methods ******
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

    public static List<Integer> checkTrues(boolean[] results) {
        int i = 0;
        List<Integer> indices = new ArrayList<>();
        for (boolean val : results) {
            if (val) {
                indices.add(i);
            }
            i ++;
        }
        return indices;
    }

    // returns list of booleans if clade contains cladeCalibrations.get(i) under the partial order of set inclusion
    public static boolean[] isSuperSetOf(TreeMap<Double, String[]> clade, TreeMap<Double, String[]> cladeCalibrations) {
        Set<String> cladeTaxa = new HashSet<>();
        for (String[] taxaArray : clade.values()) {
            cladeTaxa.addAll(Arrays.asList(taxaArray));
        }

        // Prepare result array
        boolean[] result = new boolean[cladeCalibrations.size()];
        int i = 0;

        // Check each calibration to see if it's a subset of cladeTaxa
        for (Map.Entry<Double, String[]> entry : cladeCalibrations.entrySet()) {
            String[] calibrationTaxa = entry.getValue();
            boolean isSubset = true;

            for (String taxon : calibrationTaxa) {
                if (!cladeTaxa.contains(taxon)) {
                    isSubset = false;
                    break;
                }
            }

            result[i++] = isSubset;
        }

        return result;
    }
    // returns list of TRUE if clade is subset of cladeCalibrations.get(i) under the partial order of set inclusion
    public static boolean[] isSubsetOf(TreeMap<Double, String[]> clade, TreeMap<Double, String[]> cladeCalibrations) {
        // Combine all taxa from the clade into a single set
        Set<String> cladeTaxa = new HashSet<>();
        for (String[] taxaArray : clade.values()) {
            cladeTaxa.addAll(Arrays.asList(taxaArray));
        }

        // Prepare result array
        boolean[] result = new boolean[cladeCalibrations.size()];
        int i = 0;

        // For each calibration, check if clade is a subset
        for (Map.Entry<Double, String[]> entry : cladeCalibrations.entrySet()) {
            Set<String> calibrationSet = new HashSet<>(Arrays.asList(entry.getValue()));
            boolean isSubset = true;

            for (String taxon : cladeTaxa) {
                if (!calibrationSet.contains(taxon)) {
                    isSubset = false;
                    break;
                }
            }

            result[i++] = isSubset;
        }

        return result;
    }

    // ****** tree methods ******

    public static double simRandomStem(double birthRate, double deathRate, double greaterThan, int nTaxa){
        double Q = CPPUtils.Qdist(birthRate, deathRate, greaterThan, nTaxa);
        double p = Math.random() * (1.0 - Q) + Q;
        double t = CPPUtils.transform(p, birthRate, deathRate, nTaxa);
        return t;
    }

    public static TimeTree mapCPPTree(List<String> nameList, List<Double> t) {
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
}
