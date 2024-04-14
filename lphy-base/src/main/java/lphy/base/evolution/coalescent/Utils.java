package lphy.base.evolution.coalescent;

import lphy.base.math.MathUtils;



public class Utils {

    /**
     * Calculates the time interval for a given point in time and population function.
     *
     * @param U Uniform random variable used for randomization.
     * @param populationFunction Population function used to calculate intensity and inverse intensity.
     * @param lineageCount The number of currently active pedigrees.
     * @param timeOfLastCoalescent The time of the last merge event.
     * @return The time interval for the next merge event.
     */
    private static double getInterval(double U, PopulationFunction populationFunction,
                                      int lineageCount, double timeOfLastCoalescent) {
        double intensity = populationFunction.getIntensity(timeOfLastCoalescent);
        double tmp = -Math.log(U) / MathUtils.choose2(lineageCount) + intensity;
        return populationFunction.getInverseIntensity(tmp) - timeOfLastCoalescent;
    }

    /**
     * Gets the simulated interval.
     */
    public static double getSimulatedInterval(PopulationFunction populationFunction,
                                              int lineageCount, double timeOfLastCoalescent) {
        double U = Math.random(); // Use the Java standard library to generate random numbers in the interval [0, 1)
        return getInterval(U, populationFunction, lineageCount, timeOfLastCoalescent);
    }

    /**
     * Gets the median merge interval, assuming U=0.5.
     */
    public static double getMedianInterval(PopulationFunction populationFunction,
                                           int lineageCount, double timeOfLastCoalescent) {
        return getInterval(0.5, populationFunction, lineageCount, timeOfLastCoalescent);
    }


}

