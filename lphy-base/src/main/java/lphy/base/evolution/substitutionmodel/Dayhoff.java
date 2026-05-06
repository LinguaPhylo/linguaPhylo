package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * Dayhoff empirical amino acid substitution model
 * (Dayhoff, Schwartz &amp; Orcutt 1978). Rates and frequencies loaded
 * byte-exactly from {@code aa-models/dayhoff.dat} (PAML distribution).
 */
@Citation(value = "Dayhoff, M. O., Schwartz, R. M., & Orcutt, B. C. (1978). " +
        "A model of evolutionary change in proteins. " +
        "Atlas of Protein Sequence and Structure, 5, 345-352.",
        title = "A model of evolutionary change in proteins",
        year = 1978,
        authors = {"Dayhoff", "Schwartz", "Orcutt"})
public class Dayhoff extends EmpiricalAminoAcidModel {

    public Dayhoff(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to Dayhoff empirical frequencies.",
            optional = true) Value<Double[]> freq,
                   @ParameterInfo(name = RateMatrix.meanRateParamName,
                           description = "the mean rate of the process. default 1.0",
                           optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "dayhoff", verbClause = "is", narrativeName = "Dayhoff model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"dayhoffCoalescent.lphy"},
            description = "The Dayhoff instantaneous rate matrix for amino acids (Dayhoff et al. 1978).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("dayhoff").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("dayhoff").pi; }
}
