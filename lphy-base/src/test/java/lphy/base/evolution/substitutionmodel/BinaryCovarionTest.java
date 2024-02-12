package lphy.base.evolution.substitutionmodel;

import lphy.base.evolution.coalescent.Coalescent;
import lphy.base.evolution.likelihood.PhyloCTMC;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * mode = BEAST -- using classic BEAST implementation, reversible iff hfrequencies = (0.5, 0.5)
 * FLAGS: reversible = false, TSParameterisation = false
 *
 * [ -(a*p1)-s ,   a*p1    ,    s   ,   0   ]
 * [   a*p0    , -(a*p0)-s ,    0   ,   s   ]
 * [    s      ,     0     ,  -p1-s ,  p1   ]
 * [    0      ,     s     ,    p0  , -p0-s ]
 *
 * equilibrium frequencies
 * [ p0 * f0, p1, * f0, p0 * f1, p1, * f1 ]
 */
class BinaryCovarionTest {

    RandomVariable<TimeTree> tree;

    Value<Number> alpha = new Value<>("alpha", 0.01);
    Value<Number> s = new Value<>("switchRate", 0.1);

    @BeforeEach
    void setUp() {
        // tree
        Coalescent coalescent = new Coalescent(new Value<>("theta", 10.0),
                new Value<>("n", 10), null);
        tree = coalescent.sample();
        // only tip labels are used in the follow code
    }

    @Test
    void testQAndFreqs() {
        Value<Number[]> vfreq = new Value<>("fVisibleStates", new Number[]{0.2, 0.8});
        Value<Number[]> hfreq = new Value<>("fHiddenStates", new Number[]{0.5, 0.5});

        BinaryCovarion binaryCovarion = new BinaryCovarion(alpha, s, vfreq, hfreq,
                new Value<>("meanRate", 1.0));
        Value<Double[][]> Q = binaryCovarion.apply();
        System.out.println("Q matrix: " + Arrays.deepToString(Q.value()));

        // 1st row of Q matrix
        assertArrayEquals(new double[]{-0.6683, 0.0495, 0.6188, 0.0},
                ArrayUtils.toPrimitive(Q.value()[0]), 1e-3);

        // test freqs
        Double[] freqs = binaryCovarion.getFrequencies(ValueUtils.doubleArrayValue(vfreq),
                ValueUtils.doubleArrayValue(hfreq));
        assertArrayEquals(new double[]{0.1, 0.4, 0.1, 0.4}, ArrayUtils.toPrimitive(freqs));
    }


    @Test
    void testTransProbWithEqualHFreqs() {
        double d = 0.05 + RandomUtils.getRandom().nextDouble() * 0.9;
        Value<Number[]> vfreq = new Value<>("fVisibleStates", new Number[]{d, 1.0 - d});
        Value<Number[]> hfreq = new Value<>("fHiddenStates", new Number[]{0.5, 0.5});

        BinaryCovarion binaryCovarion = new BinaryCovarion(alpha, s, vfreq, hfreq,
                new Value<>("meanRate", 1.0));
        Value<Double[][]> Q = binaryCovarion.apply();
        System.out.println("Q matrix: " + Arrays.deepToString(Q.value()));

        PhyloCTMC phyloCTMC = new PhyloCTMC(tree, null, null, Q,
                null, null, new Value<>("L", 100), null, null);
        // not simulate sequences, so ignore sample(), then tree stats and L should not affect the result
        phyloCTMC.setup();

        double[][] p = new double[4][4];
        // branchLength = 1000
        phyloCTMC.getTransitionProbabilities(100, p);
        System.out.println("Actual: " + Arrays.deepToString(p));

        // freqs
        Double[] freqs = binaryCovarion.getFrequencies(ValueUtils.doubleArrayValue(vfreq),
                ValueUtils.doubleArrayValue(hfreq));
        System.out.println("Expected: " + Arrays.toString(freqs));

        // 1st row of p matrix
        for (int j = 0; j < 4; j++) {
            assertEquals(freqs[j], p[0][j], 1e-3);
        }

    }

//TODO not working
//    @Test
//    void testTransProbWithUnEqualHFreqs() {
//        double d = 0.05 + RandomUtils.getRandom().nextDouble() * 0.9;
//        Value<Number[]> vfreq = new Value<>("fVisibleStates", new Number[]{d, 1.0 - d});
//        d = 0.05 + RandomUtils.getRandom().nextDouble() * 0.9;
//        Value<Number[]> hfreq = new Value<>("fHiddenStates", new Number[]{d, 1.0 - d});
//
//        BinaryCovarion binaryCovarion = new BinaryCovarion(alpha, s, vfreq, hfreq,
//                new Value<>("meanRate", 1.0));
//        Value<Double[][]> Q = binaryCovarion.apply();
//        System.out.println("Q matrix: " + Arrays.deepToString(Q.value()));
//
//        PhyloCTMC phyloCTMC = new PhyloCTMC(tree, null, null, Q,
//                null, null, new Value<>("L", 100), null, null);
//        // not simulate sequences, so ignore sample(), then tree stats and L should not affect the result
//        phyloCTMC.setup();
//
//        double[][] p = new double[4][4];
//        // branchLength = 1000
//        phyloCTMC.getTransitionProbabilities(100, p);
//        System.out.println("Actual: " + Arrays.deepToString(p));
//
//        // freqs
//        Double[] freqs = binaryCovarion.getFrequencies(ValueUtils.doubleArrayValue(vfreq),
//                ValueUtils.doubleArrayValue(hfreq));
//        System.out.println("Expected: " + Arrays.toString(freqs));
//
//        // 1st row of p matrix
//        for (int j = 0; j < 4; j++) {
//            assertEquals(freqs[j], p[0][j], 1e-3);
//        }
//
//    }

}