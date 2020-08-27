package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;
import lphy.graphicalModel.types.StringArrayValue;

public class TaxaFunction extends DeterministicFunction<String[]> {

    final String paramName;

    public TaxaFunction(@ParameterInfo(name = "taxa", description = "the taxa value (i.e. alignment or tree).") Value<Taxa> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="taxa",description = "The taxa in the given taxa-dimensioned value (e.g. alignment, tree et cetera).")
    public Value<String[]> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(paramName);
        return new StringArrayValue( null, v.value().getTaxa(), this);
    }
}
