package lphy.toroidalDiffusion;

import org.apache.commons.math3.analysis.MultivariateFunction;

public class WrappedBivariateDiffusionTransitionProbabilityFunction implements MultivariateFunction {

    WrappedBivariateDiffusion diff;
    double phi0;
    double psi0;

    WrappedBivariateDiffusionTransitionProbabilityFunction(WrappedBivariateDiffusion diff, double phi0, double psi0) {
        this.diff = diff;
        this.phi0 = phi0;
        this.psi0 = psi0;
    }

    @Override
    public double value(double[] doubles) {
        return diff.loglikwndtpd(phi0, psi0, doubles[0], doubles[1]);
    }
}
