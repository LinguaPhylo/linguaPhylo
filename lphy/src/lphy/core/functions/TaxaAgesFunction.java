package lphy.core.functions;

import lphy.evolution.TaxaAges;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class TaxaAgesFunction extends DeterministicFunction<TaxaAges> {

    final String taxaParamName;
    final String agesParamName;

    public TaxaAgesFunction(@ParameterInfo(name = "taxa", description = "the taxa names") Value<String[]> taxa,
                            @ParameterInfo(name = "ages", description = "the ages of the taxa") Value<Double[]> ages) {
        taxaParamName = getParamName(0);
        agesParamName = getParamName(1);
        setParam(taxaParamName, taxa);
        setParam(agesParamName, ages);
    }

    @GeneratorInfo(name="taxaAges",description = "A set of taxa with ages defined in parallel arrays.")
    public Value<TaxaAges> apply() {
        Value<String[]> taxa = (Value<String[]>)getParams().get(taxaParamName);
        Value<Double[]> ages = (Value<Double[]>)getParams().get(agesParamName);

        TaxaAges taxaAges = new TaxaAges() {
            @Override
            public Double[] getAges() {
                return ages.value();
            }

            @Override
            public int ntaxa() {
                return taxa.value().length;
            }

            public String[] getTaxa() {
                return taxa.value();
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for (int i = 0; i < taxa.value().length; i++) {
                    if (i != 0) builder.append(", ");
                    builder.append(taxa.value()[i]);
                    builder.append("=");
                    builder.append(ages.value()[i]);
                }
                builder.append("};");
                return builder.toString();
            }
        };

        return new Value<>(null, taxaAges, this);
    }
}
