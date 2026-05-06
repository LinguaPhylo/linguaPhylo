package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * cpREV empirical amino acid substitution model for proteins encoded by chloroplast DNA
 * (Adachi et al. 2000). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/cpREV10.dat} (PAML distribution; the 10-taxon version).
 */
@Citation(value = "Adachi, J., Waddell, P. J., Martin, W., & Hasegawa, M. (2000). " +
        "Plastid genome phylogeny and a model of amino acid substitution for proteins encoded by chloroplast DNA. " +
        "Journal of Molecular Evolution, 50(4), 348-358.",
        title = "Plastid genome phylogeny and a model of amino acid substitution for proteins encoded by chloroplast DNA",
        year = 2000,
        authors = {"Adachi", "Waddell", "Martin", "Hasegawa"},
        DOI = "https://doi.org/10.1007/s002399910038")
public class CpREV extends EmpiricalAminoAcidModel {

    public CpREV(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to cpREV empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "cpREV", verbClause = "is", narrativeName = "cpREV model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"cpREVCoalescent.lphy"},
            description = "The cpREV instantaneous rate matrix for amino acids in chloroplast-encoded proteins (Adachi et al. 2000).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("cpREV10").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("cpREV10").pi; }
}
