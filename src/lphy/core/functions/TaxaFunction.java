package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class TaxaFunction extends DeterministicFunction<Taxa> {

    final String paramName;

    public TaxaFunction(@ParameterInfo(name = "taxa", description = "the taxa value (i.e. alignment or tree).") Value x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @GeneratorInfo(name="taxa",description = "The taxa of the given taxa-dimensioned object (e.g. alignment, tree et cetera).")
    public Value<Taxa> apply() {
        Value v = getParams().get(paramName);
        Object value = v.value();

        Taxa rawTaxa;
        if (value instanceof Taxa) {
            rawTaxa = (Taxa) value;
        } else if (value instanceof Taxa[]) {
            Taxa[] taxaArr = (Taxa[])value;
            rawTaxa = taxaArr[0];
            for (int i = 1; i < taxaArr.length; i++) {
                if (! Arrays.equals(rawTaxa.getTaxaNames(), taxaArr[i].getTaxaNames()))
                    throw new IllegalArgumentException("Taxa[] should have the same names and same order ! " + i);
            }

        } else throw new IllegalArgumentException("the taxa function can only take type Taxa or Taxa[] as a value input!");


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

            public Taxon[] getTaxonArray() {
                return rawTaxa.getTaxonArray();
            }

            public Taxon getTaxon(int i) { return rawTaxa.getTaxon(i); };

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                for (Taxon taxon : getTaxonArray()) {
                    builder.append(taxon.toString());
                    builder.append("\n");
                }
                return builder.toString();
            }
        };

        return new Value<>( null, wrappedTaxa, this);
    }
}
