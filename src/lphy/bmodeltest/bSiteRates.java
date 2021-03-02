package lphy.bmodeltest;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static lphy.evolution.likelihood.PhyloCTMC.siteRatesParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

public class bSiteRates implements GenerativeDistribution<Double[]> {

    public static final String useSiteRatesParamName = "useSiteRates";
    public static final String useProportionInvariableParamName = "useProportionInvariable";
    public static final String proportionInvariableParamName = "proportionInvariable";

    RandomGenerator random = lphy.core.distributions.Utils.getRandom();

    Value<Double[]> rawSiteRates;
    Value<Number> proportionInvariable;
    Value<Boolean> useSiteRates, useProportionInvariable;

    public bSiteRates(
            @ParameterInfo(name = siteRatesParamName, narrativeName = "site rates", description = "raw site rates.") Value<Double[]> siteRates,
            @ParameterInfo(name = proportionInvariableParamName, narrativeName = "proportion of invariable sites", description = "the proportion of invariable sites parameter") Value<Number> proportionInvariable,
            @ParameterInfo(name = useSiteRatesParamName, narrativeName = "site rate heterogeneity indicator", description = "true if the site rates have heterogeneity.") Value<Boolean> useSiteRates,
            @ParameterInfo(name = useProportionInvariableParamName, narrativeName = "use proportional indicator", description = "true if the proportion invariable used.") Value<Boolean> useProportionInvariable) {

        rawSiteRates = siteRates;
        this.proportionInvariable = proportionInvariable;
        this.useSiteRates = useSiteRates;
        this.useProportionInvariable = useProportionInvariable;

    }

    @GeneratorInfo(name = "bSiteRates", verbClause = "is", description = "the site rates for the given bModelTest parameters.")
    public RandomVariable<Double[]> sample() {

        Double[] siteRates = null;
        Double pInv = 0.0;

        boolean hasSiteRates = useSiteRates.value();

        if (hasSiteRates) {
            siteRates = rawSiteRates.value();
        }

        if (useProportionInvariable.value()) {
            pInv = doubleValue(proportionInvariable);
        }

        Double[] bSiteRates = new Double[rawSiteRates.value().length];
        for (int i = 0; i < bSiteRates.length; i++) {
            if (pInv > 0 && random.nextDouble()<pInv) {
                bSiteRates[i] = 0.0;
            } else if (hasSiteRates) {
                bSiteRates[i] = siteRates[i];
            } else {
                bSiteRates[i] = 1.0;
            }
        }

        return new RandomVariable<>(null, bSiteRates, this);
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(siteRatesParamName, rawSiteRates);
        map.put(proportionInvariableParamName, proportionInvariable);
        map.put(useSiteRatesParamName, useSiteRates);
        map.put(useProportionInvariableParamName, useProportionInvariable);
        return map;
    }

    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case siteRatesParamName:
                rawSiteRates = value;
                break;
            case proportionInvariableParamName:
                proportionInvariable = value;
                break;
            case useSiteRatesParamName:
                useSiteRates = value;
                break;
            case useProportionInvariableParamName:
                useProportionInvariable = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }
}
