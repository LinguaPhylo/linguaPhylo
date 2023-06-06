package lphy.base.function.taxa;

import lphy.base.evolution.Taxa;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.parser.argument.ParameterInfo;

// use CreateTaxa
@Deprecated
public class TaxaAgesFromFunction extends DeterministicFunction<Taxa> {

    public static final String paramName = "0";

    public TaxaAgesFromFunction(@ParameterInfo(name = paramName, description = "the data containing taxa and ages of the taxa") Value<Taxa> taxaAgesValue) {
        setParam(paramName, taxaAgesValue);
    }

    @GeneratorInfo(name="taxaAges", description = "taxa and ages of the taxa.")
    public Value<Taxa> apply() {
        Value<Taxa> taxaAgesValue = (Value<Taxa>)getParams().get(paramName);

        Taxa tA = taxaAgesValue.value();
        String[] taxa = tA.getTaxaNames();
        Double[] ages = tA.getAges();


        Taxa taxaAges = new Taxa() {
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

            public String[] getTaxaNames() {
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
