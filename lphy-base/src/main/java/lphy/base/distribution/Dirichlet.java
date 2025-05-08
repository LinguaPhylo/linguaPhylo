package lphy.base.distribution;

import lphy.base.math.MathUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.concParamName;

/**
 * Dirichlet distribution prior.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Dirichlet extends ParametricDistribution<Double[]> {

    private Value<Number[]> concentration;
    private Value<Number> mean;
    private static final String meanName = "mean";


    public Dirichlet(@ParameterInfo(name=concParamName, narrativeName = "concentration",
            description="the concentration parameters of a Dirichlet distribution.") Value<Number[]> concentration,
                     @ParameterInfo(name = meanName,
                             description = "The expected mean per element. By default, the sampled values sum to 1.",
                             optional = true) Value<Number> mean){
        super();
        this.concentration = concentration;
        if (mean != null){
            this.mean = mean;
        }
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {  }

    @GeneratorInfo(name="Dirichlet", verbClause = "have", narrativeName = "Dirichlet distribution prior",
            category = GeneratorCategory.PRIOR,
            examples = {"birthDeathRhoSampling.lphy","dirichlet.lphy","https://linguaphylo.github.io/tutorials/time-stamped-data/"},
            description="The dirichlet probability distribution.")
    public RandomVariable<Double[]> sample() {
        double mean = 0;
        if (getMean() != null){
            mean = getMean().value().doubleValue();
        }
        Double[] dirichlet = new Double[concentration.value().length];
        double sum = 0.0;
        for (int i = 0; i < dirichlet.length; i++) {
            double val = MathUtils.randomGamma(concentration.value()[i].doubleValue(), 1.0, random);
            dirichlet[i] = val;
            sum += val;
        }

        if (mean == 0) {
            // Make sum = 1
            for (int i = 0; i < dirichlet.length; i++) {
                dirichlet[i] /= sum;
            }
        } else {
            // Make sum = mean * n
            int K = dirichlet.length;
            for (int i = 0; i < dirichlet.length; i++) {
                // scaling, sum * proportion
                dirichlet[i] = dirichlet[i] / sum * K * mean;
            }
        }

        return new RandomVariable<>("x", dirichlet, this);
    }

    public double density(Double[] d) {
        Number[] alpha = getConcentration().value();
        if (alpha.length != d.length) {
            throw new IllegalArgumentException("Dimensions don't match");
        }

        double sumAlpha = 0.0;
        for (Number a: alpha){
            sumAlpha = sumAlpha + a.doubleValue();
        }

        double sumD = 0;
        for (double a : d){
            sumD += a;
        }

        // calc gamma(sumAlpha)
        double gammaSumAlpha = org.apache.commons.math3.special.Gamma.gamma(sumAlpha);
        // calc ∏ Gamma(alpha_i)
        double gammaAlphaProd = 1.0;
        for (Number a : alpha) {
            gammaAlphaProd *= org.apache.commons.math3.special.Gamma.gamma(a.doubleValue());
        }

        //∏ d_i^(alpha_i - 1)
        double dProd = 1.0;
        for (int i = 0; i < d.length; i++) {
            double x = d[i];
            Number a = alpha[i];

            if (x <= 1e-15) {
                return 0.0;
            }

            dProd *= Math.pow(x / sumD, a.doubleValue() - 1);
        }

        // check scaling factor
        double sFactor = Math.pow(sumD, -(sumAlpha - d.length));

        double density = (gammaSumAlpha / gammaAlphaProd) * dProd * sFactor;
        return density;
    }

    @Override
    public Map<String,Value> getParams() {
        return new TreeMap<>() {{
            if (concentration!= null) put(concParamName, concentration);
            if (mean != null) put(meanName, mean);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(concParamName)) concentration = value;
        else if (paramName.equals(meanName)) mean = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);

        super.setParam(paramName, value); // constructDistribution
    }

    public Value<Number[]> getConcentration() {
        return concentration;
    }

    public Value<Number> getMean() {
        return mean;
    }

}