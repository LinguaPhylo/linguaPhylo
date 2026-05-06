package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * mtMam empirical amino acid substitution model for mammalian mitochondrial proteins
 * (Yang, Nielsen &amp; Hasegawa 1998; Cao et al. 1998). Rates and frequencies loaded
 * byte-exactly from {@code aa-models/mtmam.dat} (PAML distribution).
 */
@Citation(value = "Yang, Z., Nielsen, R., & Hasegawa, M. (1998). " +
        "Models of amino acid substitution and applications to mitochondrial protein evolution. " +
        "Molecular Biology and Evolution, 15(12), 1600-1611.",
        title = "Models of amino acid substitution and applications to mitochondrial protein evolution",
        year = 1998,
        authors = {"Yang", "Nielsen", "Hasegawa"},
        DOI = "https://doi.org/10.1093/oxfordjournals.molbev.a025888")
public class MtMam extends EmpiricalAminoAcidModel {

    public MtMam(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to mtMam empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "mtMam", verbClause = "is", narrativeName = "mtMam model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"mtMamCoalescent.lphy"},
            description = "The mtMam instantaneous rate matrix for amino acids in mammalian mitochondrial proteins (Yang et al. 1998).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("mtmam").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("mtmam").pi; }
}
