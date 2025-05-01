package lphy.base.evolution.eigensystem;

/**
 * Ported from BEAST2 by jsaghafifar 01/05/25
 * @author Andrew Rambaut
 */
public interface EigenSystem {
    /**
     * Set the instantaneous rate matrix
     * This changes the values in matrix as side effect
     * @param Qmatrix
     */
    EigenDecompositionExt decomposeMatrix(Double[][] Qmatrix);
}
