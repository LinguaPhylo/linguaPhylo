package lphy.base.evolution.coalescent.populationmodel;
import lphy.base.evolution.coalescent.PopulationFunction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class ConstantPopulation implements PopulationFunction {

    private double N0;

    /**
     * Constructor for ConstantPopulation
     *
     * @param N0 Population size (constant over time)
     */
    public ConstantPopulation(double N0) {
        this.N0 = N0;
    }

    /**
     * Calculate the population size at a given time t.
     *
     * @param t Time (not used in this constant model)
     * @return Constant population size N
     */
    @Override
    public double getTheta(double t) {
        return N0;
    }

    /**
     * Calculate the cumulative intensity over time period from 0 to t.
     *
     * @param t Time
     * @return Intensity value
     */
    @Override
    public double getIntensity(double t) {
        return t / N0;
    }

    /**
     * Calculate the time corresponding to a given intensity x.
     *
     * @param x Intensity
     * @return Time corresponding to the given intensity
     */
    @Override
    public double getInverseIntensity(double x) {
        return x * N0;
    }

    @Override
    public boolean isAnalytical() {
        return false; // Use analytical method here
    }

    @Override
    public String toString() {
        return "Constant population size of " + N0;
    }

    public static void main(String[] args) {
        double N = 100;
        double tStart = 0;
        double tEnd = 100;
        int nPoints = 100;

        ConstantPopulation constantPopulation = new ConstantPopulation(N);

        try (PrintWriter writer = new PrintWriter(new FileWriter("constant_data.csv"))) {
            writer.println("time,theta");
            for (int i = 0; i < nPoints; i++) {
                double t = tStart + (i / (double) (nPoints - 1)) * (tEnd - tStart);
                double theta = constantPopulation.getTheta(t);

                writer.printf(Locale.US, "%.4f,%.4f%n", t, theta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
