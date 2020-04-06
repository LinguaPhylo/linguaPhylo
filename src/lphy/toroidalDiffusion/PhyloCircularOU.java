package lphy.toroidalDiffusion;

import lphy.TimeTree;
import lphy.core.PhyloOU;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.toroidalDiffusion.ToroidalUtils.wrapToMaxAngle;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCircularOU extends PhyloOU {

    public PhyloCircularOU(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                           @ParameterInfo(name = "variance", description = "the variance of the underlying Brownian process.") Value<Double> variance,
                           @ParameterInfo(name = "theta", description = "the 'optimal' value that the long-term process is centered around.") Value<Double> theta,
                           @ParameterInfo(name = "alpha", description = "the drift term that determines the rate of drift towards the optimal value.") Value<Double> alpha,
                           @ParameterInfo(name = "y0", description = "the value of continuous trait at the root.") Value<Double> y0) {

        super(tree, variance, theta, alpha, y0);
    }

    protected double sampleNewState(double initialState, double time) {

        double a = alpha.value();

        double v = diffusionRate.value()/(2*a);

        double weight = Math.exp(-a*time);

        double mean = (1.0-weight)*theta.value() + weight*initialState;

        double variance = v * (1.0 - Math.exp(-2.0*a*time));

        NormalDistribution distribution = new NormalDistribution(mean, Math.sqrt(variance));
        return wrapToMaxAngle(distribution.sample(), 2*Math.PI);
    }
}
