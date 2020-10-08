package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class NTaxaFunction extends DeterministicFunction<Integer> {

    private static final String taxaParamName = "taxa";

    public NTaxaFunction(@ParameterInfo(name = taxaParamName, description = "the taxa-dimensioned value.") Value<Taxa> x) {
        setParam(taxaParamName, x);
    }

    @GeneratorInfo(name="ntaxa",description = "The number of taxa in the given taxa-dimensioned value (e.g. alignment, tree et cetera).")
    public Value<Integer> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(taxaParamName);
        return new IntegerValue( v.value().ntaxa(), this);
    }
}
