package lphy.base.distribution;

import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

public class DirichletMultinomial extends ParametricDistribution<Integer[]>{
    private Value<Double[]> p;
    private Value<Number> w;
    private Value<Integer> n;

    private Multinomial multinomial;
    private Dirichlet dirichlet;


    public DirichletMultinomial(
            @ParameterInfo(name = DistributionConstants.pParamName, description = "event probabilities.") Value<Double[]> p,
            @ParameterInfo(name = DistributionConstants.wParamName, description = "overdispersion parameter of Dirichlet multinomial distribution.") Value<Number> w,
            @ParameterInfo(name = DistributionConstants.nParamName, description = "number of trials.") Value<Integer> n
            ){
        super();
        this.p = p;
        this.w = w;
        this.n = n;

        constructDistribution(random);
    }

    public DirichletMultinomial(){
        constructDistribution(random);
    }


    @Override
    protected void constructDistribution(RandomGenerator random) {

    }

    @GeneratorInfo(name = "DirichletMultinomial", verbClause = "has", narrativeName = "dirichlet multinomial distribution",
            category = GeneratorCategory.PRIOR,
            description = "The dirichlet multinomial distribution.")

    @Override
    public RandomVariable<Integer[]> sample() {
        Value<Number[]> concentration;
        Number [] concentrationv = new Number[p.value().length];
        for (int i = 0; i < this.p.value().length; i++) {
            concentrationv[i] = this.w.value().doubleValue() * this.p.value()[i];
        }
        concentration = new Value<>("concentration", concentrationv);
        this.dirichlet = new Dirichlet(concentration, null);
        this.multinomial = new Multinomial();
        this.multinomial.setParam("n", this.n);
        Value<Double[]> prob;
        prob = this.dirichlet.sample();
        this.multinomial.setParam("p", prob);
        Integer[] result;
        result = this.multinomial.sample().value();
        return new RandomVariable<>(null, result, this);
    }



    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(DistributionConstants.pParamName, p);
            put(DistributionConstants.wParamName, w);
            put(DistributionConstants.nParamName, n);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case DistributionConstants.nParamName:
                n = value;
                break;
            case DistributionConstants.pParamName:
                p = value;
                break;
            case DistributionConstants.wParamName:
                w = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

//        super.setParam(paramName, value); // constructDistribution
    }
    public String toString() {
        return getName();
    }


}
