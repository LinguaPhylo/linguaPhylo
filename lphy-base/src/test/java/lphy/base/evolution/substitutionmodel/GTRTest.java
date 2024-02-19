package lphy.base.evolution.substitutionmodel;

import lphy.base.evolution.coalescent.Coalescent;
import lphy.base.evolution.likelihood.PhyloCTMC;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GTRTest {

    public static Stream<Arguments> getRatesAndFreqs() {
        return Stream.of(
                Arguments.of(new Double[] {0.2, 10.0, 0.3, 0.4, 5.0, 0.5},
                        new Double[]{0.20, 0.30, 0.25, 0.25},
                        new double[][]{
                                {0.8780963047046206, 0.0033252855682803723, 0.11461112844510626, 0.003967281281992822},
                                {0.002216857045520258, 0.9327483979953872, 0.005055665025823634, 0.05997907993326873},
                                {0.09168890275608481, 0.006066798030988321, 0.8959983003009074, 0.0062459989120190644},
                                {0.0031738250255942332, 0.07197489591992245, 0.006245998912019033, 0.9186052801424642}
                        }),
                Arguments.of(new Double[] {0.5, 1.0, 0.5, 0.5, 1.0, 0.5},
                        new Double[]{0.25, 0.25, 0.25, 0.25},
                        new double[][]{
                                {0.906563342722, 0.023790645491, 0.045855366296, 0.023790645491},
                                {0.023790645491, 0.906563342722, 0.023790645491, 0.045855366296},
                                {0.045855366296, 0.023790645491, 0.906563342722, 0.023790645491},
                                {0.023790645491, 0.045855366296, 0.023790645491, 0.906563342722}
                        }),
                Arguments.of(new Double[] {0.5, 1.0, 0.5, 0.5, 1.0, 0.5},
                        new Double[]{0.50, 0.20, 0.2, 0.1},
                        new double[][]{
                                {0.928287993055, 0.021032136637, 0.040163801989, 0.010516068319},
                                {0.052580341593, 0.906092679369, 0.021032136637, 0.020294842401},
                                {0.100409504972, 0.021032136637, 0.868042290072, 0.010516068319},
                                {0.052580341593, 0.040589684802, 0.021032136637, 0.885797836968}
                        }) );
    }


    RandomVariable<TimeTree> tree;
    @BeforeEach
    void setUp() {
        // this is only for init PhyloCTMC, tree would not be required.
        Coalescent coalescent = new Coalescent(new Value<>("theta", 10.0),
                new Value<>("n", 10), null);
        tree = coalescent.sample();
    }

    @ParameterizedTest
    @MethodSource("getRatesAndFreqs")
    void testGTR(Double[] rates, Double[] freqs, final double[][] expectedP) {

        GTR gtr = new GTR(new Value<>("rates", rates), new Value<>("freqs", freqs),
                new Value<>("meanRate", 1.0));

        final Value Q = gtr.apply();

        PhyloCTMC phyloCTMC = new PhyloCTMC(tree, null, null, Q,
                null, null, new Value<>("L", 100), null, null);
        // not simulate sequences, so ignore sample(), then tree stats and L should not affect the result
        phyloCTMC.setup();
        double[][] p = new double[4][4];
        double branchLength = 0.1;
        phyloCTMC.getTransitionProbabilities(branchLength, p);
        System.out.println("Actual trans probs = " + Arrays.deepToString(p));
        System.out.println("Expected trans probs = " + Arrays.deepToString(expectedP));

        for (int i = 0; i < p.length; i++) {
            assertArrayEquals(p[i], expectedP[i], 1E-12);
        }

    }


}