package lphy.core;

import lphy.core.functions.newickParser.NewickASTVisitor;
import lphy.core.functions.newickParser.NewickLexer;
import lphy.core.functions.newickParser.NewickParser;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Fabio K. Mendes
 */

/*
 * Assortment of unit tests for PhyloMultivariateBrownian class
 */
public class PhyloMultivariateBrownianTest {

    int nTries, nSamples, nTraits;
    StandardDeviation sd = new StandardDeviation();
    double[][][] sp1States, sp2States, sp3States, sp4States;

    public TimeTree initializeTree(String trNewick) {

        // antlr convoluted stuff (oof...)
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        TimeTree tree = new TimeTree();
        tree.setRoot(root);

        return tree;
    }

    private int countHowManyWithinCI(int nTries, int nSamples, int traitIdx, double[][][] allSamplesFromOneSp, StandardDeviation sd) {

        int count = 0;
        for (int k=0; k<nTries; k++) {
            double sampleStdErr = sd.evaluate(allSamplesFromOneSp[k][traitIdx]) / Math.sqrt(nSamples);
            double bound = 1.96 * sampleStdErr;
            double sampleMean = StatUtils.mean(allSamplesFromOneSp[k][traitIdx]);

            if ((sampleMean > (sampleMean - bound)) && (sampleMean < (sampleMean + bound))) count++;
        }

        // System.out.println("Within 95% CI=" + count);
        return count;
    }

    @Before
    public void setUp() {
        // tree
        TimeTree tree = initializeTree("(sp1:100.0,(sp2:5.0,(sp3:0.01,sp4:0.01)2:4.9)1:95.0)0:0.0;");
        Value<TimeTree> trValue = new Value<TimeTree>("tree", tree);

        // rate matrix
        nTraits = 3;
        // Double[][] rateMat = new Double[][] { {1.0, 0.5, 0.25}, {0.5, 1.0, 0.25}, {0.25, 0.25, 1.0} };
        Double[][] rateMat = new Double[][] { {0.1, 0.0, 0.0}, {0.0, 0.1, 0.0}, {0.0, 0.0, 0.1} }; // traits assumed independent (identity matrix)
        Value<Double[][]> rateMatValue = new Value<Double[][]>("rate", rateMat);

        // y0 root values
        Double[] y0 = new Double[] { 0.0, 1.0, 2.0 };
        Value<Double[]> y0Value = new Value<Double[]>("y0", y0);

        // distribution
        PhyloMultivariateBrownian phyloMB = new PhyloMultivariateBrownian(trValue, rateMatValue, y0Value);

        // sampling
        nTries = 100; // 95 out of 100 should be inside 95 CI
        nSamples = 100; // so 100 * 1000
        ContinuousCharacterData[] samples = new ContinuousCharacterData[nSamples];
        sp1States = new double[nTries][nTraits][nSamples];
        sp2States = new double[nTries][nTraits][nSamples];
        sp3States = new double[nTries][nTraits][nSamples];
        sp4States = new double[nTries][nTraits][nSamples];

        for (int k=0; k<nTries; k++) {
            for (int j=0; j<nTraits; j++) {
                for (int i = 0; i<nSamples; i++) {
                    samples[i] = phyloMB.sample().value();
                    sp1States[k][j][i] = samples[i].getState("sp1", j).doubleValue();
                    sp2States[k][j][i] = samples[i].getState("sp2", j).doubleValue();
                    sp3States[k][j][i] = samples[i].getState("sp3", j).doubleValue();
                    sp4States[k][j][i] = samples[i].getState("sp4", j).doubleValue();
                }
            }
        }
    }
    /*
     * This test carries out a batch of size 'nTries' simulations,
     * in turn of size 'nSamples'. So we have a total of (nTries * nSamples)
     * forward simulations along the specified phylogenetic tree.
     *
     * Out of nTries, we should expect >95 simulations to have
     * a species trait value contained within the 95%-CI defined
     * as [mean(samples) - 1.96 * sd(samples)/sqrt(nSamples)),
     *     (mean(samples) + 1.96 * sd(samples)/sqrt(nSamples)]
     * by the central limit theorem.
     */
    @Test
    public void mvnMultipleTraitValuesStdErrTest() {

        // trait 1
        int nWithinSp1Tr1 = countHowManyWithinCI(nTries, nSamples, 0, sp1States, sd); // sp1
        int nWithinSp4Tr1 = countHowManyWithinCI(nTries, nSamples, 0, sp4States, sd); // sp4

        // trait 2
        int nWithinSp1Tr2 = countHowManyWithinCI(nTries, nSamples, 1, sp1States, sd); // sp1
        int nWithinSp4Tr2 = countHowManyWithinCI(nTries, nSamples, 1, sp4States, sd); // sp4

        // trait 3
        int nWithinSp1Tr3 = countHowManyWithinCI(nTries, nSamples, 2, sp1States, sd); // sp1
        int nWithinSp4Tr3 = countHowManyWithinCI(nTries, nSamples, 2, sp4States, sd); // sp4

        Assert.assertTrue(nWithinSp1Tr1 >= 95);
        Assert.assertTrue(nWithinSp4Tr1 >= 95);
        Assert.assertTrue(nWithinSp1Tr2 >= 95);
        Assert.assertTrue(nWithinSp4Tr2 >= 95);
        Assert.assertTrue(nWithinSp1Tr3 >= 95);
        Assert.assertTrue(nWithinSp4Tr3 >= 95);
    }

