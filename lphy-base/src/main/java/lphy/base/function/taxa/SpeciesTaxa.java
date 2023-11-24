package lphy.base.function.taxa;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

public class SpeciesTaxa extends DeterministicFunction<Taxa> {

    final String paramName;

    public SpeciesTaxa(@ParameterInfo(name = "0", description = "the taxa object from which to extract the species from.") Value<Taxa> taxa) {
        paramName = getParamName(0);
        setParam(paramName, taxa);
    }

    @GeneratorInfo(name="species",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"simpleMultispeciesCoalescentTaxa.lphy"},
            description = "extract the species from the given taxa object as a new taxa object. " +
            "Useful to generate a species tree in the multispecies coalescent. The age of each species will be the youngest age of the taxa from that species.")
    public Value<Taxa> apply() {
        Value<Taxa> taxa = (Value<Taxa>)getParams().get(paramName);

        Taxa tA = taxa.value();
        String[] species = tA.getSpecies();
        Double[] ages = tA.getAges();

        if (species.length != ages.length)
            throw new IllegalArgumentException("Species length must equal to ages in taxa " + taxa.getId());

        // cannot only use names and ages, must create Taxon object, otherwise view will be broken in studio
        List<Taxon> spTaxList = new ArrayList<>();

        for (int i = 0; i < species.length; i++) {
            List<String> spFromSpTaxa = spTaxList.stream().map(Taxon::getName).toList();
            if (!spFromSpTaxa.contains(species[i])) {
                Taxon spTa = new Taxon(species[i], ages[i]);
                spTaxList.add(spTa);
            } else {
                List<Double> spAges = spTaxList.stream().map(Taxon::getAge).toList();
                int index = spFromSpTaxa.indexOf(species[i]);
                // take the max age
                if (spAges.get(index) > ages[i]) {
                    spTaxList.get(index).setAge(ages[i]);
                }
            }
        }


        Taxa speciesTaxa = new Taxa() {

            @Override
            public Taxon getTaxon(int i) {
                return spTaxList.get(i);
            }

            @Override
            public Taxon[] getTaxonArray() {
                return spTaxList.toArray(Taxon[]::new);
            }

            @Override
            public int getDimension() {
                return ntaxa();
            }

            @Override
            public int ntaxa() {
                return spTaxList.size();
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for (int i = 0; i < spTaxList.size(); i++) {
                    if (i != 0) builder.append(", ");
                    builder.append(spTaxList.get(i).getName());
                    builder.append("=");
                    builder.append(spTaxList.get(i).getAge());
                }
                builder.append("};");
                return builder.toString();
            }
        };

        return new Value<>(null, speciesTaxa, this);
    }
}
