package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * LG empirical amino acid substitution model
 * (Le &amp; Gascuel 2008). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/lg.dat} (PAML distribution).
 */
@Citation(value = "Le, S. Q., & Gascuel, O. (2008). " +
        "An improved general amino acid replacement matrix. " +
        "Molecular Biology and Evolution, 25(7), 1307-1320.",
        title = "An improved general amino acid replacement matrix",
        year = 2008,
        authors = {"Le", "Gascuel"},
        DOI = "https://doi.org/10.1093/molbev/msn067")
public class LG extends EmpiricalAminoAcidModel {

    public LG(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to LG empirical frequencies.",
            optional = true) Value<Double[]> freq,
              @ParameterInfo(name = RateMatrix.meanRateParamName,
                      description = "the mean rate of the process. default 1.0",
                      optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "lg", verbClause = "is", narrativeName = "LG model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"lgCoalescent.lphy"},
            description = "The LG instantaneous rate matrix for amino acids (Le & Gascuel 2008).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("lg").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("lg").pi; }
}
