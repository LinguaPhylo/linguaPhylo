package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;

public class Species extends DeterministicFunction<Taxa> {

    final String paramName;

    public Species(@ParameterInfo(name = "0", description = "the taxa object from which to extract the species from.") Value<Taxa> taxa) {
        paramName = getParamName(0);
        setParam(paramName, taxa);
    }

    @GeneratorInfo(name="species", description = "extract the species from the given taxa object as a new taxa object. " +
            "Useful to generate a species tree in the multispecies coalescent. The age of each species will be the youngest age of the taxa from that species.")
    public Value<Taxa> apply() {
        Value<Taxa> taxa = (Value<Taxa>)getParams().get(paramName);

        Taxa tA = taxa.value();
        String[] species = tA.getSpecies();
        Double[] ages = tA.getAges();

        List<String> spTaxa = new ArrayList<>();
        List<Double> spAges = new ArrayList<>();

        for (int i = 0; i < species.length; i++) {
            if (!spTaxa.contains(species[i])) {
                spTaxa.add(species[i]);
                spAges.add(ages[i]);
            } else {
                int index = spTaxa.indexOf(species[i]);
                if (spAges.get(index) > ages[i]) {
                    spAges.set(index, ages[i]);
                }
            }
        }

        String[] speciesTaxaNames = spTaxa.toArray(new String[0]);
        Double[] speciesAges = spAges.toArray(new Double[0]);

        Taxa speciesTaxa = new Taxa() {
            @Override
            public int getDimension() {
                return ntaxa();
            }

            @Override
            public Double[] getAges() {
                return speciesAges;
            }

            @Override
            public int ntaxa() {
                return speciesTaxaNames.length;
            }

            public String[] getTaxaNames() {
                return speciesTaxaNames;
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for (int i = 0; i < speciesTaxaNames.length; i++) {
                    if (i != 0) builder.append(", ");
                    builder.append(speciesTaxaNames[i]);
                    builder.append("=");
                    builder.append(speciesAges[i]);
                }
                builder.append("};");
                return builder.toString();
            }
        };

        return new Value<>(null, speciesTaxa, this);
    }
}
