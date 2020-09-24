package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class TaxaFunction extends DeterministicFunction<Taxa> {

    final String paramName;

    public TaxaFunction(@ParameterInfo(name = "taxa", description = "the taxa value (i.e. alignment or tree).") Value<Taxa> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="taxa",description = "The taxa of the given taxa-dimensioned object (e.g. alignment, tree et cetera).")
    public Value<Taxa> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(paramName);

        Taxa rawTaxa = v.value();

        Taxa wrappedTaxa = new Taxa() {
            @Override
            public int ntaxa() {
                return rawTaxa.ntaxa();
            }

            @Override
            public Double[] getAges() {
                return rawTaxa.getAges();
            }

            @Override
            public String[] getSpecies() {
                return rawTaxa.getSpecies();
            }

            @Override
            public String[] getTaxaNames() {
                return rawTaxa.getTaxaNames();
            }

            public Taxon[] getTaxa() {
                return rawTaxa.getTaxa();
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                for (Taxon taxon : getTaxa()) {
                    builder.append(taxon.toString());
                    builder.append("\n");
                }
                return builder.toString();
            }
        };

        return new Value<>( null, wrappedTaxa, this);
    }
}
