package lphy.core.functions;

import lphy.evolution.DataFrame;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class DataFrameConstruction extends DeterministicFunction<DataFrame> {

    String partsParamName;
    String ncharParamName;
    String ntaxaParamName;

    public DataFrameConstruction(@ParameterInfo(name = "parts", description = "the names of the partitions, if there is more than one.", optional=true) Value<String[]> parts,
                                 @ParameterInfo(name = "nchar", description = "the number of characters in each partition of the data frame.") Value nchar,
                                 @ParameterInfo(name = "ntaxa", description = "the number of taxa in the data frame.") Value<Integer> ntaxa) {
        partsParamName = getParamName(0);
        ncharParamName = getParamName(1);
        ntaxaParamName = getParamName(2);
        if (parts != null) setParam(partsParamName, parts);
        setParam(ncharParamName, nchar);
        setParam(ntaxaParamName, ntaxa);
    }

    @GeneratorInfo(name = "dataframe", description = "Constructs a data frame which describes the dimensions of the observations.")
    public Value<DataFrame> apply() {
        Value<String[]> parts = getParams().get(partsParamName);
        Value nchar = getParams().get(ncharParamName);
        Value<Integer> ntaxa = getParams().get(ntaxaParamName);

        if (nchar.value() instanceof Integer) {
            return new Value<>(null, new DataFrame(ntaxa.value(), (Integer)nchar.value()), this);
        } else if (nchar.value() instanceof Integer[]) {
            return new Value<>(null, new DataFrame(ntaxa.value(), parts.value(), (Integer[])nchar.value()), this);
        } else throw new IllegalArgumentException(ncharParamName + " must be of type Integer or Integer[]");
    }
}
