package lphy.base.lightweight.distributions;

import lphy.base.lightweight.LGenerativeDistribution;
import lphy.base.math.MathUtils;
import lphy.core.model.components.GeneratorInfo;
import lphy.core.model.components.ParameterInfo;
import lphy.core.util.RandomUtils;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class Dirichlet implements LGenerativeDistribution<Double[]> {

    private Double[] concentration;

    public Dirichlet(@ParameterInfo(name="conc", description="the concentration parameters of a Dirichlet distribution.") Double[] concentration) {

        this.concentration = concentration;
    }

    @GeneratorInfo(name="Dirichlet", description="The dirichlet probability distribution.")
    public Double[] sample() {

        Double[] dirichlet = new Double[concentration.length];
        double sum = 0.0;
        for (int i = 0; i < dirichlet.length; i++) {
            double val = MathUtils.randomGamma(concentration[i], 1.0, RandomUtils.getRandom());
            dirichlet[i] = val;
            sum += val;
        }
        for (int i = 0; i < dirichlet.length; i++) {
            dirichlet[i] /= sum;
        }

        return dirichlet;
    }

    public double density(Double d) {
        // TODO
        return 0;
    }

    public Double[] getConcentration() {

        return concentration;
    }

    public void setConcentration(Double[] concentration) {
        this.concentration = concentration;
    }

    public String toString() {
        return getName();
    }
}