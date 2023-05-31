package lphy.base.functions.taxa;

import lphy.base.evolution.ExtractStringMetaData;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.TaxaData;
import lphy.base.evolution.alignment.Alignment;
import lphy.core.graphicalmodel.components.*;

/**
 * {@code trait_D = extractTrait(taxa, "|", 2);}
 * returns a trait alignment extracted from taxa names.
 * @see ExtractStringMetaData
 */
public class ExtractTrait extends DeterministicFunction<Alignment> {

    private final String taxaParamName = "taxa";
    private final String sepParamName = "sep";
    private final String indexParamName = "i";
    private final String traitNameParamName = "name";

    public ExtractTrait(@ParameterInfo(name = taxaParamName, verb = "from", description = "the set of taxa whose names contain the traits.") Value<Taxa> taxa,
                        @ParameterInfo(name = sepParamName, description = "the substring to split the taxa names, " +
                                "where Java regular expression escape characters will be given no special meaning.") Value<String> sepStr,
                        @ParameterInfo(name = indexParamName, description = "i (>=0) is the index to extract the trait value.") Value<Integer> index,
                        @ParameterInfo(name = traitNameParamName, description = "the map containing optional arguments and their values for reuse.",
                                optional=true) Value<String> traitName) {

        setParam(taxaParamName, taxa);
        setParam(sepParamName, sepStr);
        setParam(indexParamName, index);

        if (traitName != null) setParam(traitNameParamName, traitName);
    }


    @GeneratorInfo(name="extractTrait", verbClause = "extracts", narrativeName = "trait",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            examples = {"covidDPG.lphy", "https://linguaphylo.github.io/tutorials/discrete-phylogeography/"},
            description = "return a trait alignment, which contains the set of traits extracted from taxa names.")
    public Value<Alignment> apply() {

        Taxa taxa = ((Value<Taxa>) getParams().get(taxaParamName)).value();
        String sepStr = ((Value<String>) getParams().get(sepParamName)).value();
        Integer index = ((Value<Integer>) getParams().get(indexParamName)).value();
        Value<String> labelVal = getParams().get(traitNameParamName);
        String label = labelVal==null ? "" : labelVal.getUniqueId();

        TaxaData<String> metaData = new ExtractStringMetaData(label, sepStr, index);
        Alignment traitAlg = metaData.extractTraitAlignment(taxa);

        return new Value<>(null, traitAlg, this);

    }

}
