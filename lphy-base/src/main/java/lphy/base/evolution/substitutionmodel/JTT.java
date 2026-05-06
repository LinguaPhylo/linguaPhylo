package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * JTT empirical amino acid substitution model
 * (Jones, Taylor &amp; Thornton 1992). Rates and frequencies loaded byte-exactly
 * from {@code aa-models/jones.dat} (PAML distribution; PAML's update of the
 * 1992 paper using a larger SWISSPROT data set provided by D. Jones).
 */
@Citation(value = "Jones, D. T., Taylor, W. R., & Thornton, J. M. (1992). " +
        "The rapid generation of mutation data matrices from protein sequences. " +
        "Computer Applications in the Biosciences, 8(3), 275-282.",
        title = "The rapid generation of mutation data matrices from protein sequences",
        year = 1992,
        authors = {"Jones", "Taylor", "Thornton"},
        DOI = "https://doi.org/10.1093/bioinformatics/8.3.275")
public class JTT extends EmpiricalAminoAcidModel {

    public JTT(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to JTT empirical frequencies.",
            optional = true) Value<Double[]> freq,
               @ParameterInfo(name = RateMatrix.meanRateParamName,
                       description = "the mean rate of the process. default 1.0",
                       optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "jtt", verbClause = "is", narrativeName = "JTT model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"jttCoalescent.lphy"},
            description = "The JTT instantaneous rate matrix for amino acids (Jones, Taylor & Thornton 1992).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() { return loadPamlDat("jones").R; }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() { return loadPamlDat("jones").pi; }
}
