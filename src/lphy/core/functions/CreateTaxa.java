package lphy.core.functions;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class CreateTaxa extends DeterministicFunction<Taxa> {

    public static final String taxaParamName = "names";
    public static final String speciesParamName = "species";
    public static final String agesParamName = "ages";

    public CreateTaxa(@ParameterInfo(name = taxaParamName, description = "the taxa names") Value<String[]> taxa,
                      @ParameterInfo(name = speciesParamName, description = "the species of the taxa", optional=true) Value<String[]> species,
                      @ParameterInfo(name = agesParamName, description = "the ages of the taxa", optional=true) Value<Double[]> ages) {
        setParam(taxaParamName, taxa);
        if (species != null) setParam(speciesParamName, species);
        if (ages != null) setParam(agesParamName, ages);
    }

    @GeneratorInfo(name="taxa",description = "A set of taxa with species and ages defined in parallel arrays.")
    public Value<Taxa> apply() {
        Value<String[]> names = (Value<String[]>)getParams().get(taxaParamName);
        Value<String[]> speciesValue = (Value<String[]>)getParams().get(speciesParamName);
        Value<Double[]> agesValue = (Value<Double[]>)getParams().get(agesParamName);

        Taxon[] taxonArray = new Taxon[names.value().length];

        for (int i = 0; i < taxonArray.length; i++) {

            String name = names.value()[i];
            String species = null;
            if (speciesValue != null) {
                species = speciesValue.value()[i];
            }
            double age = 0.0;
            if (agesValue != null) {
                age = agesValue.value()[i];
            }
            taxonArray[i] = new Taxon(name,species,age);
        }

        Taxa taxa = new Taxa.Simple(taxonArray);

        return new Value<>(null, taxa, this);
    }
}
