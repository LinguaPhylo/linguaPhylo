package lphy.base.function.taxa;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.model.component.Value;

import java.util.ArrayList;
import java.util.List;

public class ExtantTaxa extends DeterministicFunction<Taxa> {

    private static final String taxaParamName = "taxa";

    public ExtantTaxa(@ParameterInfo(name = taxaParamName, description = "the taxa-dimensioned value.") Value<Taxa> x) {
        setParam(taxaParamName, x);
    }

    @GeneratorInfo(name="extantTaxa",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Returns the extant taxa from the given taxa object.")
    public Value<Taxa> apply() {
        Value<Taxa> v = (Value<Taxa>)getParams().get(taxaParamName);

        List<Taxon> taxonList = new ArrayList<>();
        for (Taxon taxon : v.value().getTaxonArray()) {
            if (taxon.isExtant()) taxonList.add(taxon);
        }
        Taxa.Simple extantTaxa = new Taxa.Simple(taxonList.toArray(new Taxon[0]));

        return new Value(null, extantTaxa, this);
    }
}
