package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringArrayValue;

public class TaxaFunction extends DeterministicFunction<Taxa> {

    final String paramName;

    public TaxaFunction(@ParameterInfo(name = "taxa", description = "the taxa value (i.e. alignment or tree).") Value<Taxa> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="taxa",description = "The taxa of the given taxa-dimensioned object (e.g. alignment, tree et cetera).")
    public Value<Taxa> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(paramName);
        return new Value<>( null, v.value(), this);
    }
}
