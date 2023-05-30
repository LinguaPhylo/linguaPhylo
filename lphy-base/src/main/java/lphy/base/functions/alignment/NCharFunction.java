package lphy.base.functions.alignment;

import lphy.base.evolution.NChar;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import lphy.core.graphicalmodel.components.ParameterInfo;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.types.IntegerValue;
@Deprecated
public class NCharFunction extends DeterministicFunction<Integer> {

    private static final String sitesParamName = "sites";

    public NCharFunction(@ParameterInfo(name = sitesParamName, verb = "of", description = "a site-dimensioned object (e.g. alignment) or an array of site-dimensioned objects.") Value<NChar> sites) {
        setParam(sitesParamName, sites);
    }

    @Deprecated
    @GeneratorInfo(name = "nchar", verbClause = "is", narrativeName = "number of characters", description = "The number of sites in the given alignment.")
    public Value<Integer> apply() {
        Value<NChar> sites = getParams().get(sitesParamName);
        NChar value = sites.value();
        return new IntegerValue(value.nchar(), this);
    }
}