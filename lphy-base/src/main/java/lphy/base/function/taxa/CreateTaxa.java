package lphy.base.function.taxa;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;
import lphy.core.model.component.argument.ParameterInfo;

import java.lang.reflect.Array;

public class CreateTaxa extends DeterministicFunction<Taxa> {

    public static final String taxaParamName = "names";
    public static final String speciesParamName = "species";
    public static final String agesParamName = "ages";

    public CreateTaxa(@ParameterInfo(name = taxaParamName, description = "an array of objects representing taxa names") Value taxaNames,
                      @ParameterInfo(name = speciesParamName, description = "an array of objects representing species names", optional=true) Value<Object[]> species,
                      @ParameterInfo(name = agesParamName, description = "the ages of the taxa", optional=true) Value<Double[]> ages) {
        setParam(taxaParamName, taxaNames);
        if (species != null) setParam(speciesParamName, species);
        if (ages != null) setParam(agesParamName, ages);
    }

    @GeneratorInfo(name="taxa",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            examples = {"jcCoalescent.lphy","simpleMultispeciesCoalescentTaxa.lphy"},
            description = "A set of taxa with species and ages defined in parallel arrays.")
    public Value<Taxa> apply() {
        Value names = getParams().get(taxaParamName);
        Value speciesValue = getParams().get(speciesParamName);
        Value<Double[]> agesValue = (Value<Double[]>)getParams().get(agesParamName);

        if (names.value().getClass().isArray()) {

            if (speciesValue == null || speciesValue.value().getClass().isArray()) {

                Taxon[] taxonArray = new Taxon[Array.getLength(names.value())];

                for (int i = 0; i < taxonArray.length; i++) {

                    String name = Array.get(names.value(), i).toString();
                    String species = null;
                    if (speciesValue != null) {
                        species = Array.get(speciesValue.value(), i).toString();
                    }
                    double age = 0.0;
                    if (agesValue != null) {
                        age = agesValue.value()[i];
                    }
                    taxonArray[i] = new Taxon(name, species, age);
                }

                Taxa taxa = new Taxa.Simple(taxonArray);

                return new Value<>(null, taxa, this);
            } else {
                throw new IllegalArgumentException(speciesParamName + " must be an array.");
            }
        } else throw new IllegalArgumentException(taxaParamName + " must be an array.");
    }
}
