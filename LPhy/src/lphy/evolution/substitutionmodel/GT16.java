package lphy.evolution.substitutionmodel;

import lphy.graphicalModel.Citation;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleArray2DValue;

/**
 * Created by adru001 on 2/02/20.
 */
@Citation(
        value = "Alexey Kozlov, Joao Alves, Alexandros Stamatakis, David Posada (2021). CellPhy: accurate and fast probabilistic inference of single-cell phylogenies from scDNA-seq data. bioRxiv 2020.07.31.230292.",
        title = "CellPhy: accurate and fast probabilistic inference of single-cell phylogenies from scDNA-seq data",
        authors = {"Kozlov", "Alves", "Stamatakis", "Posada"},
        year = 2021,
        DOI = "https://doi.org/10.1101/2020.07.31.230292"
)
public class GT16 extends RateMatrix {

    public static final String ratesParamName = "rates";
    public static final String freqParamName = "freq";

    public GT16(@ParameterInfo(name = ratesParamName, narrativeName = "relative rates", description = "the relative rates of the G16 process. Size 6.") Value<Double[]> rates,
                @ParameterInfo(name = freqParamName, narrativeName = "base frequencies", description = "the base frequencies of the G16 process. Size 16.") Value<Double[]> freq,
                @ParameterInfo(name = meanRateParamName, narrativeName = "substitution rate", description = "the rate of substitution.", optional = true) Value<Number> meanRate) {

        super(meanRate);

        if (rates.value().length != 6) throw new IllegalArgumentException("Rates must have 6 dimensions.");

        setParam(ratesParamName, rates);
        setParam(freqParamName, freq);
    }

    @GeneratorInfo(name = "gt16",
            verbClause = "is",
            narrativeName = "general time-reversible rate matrix on phased genotypes",
            description = "The GTR instantaneous rate matrix on phased genotypes. Takes relative rates (6) and base frequencies (16) and produces an GT16 rate matrix.")
    public Value<Double[][]> apply() {
        Value<Double[]> rates = getRates();
        Value<Double[]> freq = getParams().get(freqParamName);
        return new DoubleArray2DValue(g16(rates.value(), freq.value()), this);
    }

    private Double[][] g16(Double[] rates, Double[] freqs) {

        int numStates = 16;

        Double[][] Q = new Double[numStates][numStates];

        double[] totalRates = new double[numStates];


        // construct off-diagonals
        int rateIndex = 0;
        for (int i = 0; i < numStates; i++) {

            int fromParent1State = i / 4;
            int fromParent2State = i % 4;
            for (int j = i + 1; j < numStates; j++) {
                int toParent1State = j / 4;
                int toParent2State = j % 4;

                if (fromParent1State == toParent1State || fromParent2State == toParent2State) {

                    int first, second;

                    if (fromParent1State != toParent1State) {
                        first = Math.min(fromParent1State, toParent1State);
                        second = Math.max(fromParent1State, toParent1State);
                    } else {
                        first = Math.min(fromParent2State, toParent2State);
                        second = Math.max(fromParent2State, toParent2State);
                    }

                    rateIndex = first + second;
                    if (first == 0) rateIndex -= 1;

                    Q[i][j] = rates[rateIndex] * freqs[j];
                    Q[j][i] = rates[rateIndex] * freqs[i];

                } else {
                    Q[i][j] = 0.0;
                    Q[j][i] = 0.0;
                }
            }
        }

        // construct diagonals
        for (
                int i = 0;
                i < numStates; i++) {
            double totalRate = 0.0;
            for (int j = 0; j < numStates; j++) {
                if (j != i) {
                    totalRate += Q[i][j];
                }
            }
            Q[i][i] = -totalRate;
        }

        // normalise rate matrix to one expected substitution per unit time
        normalize(freqs, Q, totalRateDefault1());

        return Q;
    }

    public Value<Double[]> getRates() {
        return getParams().get(ratesParamName);
    }

    public Value<Double[]> getFreq() {
        return getParams().get(freqParamName);
    }
}
