package lphy.core.functions;

import lphy.evolution.NChar;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerValue;

public class NCharFunction extends DeterministicFunction<Integer> {

    final String paramName;

    public NCharFunction(@ParameterInfo(name = "sites", description = "the site-dimensioned object (e.g. alignment).") Value<NChar> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="nchar",description = "The number of sites in the given alignment.")
    public Value<Integer> apply() {
        Value<NChar> v = (Value<NChar>)getParams().get(paramName);
        return new IntegerValue( v.value().nchar(), this);
    }
}
