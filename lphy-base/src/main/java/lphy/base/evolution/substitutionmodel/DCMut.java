package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * DCMut empirical amino acid substitution model — a re-estimated Dayhoff matrix
 * (Kosiol &amp; Goldman 2005). Rates and frequencies loaded byte-exactly from
 * {@code aa-models/dayhoff-dcmut.dat} (PAML distribution).
 */
@Citation(value = "Kosiol, C., & Goldman, N. (2005). " +
        "Different versions of the Dayhoff rate matrix. " +
        "Molecular Biology and Evolution, 22(2), 193-199.",
        title = "Different versions of the Dayhoff rate matrix",
        year = 2005,
        authors = {"Kosiol", "Goldman"},
        DOI = "https://doi.org/10.1093/molbev/msi005")
public class DCMut extends EmpiricalAminoAcidModel {

    public DCMut(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to DCMut empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "dcmut", verbClause = "is", narrativeName = "DCMut model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"dcmutCoalescent.lphy"},
            description = "The DCMut instantaneous rate matrix for amino acids — a re-estimation of the Dayhoff matrix (Kosiol & Goldman 2005).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("dayhoff-dcmut").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("dayhoff-dcmut").pi; }
}
