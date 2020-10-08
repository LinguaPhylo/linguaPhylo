package lphy.core.functions;

import lphy.evolution.NChar;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.IntegerArrayValue;
import lphy.graphicalModel.types.IntegerValue;

public class NCharFunction extends DeterministicFunction {

    private static final String sitesParamName = "sites";

    public NCharFunction(@ParameterInfo(name = sitesParamName, description = "a site-dimensioned object (e.g. alignment) or an array of site-dimensioned objects.") Value sites) {
        setParam(sitesParamName, sites);
    }

    @GeneratorInfo(name="nchar",description = "The number of sites in the given alignment(s).")
    public Value apply() {
        Value sites = getParams().get(sitesParamName);
        Object value = sites.value();

        if (value instanceof NChar) {
            return new IntegerValue( ((NChar)value).nchar(), this);
        } else if (value instanceof NChar[]) {
            NChar[] nChars = (NChar[])value;
            Integer[] siteCounts = new Integer[nChars.length];
            for (int i = 0; i < nChars.length; i++) {
                siteCounts[i] = nChars[i].nchar();
            }

            return new IntegerArrayValue(null, siteCounts, this);
        } else throw new IllegalArgumentException("the nchar function can only take type NChar or NChar[] as a value input!");
    }
}
