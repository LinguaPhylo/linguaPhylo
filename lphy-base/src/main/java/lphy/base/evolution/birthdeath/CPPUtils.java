package lphy.base.evolution.birthdeath;

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
        double r = b - d;
        double A = rho * b;
        double B = b * (1- rho) - d;
        if (Math.abs(r) < 1e-10) {
            // when diversification rate ~ 0
            p = A * t / (1 + A * t);
        } else if (r < 0){
            double exp_rt = Math.exp(r * t);
            p = A * (1 - exp_rt) / (-A * exp_rt - B);
        } else{
            // r > 0
            double exp_neg_rt = Math.exp(-r * t);
            p = A * (1-exp_neg_rt) / (A + B * exp_neg_rt);
        }
        return p;
    }

    public static double inverseCDF(double b, double d, double rho, double p) {
        double t = Math.log(1 + ((b - d) * p) / (b * rho * (1 - p))) / (b - d);
        return t;
    }

    public static double densityBD(double b, double d, double rho, double time) {
        double density;
        double r = b - d;
        double A = rho * b;
        double B = b * (1- rho) - d;
        if (Math.abs(r) < 1e-10) {
             density = A / ((1.0 + A * time) * (1.0 + A * time));
        } else if (r < 0){
            double exp_rt = Math.exp(r * time);
            density = A* r  * r * r * time / (A * exp_rt + B) * (A * exp_rt + B);
        } else {
            double exp_neg_rt = Math.exp(-r*time);
            density = A * r / Math.pow(A + B * exp_neg_rt, 2) * time;
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

        double eps = 1e-12;
        Qlower = Math.max(eps, Math.min(1.0 - eps, Qlower));
        Qupper = Math.max(Qlower + eps, Math.min(1.0 - eps, Qupper));

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
        // check the ages, superset should be older
        for (int j = 0; j < result.length; j++) {
            if (result[j]) {
                double supersetAge = clade.getAge();
                double subsetAge = cladeCalibrations.get(j).getAge();

                if (supersetAge < subsetAge) {
                    throw new IllegalArgumentException(
                            "Superset clade " + Arrays.toString(clade.getNames()) +
                                    " has age " + supersetAge +
                                    " which is younger than its subset calibration clade " +
                                    Arrays.toString(cladeCalibrations.get(j).getNames()) +
                                    " with age " + subsetAge +
                                    ". Please double check the clade ages."
                    );
                }
            }
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

        // check ages
        // If there's a superset calibration, check ages: subset clade must not be older than superset
        for (int j = 0; j < result.length; j++) {
            if (result[j]) {
                double supersetAge = cladeCalibrations.get(j).getAge();
                double subsetAge = clade.getAge();

                if (subsetAge > supersetAge) {
                    throw new IllegalArgumentException(
                            "Clade " + Arrays.toString(clade.getNames()) +
                                    " has age " + subsetAge +
                                    " which is older than its superset calibration clade " +
                                    Arrays.toString(cladeCalibrations.get(j).getNames()) +
                                    " with age " + supersetAge +
                                    ". Please double check the clade ages."
                    );
                }
            }
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

}
