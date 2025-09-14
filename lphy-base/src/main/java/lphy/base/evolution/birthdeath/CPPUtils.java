package lphy.base.evolution.birthdeath;

import lphy.core.model.Value;

import java.util.*;

    /*
       The mathematical and sampling methods for CPP
    */
public class CPPUtils {
    public static class Clade{
        private String[] names;
        private double age;
        public Clade(double age, String[] names){
            this.age = age;
            this.names = names;
        }

        public String[] getNames(){
            return names;
        }

        public double getAge(){
            return age;
        }

        public void setAge(double age){
            this.age = age;
        }

        public void setNames(String[] names){
            this.names = names;
        }
    }

    // ****** mathematical methods ******
    public static double CDF(double b, double d, double rho, double t) {
        double p;
        if (Math.abs(b - d)> 1e-4) {
            p = rho * b * (1 - Math.exp(-(b - d) * t)) / (rho * b + (b * (1 - rho) - d) * Math.exp(-(b - d) * t));
        } else {
            p = rho * b * t / (1 + rho + b + t);
        }
        return p;
    }

    public static double inverseCDF(double b, double d, double rho, double p) {
        double t = Math.log(1 + ((b - d) * p) / (b * rho * (1 - p))) / (b - d);
        return t;
    }

    public static double densityBD(double b, double d, double rho, double time) {
        double density;
        if (Math.abs(b-d)> 1e-4) {
            density = rho * b * (b - d) * Math.exp(-(b - d) * time) / (rho * b + (b * (1 - rho) - d) * Math.exp(-(b - d) * time));
        } else {
            density = rho * b / Math.exp(1 + rho * b * time);
        }
        return density;
    }

    public static double Qdist(double birthRate, double deathRate, double t, int nSims){
        double p = birthRate *( 1 - Math.exp(- (birthRate - deathRate) * t))/(birthRate - deathRate * Math.exp(-(birthRate - deathRate)* t));
        return Math.pow(p, nSims);
    }

    public static double transform(double p, double birthRate, double deathRate, int nSims) {
        double t = Math.log((deathRate * Math.pow(p, (double) 1 / nSims) - birthRate) / (birthRate * (Math.pow(p, (double) 1 / nSims) - 1))) / (birthRate - deathRate);
        return t;
    }


