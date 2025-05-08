package lphy.base.distribution;

import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirichletTest {
    @Test
    void scale() {
        Number[] conc = new Number[]{2,2,2};
        Value<Number[]> concValue = new Value<>("", conc);
        double mean = 5.0;
        Dirichlet d = new Dirichlet(concValue, new Value<>("", mean));
        Double[] result = d.sample().value();
        double observe = 0;
        for (int i = 0; i< result.length; i++) {
            observe += result[i];
        }

        assertEquals(mean*3, observe, 1e-6);
    }

    @Test
    void standard() {
        Number[] conc = new Number[]{1,1,1};
        Value<Number[]> concValue = new Value<>("", conc);
        Dirichlet d = new Dirichlet(concValue, null);
        Double[] result = d.sample().value();
        double observe = 0;
        for (int i = 0; i< result.length; i++) {
            observe += result[i];
        }

        assertEquals(1, observe, 1e-6);
    }

    @Test
    void densityTest() {
        Number[] conc = new Number[]{1,1};
        Value<Number[]> concValue = new Value<>("", conc);
        Dirichlet d = new Dirichlet(concValue, new Value<>("", 0.5));
        Double[] result = d.sample().value();
        double p = d.density(result);
        double product = 1;
        for (int i = 0; i< result.length; i++) {
            product *= Math.pow(result[i].doubleValue(), 0);
        }
        double exp = (gamma(2.0)/ (gamma(1.0)*gamma(1.0)))*product;
        assertEquals(exp, p, 1e-6);
    }

    @Test
    void scaledDensityTest() {
        Number[] conc = new Number[]{1,1};
        Value<Number[]> concValue = new Value<>("", conc);
        Dirichlet d = new Dirichlet(concValue, new Value<>("", 1));
        Double[] result = d.sample().value();
        double p = d.density(result);
        double product = 1;
        for (int i = 0; i< result.length; i++) {
            product *= Math.pow(result[i].doubleValue(), 0);
        }
        double sFactor = Math.pow(2, -(2 - result.length));
        double exp = (gamma(2.0)/ (gamma(1.0)*gamma(1.0)))*product*sFactor;
        assertEquals(exp, p, 1e-6);
    }

    public double gamma(double x){
        return org.apache.commons.math3.special.Gamma.gamma(x);
    }
}
