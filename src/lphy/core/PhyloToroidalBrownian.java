package lphy.core;

import lphy.TimeTree;
import lphy.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArrayValue;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by alexpopinga on 2/02/20.
 */
public class PhyloToroidalBrownian extends PhyloMultivariateBrownian {

    boolean anglesInRadians = true;

    // ANGLES IN RADIANS FOR THIS IMPLEMENTATIONS
    double MAX_ANGLE_VALUE = Math.PI*2.0;

    public PhyloToroidalBrownian(@ParameterInfo(name = "tree", description = "the time tree.") Value<TimeTree> tree,
                                 @ParameterInfo(name = "diffusionMatrix", description = "the multivariate diffusion rates.") Value<Double[][]> diffusionRate,
                                 @ParameterInfo(name = "y", description = "the value of multivariate traits at the root.") Value<Double[]> y) {
        super (tree, diffusionRate, y);
    }

    protected Double[] handleBoundaries(double[] rawValues) {

        Double[] newValues = new Double[rawValues.length];

        for (int i = 0; i < rawValues.length; i++) {
            newValues[i] = PhyloCircularBrownian.wrapToMaxAngle(rawValues[i], MAX_ANGLE_VALUE);
        }
        return newValues;
    }
}