    // ****** time sampling methods ******
    // time sampling methods (with condition time optional and lowerTail optional)
    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, double conditionTime, boolean lowerTail, int nSims) {
        // Calculate the CDF value (Q)
        double Q = CDF(birthRate, deathRate, samplingProbability, conditionTime);

        // Array to store the result
        double[] results = new double[nSims];

        // Generate the samples based on the lowerTail flag
        for (int i = 0; i < nSims; i++) {
            double p;
            if (lowerTail) {
                p = Math.random()*Q;
            } else {
                p = Math.random()*(1-Q) + Q;
            }
            results[i] = inverseCDF(birthRate, deathRate, samplingProbability,p);
        }

        return results;
    }

    public static double[] sampleTimes(double birthRate, double deathRate, double samplingProbability, double conditionTime, int nSims) {
        // Calculate the CDF value (Q)
        double Q = CDF(birthRate, deathRate, samplingProbability, conditionTime);

        // Array to store the result
        double[] results = new double[nSims];

        // Generate the samples based on the lowerTail flag
        for (int i = 0; i < nSims; i++) {
            double p;
            // default sample from [0,Q], lowerTail=True
            p = Math.random()*Q;

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

        // Array to store the result
        double[] times = new double[nSims];

        // Generate the samples
        for (int i = 0; i < nSims; i++) {
            // Generate a random probability between Qlower and Qupper
            double p = Math.random()*(Qupper - Qlower) + Qlower;
            // Use InverseCDF to get the sample time
            times[i] = inverseCDF(birthRate, deathRate, samplingProbability, p);
        }

        return times;
    }

    public static int sampleIndex(double[] weights) {
        // normalize weights
        double sum = 0;
        for (double w : weights) sum += w;
        double[] cdf = new double[weights.length];
        cdf[0] = weights[0] / sum;
        for (int i = 1; i < weights.length; i++) {
            cdf[i] = cdf[i - 1] + weights[i] / sum;
        }

        // generate random number
        double num = Math.random();

        // find index
        for (int i = 0; i < cdf.length; i++) {
            if (num <= cdf[i]) return i;
        }
        return weights.length - 1; // fallback
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
    public static boolean[] isSuperSetOf(Clade clade, List<Clade> cladeCalibrations) {
        Set<String> cladeTaxa = new HashSet<>(Arrays.asList(clade.getNames()));

        // Prepare result array
        boolean[] result = new boolean[cladeCalibrations.size()];
        int i = 0;

        // Check each calibration to see if it's a subset of cladeTaxa
        for (Clade entry : cladeCalibrations) {
            // assume it is super set, unless check unique names
            boolean isSuperSet = true;

            for (String taxon : entry.getNames()) {
                if (!cladeTaxa.contains(taxon)) {
                    isSuperSet = false;
                    break;
                }
            }

            result[i] = isSuperSet;
            i++;
        }

        return result;
    }
    // returns list of TRUE if clade is subset of cladeCalibrations.get(i) under the partial order of set inclusion
    public static boolean[] isSubsetOf(Clade clade, List<Clade> cladeCalibrations) {
        // Prepare result array
        boolean[] result = new boolean[cladeCalibrations.size()];
        int i = 0;

        // For each calibration, check if clade is a subset
        for (Clade entry : cladeCalibrations) {
            Set<String> calibrationSet = new HashSet<>(Arrays.asList(entry.getNames()));
            boolean isSubset = true;

            for (String taxon : clade.getNames()) {
                if (!calibrationSet.contains(taxon)) {
                    isSubset = false;
                    break;
                }
            }

            result[i] = isSubset;
            i++;
        }

        return result;
    }

    // ****** tree methods ******

    public static double simRandomStem(double birthRate, double deathRate, double greaterThan, int nTaxa){
        double Q = Qdist(birthRate, deathRate, greaterThan, nTaxa);

        double p = Math.random()*(1-Q) + Q;

        double t = transform(p, birthRate, deathRate, nTaxa);
        return t;
    }

        // TODO: not finished
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

//    /**
//     * Get the number of given taxa
//     * @param n length of the whole set
//     * @param k the length of each subset
//     * @return the total number of all subsets
//     */
//    private double sumOverSubsets(int n, int k) {
//        int[][] subsets = permutations(n,k);
//        double totalSum = 0;
//        for (int[] subset : subsets) {
//            totalSum += f(subset);
//        }
//        return totalSum;
//    }
//
//    // TODO: what is f()
//    private double f(int[] subset) {
//        return 0.0;
//    }
//
//    public static int[][] permutations(int n, int k) {
//        // Use default values from the R function:
//        // v = 1:n, set = true, repeats.allowed = false
//        int[] v = new int[n];
//        for (int i = 0; i < n; i++) {
//            v[i] = i + 1;
//        }
//        boolean set = true;
//        boolean repeatsAllowed = false;
//
//        // Input validation
//        if (n < 1) {
//            throw new IllegalArgumentException("bad value of n");
//        }
//        if (k < 1) {
//            throw new IllegalArgumentException("bad value of k");
//        }
//        if (k > n && !repeatsAllowed) {
//            throw new IllegalArgumentException("k > n and repeats.allowed=false");
//        }
//
//        // Generate permutations without repeats
//        return generateWithoutRepeats(n, k, v);
//    }
//
//    // Recursive function for permutations without repeats
//    private static int[][] generateWithoutRepeats(int n, int r, int[] v) {
//        if (r == 1) {
//            int[][] result = new int[n][1];
//            for (int i = 0; i < n; i++) {
//                result[i][0] = v[i];
//            }
//            return result;
//        } else if (n == 1) {
//            int[][] result = new int[1][r];
//            result[0][0] = v[0];
//            return result;
//        } else {
//            List<int[]> resultList = new ArrayList<>();
//
//            for (int i = 0; i < n; i++) {
//                // Create a new array without element at index i
//                int[] newV = new int[n - 1];
//                int idx = 0;
//                for (int j = 0; j < n; j++) {
//                    if (j != i) {
//                        newV[idx++] = v[j];
//                    }
//                }
//
//                // Recursive call
//                int[][] subPermutations = generateWithoutRepeats(n - 1, r - 1, newV);
//
//                // Combine current element with sub-permutations
//                for (int[] subPerm : subPermutations) {
//                    int[] newRow = new int[r];
//                    newRow[0] = v[i];
//                    System.arraycopy(subPerm, 0, newRow, 1, r - 1);
//                    resultList.add(newRow);
//                }
//            }
//
//            // Convert List to array
//            int[][] result = new int[resultList.size()][r];
//            for (int i = 0; i < resultList.size(); i++) {
//                result[i] = resultList.get(i);
//            }
//            return result;
//        }
//    }


}
