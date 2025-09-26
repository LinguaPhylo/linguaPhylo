package lphy.base.evolution.likelihood;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.Value;

/**
 * Common interface for phylogenetic evolutionary models that require tree topology,
 * molecular clock rates, and branch-specific evolutionary rates.
 */

public interface PhyloLikelihood {
    /**
     * @return the phylogenetic time tree with topology and divergence times.
     */
    Value<TimeTree> getTree();

    /**
     * @return the global molecular clock rate that scales evolutionary time.
     */
    Value<Number> getClockRate();

    /**
     * @return branch-specific rate multipliers for relaxed clock models.
     */
    Value<Double[]> getBranchRates();
}
