package lphy.core.functions;

import lphy.evolution.TaxaAges;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class TaxaAgesFromFunction extends DeterministicFunction<TaxaAges> {

    final String paramName;

    public TaxaAgesFromFunction(@ParameterInfo(name = "0", description = "the data containing taxa and ages of the taxa") Value<TaxaAges> taxaAgesValue) {
        paramName = getParamName(0);
        setParam(paramName, taxaAgesValue);
    }

    @GeneratorInfo(name="taxaAges", description = "taxa and ages of the taxa.")
    public Value<TaxaAges> apply() {
        Value<TaxaAges> taxaAgesValue = (Value<TaxaAges>)getParams().get(paramName);

        TaxaAges tA = taxaAgesValue.value();
        String[] taxa = tA.getTaxa();
        Double[] ages = tA.getAges();


        TaxaAges taxaAges = new TaxaAges() {
            @Override
            public int getDimension() {
                return ntaxa();
            }

            @Override
            public Double[] getAges() {
                return ages;
            }

            @Override
            public int ntaxa() {
                return taxa.length;
            }

            public String[] getTaxa() {
                return taxa;
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for (int i = 0; i < taxa.length; i++) {
                    if (i != 0) builder.append(", ");
                    builder.append(taxa[i]);
                    builder.append("=");
                    builder.append(ages[i]);
                }
                builder.append("};");
                return builder.toString();
            }
        };

        return new Value<>(null, taxaAges, this);
    }
}
