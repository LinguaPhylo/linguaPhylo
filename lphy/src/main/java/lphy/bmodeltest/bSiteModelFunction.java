package lphy.bmodeltest;

import lphy.evolution.sitemodel.SiteModel;
import lphy.graphicalModel.*;

import java.util.Map;

import static lphy.evolution.likelihood.PhyloCTMC.QParamName;
import static lphy.evolution.likelihood.PhyloCTMC.siteRatesParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

@Citation(
        value = "Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6",
        title = "bModelTest: Bayesian phylogenetic site model averaging and model comparison",
        authors = {"Bouckaert", "Drummond"},
        year = 2017,
        DOI = "https://doi.org/10.1186/s12862-017-0890-6"
)
public class bSiteModelFunction extends DeterministicFunction<SiteModel> {

    public static final String useSiteRatesParamName = "useSiteRates";
    public static final String useProportionInvariableParamName = "usePInv";
    public static final String proportionInvariableParamName = "pInv";

    public bSiteModelFunction(
            @ParameterInfo(name = QParamName, narrativeName = "instantaneous rate matrix", description = "the instantaneous rate matrix.") Value<Double[][]> Q,
            @ParameterInfo(name = siteRatesParamName, narrativeName = "site rates", description = "raw site rates.") Value<Double[]> siteRates,
            @ParameterInfo(name = proportionInvariableParamName, narrativeName = "proportion of invariable sites", description = "the proportion of invariable sites parameter") Value<Number> proportionInvariable,
            @ParameterInfo(name = useSiteRatesParamName, narrativeName = "site rate heterogeneity indicator", description = "true if the site rates have heterogeneity.") Value<Boolean> useSiteRates,
            @ParameterInfo(name = useProportionInvariableParamName, narrativeName = "proportion invariable indicator", description = "true if the proportion invariable used.") Value<Boolean> useProportionInvariable) {

        setParam(QParamName, Q);
        setParam(siteRatesParamName, siteRates);
        setParam(proportionInvariableParamName, proportionInvariable);
        setParam(useSiteRatesParamName, useSiteRates);
        setParam(useProportionInvariableParamName, useProportionInvariable);

    }

    @GeneratorInfo(name = "bSiteModel", narrativeName = "bModelTest site model", verbClause = "is",
            category = GeneratorCategory.MODEL_AVE_SEL, examples = {"simpleBModelTest.lphy"},
            description = "Returns the site model for the given parameters.")
    public Value<SiteModel> apply() {

        Map<String, Value> params = getParams();
        Value<Double[][]> Q = (Value<Double[][]>)params.get(QParamName);

        Value<Double[]> siteRatesParam = (Value<Double[]>)params.get(siteRatesParamName);

        Value<Number> proportionInvariableParam = (Value<Number>)params.get(proportionInvariableParamName);

        Value<Boolean> useSiteRates = (Value<Boolean>)params.get(useSiteRatesParamName);


        Value<Boolean> useProportionInvariable = (Value<Boolean>)params.get(useProportionInvariableParamName);



        Double[] siteRates = null;
        Double proportionInvariable = 0.0;

        if (useSiteRates.value()) {
            siteRates = siteRatesParam.value();
        }

        if (useProportionInvariable.value()) {
            proportionInvariable = doubleValue(proportionInvariableParam);
        }

        SiteModel siteModel = new SiteModel(Q.value(), siteRates, proportionInvariable);

        return new Value<>(null, siteModel, this);
    }
}
