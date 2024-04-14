package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class GompertzPopulation implements PopulationFunction {


    public static final String BParamName = "b";
    public static final String NINFINITYParamName = "NInfinity";
    public static final String T50ParamName = "t50";
    public static final String F0ParamName = "f0";

    private double N0;  // Initial population size
    private double b;   // Initial growth rate of tumor growth
    private double NInfinity; // Carrying capacity
    private double t50; // time when population is half of carrying capacity
    private double f0;        // Initial proportion of the carrying capacity

    private double resolution_magic_number = 1e4;

    public static double computeT50(double NInfinity, double N0, double b) {

        if (N0 >= NInfinity || b <= 0) {
            throw new IllegalArgumentException("N0 must be less than NInfinity and b must be greater than 0.");
        }
        double ratio = NInfinity / N0;
        double proportion = 0.5;
//        if (proportion <= 0 || proportion >= 1) {
//            throw new IllegalArgumentException("Proportion must be between 0 and 1.");
//        }
        double t50 = Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
        return t50;
    }




    public double getTimeForGivenProportion(double k) {
        // Ensure b is not 0 to avoid division by zero
        if (b == 0) {
            throw new IllegalArgumentException("Growth rate b cannot be zero.");
        }

        double ratio = NInfinity / N0;
        double proportion = k * ratio;
        // Ensure proportion is within valid range to avoid taking log of non-positive number
        if (proportion <= 0 || proportion >= ratio) {
            throw new IllegalArgumentException("Proportion must be between 0 and " + ratio);
        }

        // Apply the formula to calculate t*
        double tStar = Math.log(1 - Math.log(proportion) / Math.log(ratio)) / b;
        return tStar;
    }

    public double getN0() {
        return this.N0;
    }



    private IterativeLegendreGaussIntegrator createIntegrator() {
        int numberOfPoints = 5; // Legendre-Gauss points
        double relativeAccuracy = 1.0e-10; // relative precision
        double absoluteAccuracy = 1.0e-9; // absolute accuracy
        int minimalIterationCount = 2; // Minimum number of iterations
        int maximalIterationCount = 100000; //Maximum number of iterations, adjust as needed
        return new IterativeLegendreGaussIntegrator(numberOfPoints, relativeAccuracy, absoluteAccuracy, minimalIterationCount, maximalIterationCount);
    }

    /**
     * @param
     * @param b
     * @param NInfinity
     */
//    public GompertzPopulation(double t50, double b, double NInfinity) {   //(this is for t50 )
//        this.b = b;
//        this.t50 = t50;
//        this.NInfinity = NInfinity;
//        // Calculate N0 based on t50, b, and NInfinity
//        // N(t50) = NInfinity / 2
//        // t50 is a time location given by the user, t50 < 0 means it is in the early exponential phase
//        this.N0 = NInfinity * Math.pow(2, -Math.exp(-b * this.t50));
//    }

    public GompertzPopulation(double f0, double b, double NInfinity) {    //(this is for f0 )
        this.f0 = f0;
        this.b = b;
        this.NInfinity = NInfinity;
        this.N0  = NInfinity * this .f0;
    }



    /**
     * Implement the Gompertz function to calculate theta at time t
     * Assuming theta is proportional to population size for simplicity
     *
     * @param t time, where t > 0 is time in the past
     * @return N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)))
     */
//    @Override  //(this is for t50 )
//    public double getTheta(double t) {
//        // the sign of b * t is such that t = 0 is present time and t > 0 is time in the past
//        return N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)));
//    }

    @Override  //(this is for f0 )
    public double getTheta(double t) {
        // Calculate N0 from f0 and NInfinity
        // double N0 = NInfinity * f0;
        return N0 * Math.exp(Math.log(NInfinity / N0) * (1 - Math.exp(b * t)));
    }


    @Override
    public double getIntensity(double t) {
        if (t == 0) return 0;

        UnivariateFunction function = time -> 1 / getTheta(time);

        //  Use the separate method to create the integrator
//    return legrandeIntegrator(function, t);
        IterativeLegendreGaussIntegrator integrator = createIntegrator();
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }

//    return rombergIntegrator(function, t);
//}

    private double legrandeIntegrator(UnivariateFunction function, double t) {
        IterativeLegendreGaussIntegrator integrator = createIntegrator();
        return integrator.integrate(Integer.MAX_VALUE, function, 0, t);
    }

    private double rombergIntegrator(UnivariateFunction function, double t) {

        int maxBound = Integer.MAX_VALUE;
//    int maxBound = 100000;

        RombergIntegrator integrator = new RombergIntegrator();
        double absoluteAccuracy = 1e-6;
        integrator = new RombergIntegrator(RombergIntegrator.DEFAULT_RELATIVE_ACCURACY,
                absoluteAccuracy, RombergIntegrator.DEFAULT_MIN_ITERATIONS_COUNT,
                RombergIntegrator.ROMBERG_MAX_ITERATIONS_COUNT);

//    try {
//        System.out.println("Absolute accuracy = " + integrator.getAbsoluteAccuracy());
//        System.out.println("Relative accuracy = " + integrator.getRelativeAccuracy());
        return integrator.integrate(maxBound, function, 0, t);
//    }
    }






//    @Override
//    public double getIntensity(double t) {
//
//        if (t == 0) return 0;
//
//        if (getTheta(t) < NInfinity/resolution_magic_number) {
//            throw new RuntimeException("Theta too small to calculate intensity!");
//        }
//
//        UnivariateFunction function = time -> 1.0 / getTheta(time);
//        UnivariateIntegrator integrator = new TrapezoidIntegrator();
//        // The number 10000 here represents a very high number of iterations for accuracy.
//        return integrator.integrate(100000, function, 0, t);
//    }
//


    // passes unit test without magic number
// Error when running LPhy script that signs at interval endpoints are not different
//    @Override
//    public double getInverseIntensity(double x) {
////
////
////    UnivariateFunction thetaFunction = time -> getTheta(time) - NInfinity / resolution_magic_number;
////    UnivariateSolver thetaSolver = new BrentSolver();
////    double startValue = thetaFunction.value(0);
////    double endValue = thetaFunction.value(1000);
////
////    System.out.println("Function value at start of the interval (0): " + startValue);
////    System.out.println("Function value at end of the interval (1000): " + endValue);
////    double maxTime = thetaSolver.solve(100, thetaFunction, 1e-6, t50 * 10);
////    System.out.println("maxTime = " + maxTime);
////
////    UnivariateFunction function = time -> getIntensity(time) - x;
////    UnivariateSolver solver = new BrentSolver();
////    // The range [0, 100] might need to be adjusted depending on the growth model and expected time range.
//////        return solver.solve(100, function, 0, 100);
////    return solver.solve(100, function, 0.001, maxTime);
////
////}
//
//



    @Override
    public double getInverseIntensity(double x) {
        double targetIntensity = x;
        //double targetIntensity = 1000;

        double proportionForT1 = 0.01;
        double proportionForT50 = 0.5;

        double t1 = getTimeForGivenProportion(proportionForT1);
        double t50 = getTimeForGivenProportion(proportionForT50);
        System.out.println("f0 = " + f0);
        System.out.println("t1 = " + t1);

        double growthPhaseTime = t1 - t50;

        double deltaTime = growthPhaseTime / 100;

        double time = Math.max(t1,0);
        double intensity = getIntensity(time);
        System.out.println("t1 = " + t1);

        while (intensity < targetIntensity) {
            time += deltaTime;
            intensity = getIntensity(time);

        }

        // return time;
        return Math.max(time, 0);
    }






    @Override
    public boolean isAnalytical() {
        return false; //use numerical method here
    }




    public static void main(String[] args) {
        //double t50 = 10;
        double f0 =0.5;
        double b = 0.1;
        double NInfinity = 1000;
        double tStart = 0;
        double tEnd = 50;
        int nPoints = 100;


        GompertzPopulation gompertzPopulation = new GompertzPopulation(f0, b, NInfinity);

        try (PrintWriter writer = new PrintWriter(new FileWriter("gompertzpopt50_data.csv"))) {
            writer.println("time,theta");
            for (int i = 0; i < nPoints; i++) {
                double t = tStart + (i / (double)(nPoints - 1)) * (tEnd - tStart);
                double theta = gompertzPopulation.getTheta(t);

                writer.printf(Locale.US, "%.4f,%.4f%n", t, theta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}




