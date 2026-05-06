package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

/**
 * rtREV empirical amino acid substitution model for retroviral reverse transcriptase
 * (Dimmic et al. 2002).
 */
@Citation(value = "Dimmic, M. W., Rest, J. S., Mindell, D. P., & Goldstein, R. A. (2002). " +
        "rtREV: an amino acid substitution matrix for inference of retrovirus and reverse transcriptase phylogeny. " +
        "Journal of Molecular Evolution, 55(1), 65-73.",
        title = "rtREV: an amino acid substitution matrix for inference of retrovirus and reverse transcriptase phylogeny",
        year = 2002,
        authors = {"Dimmic", "Rest", "Mindell", "Goldstein"},
        DOI = "https://doi.org/10.1007/s00239-001-2304-y")
public class RtREV extends EmpiricalAminoAcidModel {

    public RtREV(@ParameterInfo(name = SubstModelParamNames.FreqParamName,
            description = "the base frequencies (alphabetical AA order). Defaults to rtREV empirical frequencies.",
            optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(freq, meanRate);
    }

    @GeneratorInfo(name = "rtREV", verbClause = "is", narrativeName = "rtREV model",
            category = GeneratorCategory.RATE_MATRIX, examples = {"rtREVCoalescent.lphy"},
            description = "The rtREV instantaneous rate matrix for amino acids in retroviral reverse transcriptase (Dimmic et al. 2002).")
    public lphy.core.model.Value<Double[][]> apply() {
        return super.apply();
    }

    @Override
    protected double[][] getEmpiricalRatesPAML() {
        double[][] r = new double[20][20];
        r[1][0] = 34;
        r[2][0] = 51;  r[2][1] = 35;
        r[3][0] = 10;  r[3][1] = 30;  r[3][2] = 384;
        r[4][0] = 439; r[4][1] = 92;  r[4][2] = 128; r[4][3] = 1;
        r[5][0] = 32;  r[5][1] = 221; r[5][2] = 236; r[5][3] = 78;  r[5][4] = 70;
        r[6][0] = 81;  r[6][1] = 10;  r[6][2] = 79;  r[6][3] = 542; r[6][4] = 1;
        r[6][5] = 372;
        r[7][0] = 135; r[7][1] = 41;  r[7][2] = 94;  r[7][3] = 61;  r[7][4] = 48;
        r[7][5] = 18;  r[7][6] = 70;
        r[8][0] = 30;  r[8][1] = 90;  r[8][2] = 320; r[8][3] = 91;  r[8][4] = 124;
        r[8][5] = 387; r[8][6] = 34;  r[8][7] = 68;
        r[9][0] = 1;   r[9][1] = 24;  r[9][2] = 35;  r[9][3] = 1;   r[9][4] = 104;
        r[9][5] = 33;  r[9][6] = 1;   r[9][7] = 1;   r[9][8] = 34;
        r[10][0] = 45; r[10][1] = 18; r[10][2] = 15; r[10][3] = 5;  r[10][4] = 110;
        r[10][5] = 54; r[10][6] = 21; r[10][7] = 3;  r[10][8] = 51; r[10][9] = 385;
        r[11][0] = 38; r[11][1] = 593; r[11][2] = 123; r[11][3] = 20; r[11][4] = 16;
        r[11][5] = 309; r[11][6] = 141; r[11][7] = 30; r[11][8] = 76; r[11][9] = 34;
        r[11][10] = 23;
        r[12][0] = 235; r[12][1] = 57; r[12][2] = 1;  r[12][3] = 1;  r[12][4] = 156;
        r[12][5] = 158; r[12][6] = 1;  r[12][7] = 37; r[12][8] = 116; r[12][9] = 375;
        r[12][10] = 581; r[12][11] = 134;
        r[13][0] = 1;  r[13][1] = 7;  r[13][2] = 49; r[13][3] = 1;  r[13][4] = 70;
        r[13][5] = 1;  r[13][6] = 1;  r[13][7] = 7;  r[13][8] = 141; r[13][9] = 64;
        r[13][10] = 179; r[13][11] = 14; r[13][12] = 247;
        r[14][0] = 97; r[14][1] = 24; r[14][2] = 33; r[14][3] = 55; r[14][4] = 1;
        r[14][5] = 68; r[14][6] = 52; r[14][7] = 17; r[14][8] = 44; r[14][9] = 10;
        r[14][10] = 22; r[14][11] = 43; r[14][12] = 1; r[14][13] = 11;
        r[15][0] = 460; r[15][1] = 102; r[15][2] = 294; r[15][3] = 136; r[15][4] = 75;
        r[15][5] = 225; r[15][6] = 95; r[15][7] = 152; r[15][8] = 183; r[15][9] = 4;
        r[15][10] = 24; r[15][11] = 77; r[15][12] = 1; r[15][13] = 20; r[15][14] = 134;
        r[16][0] = 258; r[16][1] = 64; r[16][2] = 148; r[16][3] = 55; r[16][4] = 117;
        r[16][5] = 146; r[16][6] = 82; r[16][7] = 7;  r[16][8] = 49; r[16][9] = 72;
        r[16][10] = 25; r[16][11] = 110; r[16][12] = 131; r[16][13] = 69; r[16][14] = 62;
        r[16][15] = 671;
        r[17][0] = 5;  r[17][1] = 13; r[17][2] = 16; r[17][3] = 1;  r[17][4] = 55;
        r[17][5] = 10; r[17][6] = 17; r[17][7] = 23; r[17][8] = 48; r[17][9] = 39;
        r[17][10] = 47; r[17][11] = 6; r[17][12] = 111; r[17][13] = 182; r[17][14] = 9;
        r[17][15] = 14; r[17][16] = 1;
        r[18][0] = 55; r[18][1] = 47; r[18][2] = 28; r[18][3] = 1;  r[18][4] = 131;
        r[18][5] = 45; r[18][6] = 1;  r[18][7] = 21; r[18][8] = 307; r[18][9] = 26;
        r[18][10] = 64; r[18][11] = 1; r[18][12] = 74; r[18][13] = 1017; r[18][14] = 14;
        r[18][15] = 31; r[18][16] = 34; r[18][17] = 176;
        r[19][0] = 197; r[19][1] = 29; r[19][2] = 21; r[19][3] = 6;  r[19][4] = 295;
        r[19][5] = 36; r[19][6] = 35; r[19][7] = 3;  r[19][8] = 1;  r[19][9] = 1048;
        r[19][10] = 112; r[19][11] = 19; r[19][12] = 236; r[19][13] = 92; r[19][14] = 25;
        r[19][15] = 39; r[19][16] = 196; r[19][17] = 26; r[19][18] = 59;
        return r;
    }

    @Override
    protected double[] getEmpiricalFrequenciesPAML() {
        return new double[]{
                0.0646, 0.0453, 0.0376, 0.0422, 0.0114,
                0.0606, 0.0607, 0.0639, 0.0273, 0.0679,
                0.1018, 0.0751, 0.015,  0.0287, 0.0681,
                0.0488, 0.0622, 0.0251, 0.0318, 0.0619
        };
    }
}
