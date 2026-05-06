package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * mtArt empirical amino acid substitution model for arthropod mitochondrial proteins
 * (Abascal, Posada &amp; Zardoya 2007). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/mtArt.dat} (PAML distribution).
 */
@Citation(value = "Abascal, F., Posada, D., & Zardoya, R. (2007). " +
        "MtArt: a new model of amino acid replacement for Arthropoda. " +
        "Molecular Biology and Evolution, 24(1), 1-5.",
        title = "MtArt: a new model of amino acid replacement for Arthropoda",
        year = 2007,
        authors = {"Abascal", "Posada", "Zardoya"},
        DOI = "https://doi.org/10.1093/molbev/msl136")
public class MtArt extends EmpiricalAminoAcidModel {

    public MtArt(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to mtArt empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "mtArt", verbClause = "is", narrativeName = "mtArt model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"mtArtCoalescent.lphy"},
            description = "The mtArt instantaneous rate matrix for amino acids in arthropod mitochondrial proteins (Abascal et al. 2007).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("mtArt").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("mtArt").pi; }
}
