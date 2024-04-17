package lphy.base.evolution.branchrate;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.parser.newick.NewickASTVisitor;
import lphy.base.parser.newick.NewickLexer;
import lphy.base.parser.newick.NewickParser;
import lphy.core.model.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalClockTest {
    final int nTaxa = 16;
    TimeTree tree;

    @BeforeEach
    void setUp() {
        String trNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        this.tree = new TimeTree();
        this.tree.setRoot(root);
    }

    @Test
    void getCladeRate() {
        TimeTreeNode[] clades = {tree.getNodes().get(4), tree.getNodes().get(5)};
        Double[] cladeRates = {0.4, 0.6};

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<TimeTreeNode[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRate = new Value<>("rootRate" , 0.2);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRate, null);

        double observe = instance.getCladeRate(tree.getNodes().get(4), cladesValue, cladeRatesValue);
        assertEquals(0.4, observe);
    }

    @Test
    void setRate() {
        TimeTreeNode node = tree.getNodes().get(4);
        TimeTreeNode[] clades = {node};
        Double[] cladeRates = {0.4};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<TimeTreeNode[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, null);
        instance.setRate(node, 0.4, true);

        assertEquals(0.4, node.getBranchRate());
        assertEquals(0.4, node.getLeft().getBranchRate());
        assertEquals(0.4, node.getRight().getBranchRate());
    }

    @Test
    void apply() {
        TimeTreeNode node = tree.getNodes().get(4);
        TimeTreeNode[] clades = {node};
        Double[] cladeRates = {0.4};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<TimeTreeNode[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);
        Value<Boolean> includeStemValue = new Value<>("includeStem" , Boolean.TRUE);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, includeStemValue);
        Value<Double[]> observe = instance.apply();

        Double[] expect = {0.4, 0.4, rootRate, rootRate, 0.4, rootRate};
        Value<Double[]> expectValue = new Value<>(null, expect);
        for (int i = 0; i<expect.length; i++){
            assertEquals(expectValue.value()[i], observe.value()[i]);
        }
    }


    @Test
    void applyExcludeStem() {
        TimeTreeNode node = tree.getNodes().get(4);
        TimeTreeNode[] clades = {node};
        Double[] cladeRates = {0.4};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<TimeTreeNode[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);
        Value<Boolean> includeStemValue = new Value<>("includeStem" , Boolean.FALSE);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, includeStemValue);
        Value<Double[]> observe = instance.apply();

        Double[] expect = {0.4, 0.4, rootRate, rootRate, rootRate, rootRate};
        Value<Double[]> expectValue = new Value<>(null, expect);
        for (int i = 0; i<expect.length; i++){
            assertEquals(expectValue.value()[i], observe.value()[i]);
        }
    }
}
