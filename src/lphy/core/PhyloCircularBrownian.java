package lphy.core;

import lphy.TimeTree;
import lphy.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class PhyloCircularBrownian extends PhyloBrownian {

    boolean anglesInRadians = true;

    // ANGLES IN RADIANS FOR THIS IMPLEMENTATIONS
    double MAX_ANGLE_VALUE = Math.PI*2.0;

    public PhyloCircularBrownian(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                                 @ParameterInfo(name = "diffRate", description = "the diffusion rate.") Value<Double> diffusionRate,
                                 @ParameterInfo(name = "y0", description = "the value of continuous trait at the root.") Value<Double> y0) {
        super(tree, diffusionRate, y0);

        if (!anglesInRadians) {
            MAX_ANGLE_VALUE = 360.0;
        }
    }

    protected double handleBoundaries(double rawAngle) {
        return wrapToMaxAngle(rawAngle, MAX_ANGLE_VALUE);
    }

    static double wrapToMaxAngle(double rawAngle, double MAX_ANGLE_VALUE) {
        if (rawAngle > MAX_ANGLE_VALUE) {
            int K = (int)Math.floor(rawAngle / MAX_ANGLE_VALUE);
            double fractionRemainder = rawAngle / MAX_ANGLE_VALUE - K;
            return fractionRemainder * MAX_ANGLE_VALUE;
        }

        if (rawAngle < 0.0) {
            int K = (int)Math.floor(-rawAngle / MAX_ANGLE_VALUE);
            double fractionRemainder = (-rawAngle / MAX_ANGLE_VALUE) - K;
            return MAX_ANGLE_VALUE - (fractionRemainder * MAX_ANGLE_VALUE);
        }

        return rawAngle;
    }
}
