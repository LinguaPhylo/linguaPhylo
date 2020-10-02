package test.core;

import lphy.core.PhyloMultivariateBrownian;
import lphy.core.functions.newickParser.NewickASTVisitor;
import lphy.core.functions.newickParser.NewickLexer;
import lphy.core.functions.newickParser.NewickParser;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

/**
 * @author: Fabio K. Mendes
 */

/*
 * Assortment of unit tests for PhyloMultivariateBrownian class
 */
public class PhyloMultivariateBrownianTest {

    TimeTree tree;

    public void initializeTree(String trNewick) {

        // antlr convoluted stuff (oof...)
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        tree = new TimeTree();
        tree.setRoot(root);
    }

    /*
     *
     */
    @Test
    public void mvnMultipleTraitValuesStdErrTest() {

        // tree
        initializeTree("(sp1:5.0,(sp2:3.0,(sp3:1.0,sp4:1.0)2:2.0)1:2.0)0:0.0;");
        Value<TimeTree> trValue = new Value<TimeTree>("tree", tree);

        // rate matrix
        int nTraits = 3;
        Double[][] rateMat = new Double[nTraits][nTraits];
        Value<Double[][]> rateMatValue = new Value<Double[][]>("rate", rateMat);

        // y0 root values
        Double[] y0 = new Double[] { 0.0, 1.0, 2.0 };
        Value<Double[]> y0Value = new Value<Double[]>("y0", y0);

        // distribution
        PhyloMultivariateBrownian phyloMB = new PhyloMultivariateBrownian(trValue, rateMatValue, y0Value);

        // sampling
        RandomVariable<ContinuousCharacterData> sample = phyloMB.sample();
    }

//    /*
//     *
//     */
//    @Test
//    public void mvnMultipleTraitValuesPhyloSignalTest() {
//
//    }
}