package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * WAG empirical amino acid substitution model
 * (Whelan &amp; Goldman 2001). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/wag.dat} (PAML distribution).
 */
@Citation(value = "Whelan, S., & Goldman, N. (2001). " +
        "A general empirical model of protein evolution derived from multiple protein families using a maximum-likelihood approach. " +
        "Molecular Biology and Evolution, 18(5), 691-699.",
        title = "A general empirical model of protein evolution derived from multiple protein families using a maximum-likelihood approach",
        year = 2001,
        authors = {"Whelan", "Goldman"},
        DOI = "https://doi.org/10.1093/oxfordjournals.molbev.a003851")
public class WAG extends EmpiricalAminoAcidModel {

    public WAG(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to WAG empirical frequencies.",
            optional = true) Value<Double[]> freq,
               @ParameterInfo(name = RateMatrix.meanRateParamName,
                       description = "the mean rate of the process. default 1.0",
                       optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "wag", verbClause = "is", narrativeName = "WAG model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"wagCoalescent.lphy"},
            description = "The WAG instantaneous rate matrix for amino acids (Whelan & Goldman 2001).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("wag").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("wag").pi; }
}
