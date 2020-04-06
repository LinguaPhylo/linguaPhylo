package lphy.core;

import lphy.TimeTree;
import lphy.TimeTreeNode;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloOU extends PhyloBrownian {

    protected Value<Double> theta;
    protected Value<Double> alpha;

    String thetaParamName;
    String alphaParamName;

    public PhyloOU(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                   @ParameterInfo(name = "diffRate", description = "the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.") Value<Double> variance,
                   @ParameterInfo(name = "theta", description = "the 'optimal' value that the long-term process is centered around.") Value<Double> theta,
                   @ParameterInfo(name = "alpha", description = "the drift term that determines the rate of drift towards the optimal value.") Value<Double> alpha,
                   @ParameterInfo(name = "y0", description = "the value of continuous trait at the root.") Value<Double> y0) {

        super(tree, variance, y0);

        this.theta = theta;
        this.alpha = alpha;

        thetaParamName = getParamName(2);
        alphaParamName = getParamName(3);
        y0RateParamName = getParamName(4);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(treeParamName, tree);
        map.put(diffusionRateParamName, diffusionRate);
        map.put(thetaParamName, theta);
        map.put(alphaParamName, alpha);
        map.put(y0RateParamName, y0);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(diffusionRateParamName)) diffusionRate = value;
        else if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(alphaParamName)) alpha = value;
        else if (paramName.equals(y0RateParamName)) y0 = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    protected double sampleNewState(double initialState, double time) {

        double a = alpha.value();

        double v = diffusionRate.value()/(2*a);

        double weight = Math.exp(-a*time);

        double mean = (1.0-weight)*theta.value() + weight*initialState;

        double variance = v * (1.0 - Math.exp(-2.0*a*time));

        NormalDistribution distribution = new NormalDistribution(mean, Math.sqrt(variance));
        return handleBoundaries(distribution.sample());
    }
}
