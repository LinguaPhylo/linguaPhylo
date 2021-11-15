package lphy.core.lightweight.distributions;

import lphy.core.lightweight.LGenerativeDistribution;
import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;

/**
 * Created by Alexei Drummond on 18/12/19.
 */
public class LogNormal implements LGenerativeDistribution<Double> {

    private Double M;
    private Double S;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = "meanlog", description = "the mean of the distribution on the log scale.") Double M,
                     @ParameterInfo(name = "sdlog", description = "the standard deviation of the distribution on the log scale.") Double S) {

        this.M = M;
        this.S = S;
    }

    private void setup() { logNormalDistribution = new LogNormalDistribution(M, S); }

    @GeneratorInfo(name="LogNormal", description="The log-normal probability distribution.")
    public Double sample() {
        return logNormalDistribution.sample();
    }

    public double logDensity(Double x) {
        return logNormalDistribution.logDensity(x);
    }

    public Double getM() {
        return M;
    }

    public Double getS() {
        return S;
    }

    public void setM(Double m) {
        if (!M.equals(m)) {
            M = m;
            setup();
        }
    }

    public void setS(Double s) {
        if (!S.equals(s)) {
            S = s;
            setup();
        }
    }
}