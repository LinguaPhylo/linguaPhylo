package lphy.base.bmodeltest;

import lphy.base.distribution.DiscretizedGamma;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.system.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.base.distribution.DistributionConstants.shapeParamName;
import static lphy.base.evolution.likelihood.PhyloCTMC.LParamName;
import static lphy.core.model.ValueUtils.doubleValue;

public class bSiteRates implements GenerativeDistribution<Double[]> {

    public static final String ncatParamName = "ncat";
    public static final String useShapeParamName = "useShape";
    public static final String useProportionInvariableParamName = "useProportionInvariable";
    public static final String proportionInvariableParamName = "proportionInvariable";

    RandomGenerator random = RandomUtils.getRandom();

    Value<Number> shape;
    Value<Integer> ncat;
    Value<Integer> L;
    Value<Number> proportionInvariable;
    Value<Boolean> useShape, useProportionInvariable;

    public bSiteRates(
            @ParameterInfo(name = shapeParamName, narrativeName = "Gamma shape parameter", description = "the shape parameter of the discretized Gamma distribution.") Value<Number> shape,
            @ParameterInfo(name = ncatParamName, narrativeName = "number of categories", description = "the number of categories of the discretized Gamma distribution.") Value<Integer> ncat,
            @ParameterInfo(name = LParamName, narrativeName = "number of sites", description = "the number of sites to simulate.") Value<Integer> L,
            @ParameterInfo(name = proportionInvariableParamName, narrativeName = "proportion of invariable sites", description = "the proportion of invariable sites parameter") Value<Number> proportionInvariable,
            @ParameterInfo(name = useShapeParamName, narrativeName = "shape parameter indicator", description = "true if the non-zero site rates follow a discretized Gamma distribution.") Value<Boolean> useShape,
            @ParameterInfo(name = useProportionInvariableParamName, narrativeName = "use proportional indicator", description = "true if there is a proportion of invariable sites.") Value<Boolean> useProportionInvariable) {

        this.shape = shape;
        this.ncat = ncat;
        this.L = L;
        this.proportionInvariable = proportionInvariable;
        this.useShape = useShape;
        this.useProportionInvariable = useProportionInvariable;

    }

    @GeneratorInfo(name = "bSiteRates", verbClause = "is",
            category = GeneratorCategory.SITE_MODEL, examples = {"simpleBModelTest2.lphy"},
            description = "the site rates for the given bModelTest parameters.")
    public RandomVariable<Double[]> sample() {

        Double[] siteRates = new Double[L.value()];
        Double pInv = 0.0;

        boolean hasShape = useShape.value();

        if (hasShape) {
            DiscretizedGamma discretizedGamma = new DiscretizedGamma(shape, ncat);
            for (int i = 0; i < siteRates.length; i++) {
                siteRates[i] = discretizedGamma.sample().value();
            }
        } else {
            Arrays.fill(siteRates, 1.0);
        }

        if (useProportionInvariable.value()) {
            pInv = doubleValue(proportionInvariable);
        }

        for (int i = 0; i < siteRates.length; i++) {
            if (pInv > 0 && random.nextDouble()<pInv) {
                siteRates[i] = 0.0;
            }
        }

        return new RandomVariable<>(null, siteRates, this);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(shapeParamName, shape);
        map.put(ncatParamName, ncat);
        map.put(LParamName, L);
        map.put(proportionInvariableParamName, proportionInvariable);
        map.put(useShapeParamName, useShape);
        map.put(useProportionInvariableParamName, useProportionInvariable);
        return map;
    }

    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case shapeParamName:
                shape = value;
                break;
            case ncatParamName:
                ncat = value;
                break;
            case LParamName:
                L = value;
                break;
            case proportionInvariableParamName:
                proportionInvariable = value;
                break;
            case useShapeParamName:
                useShape = value;
                break;
            case useProportionInvariableParamName:
                useProportionInvariable = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }
}
