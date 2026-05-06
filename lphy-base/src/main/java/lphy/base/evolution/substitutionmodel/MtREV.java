package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * mtREV empirical amino acid substitution model for proteins encoded by mitochondrial DNA
 * (Adachi &amp; Hasegawa 1996). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/mtREV24.dat} (PAML distribution; the 24-taxon version).
 */
@Citation(value = "Adachi, J., & Hasegawa, M. (1996). " +
        "Model of amino acid substitution in proteins encoded by mitochondrial DNA. " +
        "Journal of Molecular Evolution, 42(4), 459-468.",
        title = "Model of amino acid substitution in proteins encoded by mitochondrial DNA",
        year = 1996,
        authors = {"Adachi", "Hasegawa"},
        DOI = "https://doi.org/10.1007/BF02498640")
public class MtREV extends EmpiricalAminoAcidModel {

    public MtREV(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to mtREV empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "mtREV", verbClause = "is", narrativeName = "mtREV model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"mtREVCoalescent.lphy"},
            description = "The mtREV instantaneous rate matrix for amino acids in mitochondrial proteins (Adachi & Hasegawa 1996).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("mtREV24").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("mtREV24").pi; }
}
