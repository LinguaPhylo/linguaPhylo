package lphy.base.evolution.coalescent;


import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;

public interface PopulationFunction {

    /**
     * @param t time
     * @return value of theta at time t
     */
    double getTheta(double t);

    /**
     * @param t time
     * @return value of demographic intensity function at time t (x = integral 1/N(s) ds from 0 to t).
     */
    default double getIntensity(double t) {

        UnivariateFunction function = time -> 1 / getTheta(time);
        UnivariateIntegrator integrator = new TrapezoidIntegrator();
        return integrator.integrate(10000, function, 0, t);
    }


    /**
     * @param x the coalescent intensity
     * @return value of inverse demographic intensity function
     *         (returns time, needed for simulation of coalescent intervals).
     */

    default double getInverseIntensity(double x) {
        return 0;
    }

    boolean isAnalytical();


}