    /*
     * This test checks that mean (across nTries) of trait value means (across nSamples)
     * follows expectations from phylogenetic tree:
     *
     * i.e., trait values in species 3 and 4 should be on average more similar
     * than those in species 3 and 2, which in turn should be more similar
     * than those of species 3 and 1; also, trait values in species 3 and 4
     * should be more similar than those in species 3 and 1.
     *
     */
    @Test
    public void mvnMultipleTraitValuesPhyloSignalTest() {

        double[][][] diffsSp3Sp4 = new double[nTraits][nTries][nSamples];
        double[][][] diffsSp3Sp2 = new double[nTraits][nTries][nSamples];
        double[][][] diffsSp3Sp1 = new double[nTraits][nTries][nSamples];

        double[][] meanDiffsSp3Sp4 = new double[nTraits][nTries];
        double[][] meanDiffsSp3Sp2 = new double[nTraits][nTries];
        double[][] meanDiffsSp3Sp1 = new double[nTraits][nTries];

        for (int j=0; j<nTraits; j++) {
            for (int k=0; k<nTries; k++) {
                for (int i=0; i<nSamples; i++) {
                    diffsSp3Sp4[j][k][i] = sp3States[k][j][i] - sp4States[k][j][i];
                    diffsSp3Sp2[j][k][i] = sp3States[k][j][i] - sp2States[k][j][i];
                    diffsSp3Sp1[j][k][i] = sp3States[k][j][i] - sp1States[k][j][i];
                }

                meanDiffsSp3Sp4[j][k] = Math.abs(StatUtils.mean(diffsSp3Sp4[j][k])); // abs mean difference among i samples within k-th try, for j-th trait
                meanDiffsSp3Sp2[j][k] = Math.abs(StatUtils.mean(diffsSp3Sp2[j][k]));
                meanDiffsSp3Sp1[j][k] = Math.abs(StatUtils.mean(diffsSp3Sp1[j][k]));
            }
        }

        // trait 1
        Assert.assertTrue(
                Math.abs(StatUtils.mean(meanDiffsSp3Sp4[0])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[0])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp4[0])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp2[0])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp2[0])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[0]))
        );

        // trait 2
        Assert.assertTrue(
                Math.abs(StatUtils.mean(meanDiffsSp3Sp4[1])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[1])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp4[1])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp2[1])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp2[1])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[1]))
        );

        // trait 3
        Assert.assertTrue(
                Math.abs(StatUtils.mean(meanDiffsSp3Sp4[2])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[2])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp4[2])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp2[2])) &&
                        Math.abs(StatUtils.mean(meanDiffsSp3Sp2[2])) < Math.abs(StatUtils.mean(meanDiffsSp3Sp1[2]))
        );
    }
}