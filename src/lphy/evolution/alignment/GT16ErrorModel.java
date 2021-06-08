package lphy.evolution.alignment;

import lphy.core.distributions.Utils;
import lphy.evolution.datatype.PhasedGenotype;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adru001 on 2/02/20.
 */
public class GT16ErrorModel implements GenerativeDistribution<Alignment> {

    Value<Double> epsilon;
    Value<Double> delta;
    Value<Alignment> alignment;

    public final String epsilonParamName = "epsilon";
    public final String deltaParamName = "delta";
    public final String alignmentParamName = "alignment";

    RandomGenerator random;

    public GT16ErrorModel(@ParameterInfo(name = epsilonParamName, description = "the sequencing and amplification error probability.") Value<Double> epsilon,
                          @ParameterInfo(name = deltaParamName, description = "the allelic drop out probability.") Value<Double> delta,
                          @ParameterInfo(name = alignmentParamName, description = "the genotype alignment.") Value<Alignment> alignment) {

        this.epsilon = epsilon;
        this.delta = delta;

        if (alignment.value().getSequenceType() != PhasedGenotype.INSTANCE) {
            throw new RuntimeException("GT16ErrorModel can only be applied alignments of type " + PhasedGenotype.NAME);
        }

        this.alignment = alignment;
        this.random = Utils.getRandom();
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(epsilonParamName, epsilon);
        map.put(deltaParamName, delta);
        map.put(alignmentParamName, alignment);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(epsilonParamName)) {
            epsilon = value;
        } else if (paramName.equals(deltaParamName)) delta = value;
        else if (paramName.equals(alignmentParamName)) alignment = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    @GeneratorInfo(name = "GT16ErrorModel", description = "An error model distribution on an phased genotype alignment.")
    public RandomVariable<Alignment> sample() {

        Alignment original = alignment.value();
        SimpleAlignment newAlignment = new ErrorAlignment(original.nchar(), original);

        double e = epsilon.value();
        double d = delta.value();

        double[][] errorMatrix = errorMatrix(e, d);

        for (int i = 0; i < newAlignment.ntaxa(); i++) {
            for (int j = 0; j < newAlignment.nchar(); j++) {
                newAlignment.setState(i, j, error(original.getState(i, j), errorMatrix));
            }
        }

        return new RandomVariable<>("D", newAlignment, this);
    }

    public double getEpsilon() {
        return Objects.requireNonNull(epsilon).value();
    }

    public double getDelta() {
        return Objects.requireNonNull(delta).value();
    }

    public Alignment getOriginalAlignment() {
        return Objects.requireNonNull(alignment).value();
    }


    private int error(int state, double[][] errorMatrix) {

        double U = random.nextDouble();

        double[] row = errorMatrix[state];

        double sum = 0;
        for (int i = 0; i < row.length; i++) {
            sum += row[i];
            if (U <= sum) return i;
        }
        throw new RuntimeException("Error in error model! The sum of row should be equal to 1.0");
    }


    private double[][] errorMatrix(double epsilon, double delta) {

        double e = epsilon;
        double f = epsilon / 6.0;
        double d = delta;
        double b = delta / 2.0;

        double[][] errorMatrix = {
           // AA     AC     AG     AT     CA   CC     CG     CT     GA     GC   GG     GT     TA     TC     TG   TT
            {1-e,     f,     f,     f,     f,   0,     0,     0,     f,     0,   0,     0,     f,     0,     0,   0},// AA
            {f+b, 1-e-d,     f,   f+b,     0, f+b,     0,     0,     0,     f,   0,     0,     0,     f,     0,   0},// AC
            {f+b,     f, 1-e-d,     f,     0,   0,     f,     0,     0,     0, f+b,     0,     0,     0,     f,   0},// AG
            {f+b,     f,     f, 1-e-d,     0,   0,     0,     f,     0,     0,   0,     f,     0,     0,     0, f+b},// AT
            {f+b,     0,     0,     0, 1-e-d, f+b,     f,     f,     f,     0,   0,     0,     f,     0,     0,   0},// CA
            {  0,     f,     0,     0,     f, 1-e,     f,     f,     0,     f,   0,     0,     0,     f,     0,   0},// CC
            {  0,     0,     f,     0,     f, f+b, 1-e-d,     f,     0,     0, f+b,     0,     0,     0,     f,   0},// CG
            {  0,     0,     0,     f,     f, f+b,     f, 1-e-d,     0,     0,   0,     f,     0,     0,     0, f+b},// CT
            {f+b,     0,     0,     0,     f,   0,     0,     0, 1-e-d,     f, f+b,     f,     f,     0,     0,   0},// GA
            {  0,     f,     0,     0,     0, f+b,     0,     0,     f, 1-e-d, f+b,     f,     0,     f,     0,   0},// GC
            {  0,     0,     f,     0,     0,   0,     f,     0,     f,     f, 1-e,     f,     0,     0,     f,   0},// GG
            {  0,     0,     0,     f,     0,   0,     0,     f,     f,     f, f+b, 1-e-d,     0,     0,     0, f+b},// GT
            {f+b,     0,     0,     0,     f,   0,     0,     0,     f,     0,   0,     0, 1-e-d,     f,     f, f+b},// TA
            {  0,     f,     0,     0,     0, f+b,     0,     0,     0,     f,   0,     0,     f, 1-e-d,     f, f+b},// TC
            {  0,     0,     f,     0,     0,   0,     f,     0,     0,     0, f+b,     0,     f,     f, 1-e-d, f+b},// TG
            {  0,     0,     0,     f,     0,   0,     0,     f,     0,     0,   0,     f,     f,     f,     f, 1-e} // TT
        };
        return errorMatrix;
    }
}
