package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.io.TaxaAttr;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class GuessAges extends DeterministicFunction<Taxa> {

    private final String paramName;
    private final String regxParamName;
    private final String ageTypeParamName;

    public GuessAges(@ParameterInfo(name = "taxa", description = "the taxa value (i.e. alignment or tree).") Value<Taxa> taxa,
                     @ParameterInfo(name = "regx", description = "Java regular expression to extract dates.") Value<String> regx,
                     @ParameterInfo(name = "ageType", description = "age type (i.e. forward, backward, age).") Value<String> ageType) {
        paramName = getParamName(0);
        regxParamName = getParamName(1);
        ageTypeParamName = getParamName(2);
        setParam(paramName, taxa);
        setParam(regxParamName, regx);
        setParam(ageTypeParamName, ageType);
    }

    @GeneratorInfo(name="guessAges",description = "Use regular expression to guess the ages from taxa names.")
    public Value<Taxa> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(paramName);
        Value<String> regx = getParams().get(regxParamName);
        Value<String> ageType = getParams().get(ageTypeParamName);

        Taxa rawTaxa = v.value();

        TaxaAttr taxaAttr = new TaxaAttr(rawTaxa.getTaxaNames(), regx.value(), ageType.value());

        return new Value<>( null, taxaAttr, this);
    }
}
