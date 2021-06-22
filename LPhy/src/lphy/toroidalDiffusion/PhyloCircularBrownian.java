package lphy.toroidalDiffusion;

import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.continuous.PhyloBrownian;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;

import static lphy.toroidalDiffusion.ToroidalUtils.wrapToMaxAngle;

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

    @GeneratorInfo(name = "PhyloCircularBrownian",
            verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic Brownian process with periodic boundary conditions",
            description = "The phylogenetic Brownian process with periodic boundary conditions. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.")
    public RandomVariable<ContinuousCharacterData> sample() {
        return super.sample();
    }

    protected double handleBoundaries(double rawAngle) {
        return wrapToMaxAngle(rawAngle, MAX_ANGLE_VALUE);
    }

}
