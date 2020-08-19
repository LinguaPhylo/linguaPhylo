package lphy.core.functions;

import lphy.evolution.DataFrame;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class DataFrameFunction extends DeterministicFunction<DataFrame> {

    String ntaxaParamName;
    String ncharParamName;

    public DataFrameFunction(@ParameterInfo(name = "ntaxa", description = "the number of taxa in the data frame.") Value<Integer> ntaxa,
                             @ParameterInfo(name = "nchar", description = "the number of characters in the data frame.") Value<Integer> nchar) {
        ntaxaParamName = getParamName(0);
        ncharParamName = getParamName(1);
        setParam(ntaxaParamName, ntaxa);
        setParam(ncharParamName, nchar);
    }

    @GeneratorInfo(name = "dataframe", description = "Constructs a data frame which describes the dimensions of the observations.")
    public Value<DataFrame> apply() {
        Value<Integer> ntaxa = getParams().get(ntaxaParamName);
        Value<Integer> nchar = getParams().get(ncharParamName);

        return new Value<>("x", new DataFrame(ntaxa.value(), nchar.value()), this);
    }
}
