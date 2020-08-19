package lphy.core.functions;

import lphy.evolution.NTaxa;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class NTaxaFunction extends DeterministicFunction<Integer> {

    final String paramName;

    public NTaxaFunction(@ParameterInfo(name = "taxa", description = "the taxa-dimensioned value.") Value<NTaxa> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="ntaxa",description = "The number of taxa in the given taxa-dimensioned value (e.g. alignment, tree et cetera).")
    public Value<Integer> apply() {
        Value<NTaxa> v = (Value<NTaxa>)getParams().get(paramName);
        return new IntegerValue( v.value().ntaxa(), this);
    }
}
