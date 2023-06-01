package lphy.base.functions.taxa;

import lphy.base.evolution.Taxa;
import lphy.core.model.components.DeterministicFunction;
import lphy.core.model.components.GeneratorInfo;
import lphy.core.model.components.ParameterInfo;
import lphy.core.model.components.Value;
import lphy.core.model.types.IntegerValue;

@Deprecated
public class NTaxaFunction extends DeterministicFunction<Integer> {

    private static final String taxaParamName = "taxa";

    public NTaxaFunction(@ParameterInfo(name = taxaParamName, description = "the taxa-dimensioned value.") Value<Taxa> x) {
        setParam(taxaParamName, x);
    }

    @Deprecated
    @GeneratorInfo(name="ntaxa",description = "The number of taxa in the given taxa-dimensioned value (e.g. alignment, tree et cetera).")
    public Value<Integer> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(taxaParamName);
        return new IntegerValue( v.value().ntaxa(), this);
    }
}
