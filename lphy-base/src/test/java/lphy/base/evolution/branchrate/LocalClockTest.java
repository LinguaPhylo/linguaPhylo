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

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalClockTest {
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
    void setRate() {
        TimeTreeNode node = tree.getNodes().get(4);
        TimeTreeNode[] clades = {node};
        Double[] cladeRates = {0.4};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<Object[]> cladesValue = new Value<>("clades", clades);
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
        TimeTreeNode node1 = null;
        TimeTreeNode node2 = null;

        for (int i = 0; i<tree.getNodes().size(); i++){
            if (Objects.equals(tree.getNodes().get(i).getId(), "4")){ //node2 is the leaf node 4
                node2 = tree.getNodes().get(i);
            } else if (tree.getNodes().get(i).getAllLeafNodes().size() == 2){ //node1 is the parent of (2,3)
                node1 = tree.getNodes().get(i);
            }
        }

        assertEquals(true, node1 != null);
        assertEquals(true, node2 != null);


        TimeTreeNode[] clades = {node1, node2};
        Double[] cladeRates = {0.4,0.3};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<Object[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);
        Value<Boolean> includeStemValue = new Value<>("includeStem" , Boolean.TRUE);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, includeStemValue);
        Value<TimeTree> observe = instance.apply();
        List<TimeTreeNode> allNodes = observe.value().getNodes();

        for (int i = 0; i<allNodes.size() - 1; i++){
            TimeTreeNode node = allNodes.get(i);
            if (node.getId() != null){
                if (node.getId().equals("2") || node.getId().equals("3")){ //leaf node 2 and 3 should have branch rate 0.4
                    assertEquals(0.4, node.getBranchRate());
                } else if (node.getId().equals("1")){ // not specified, should be rootRate
                    assertEquals(rootRate, node.getBranchRate());
                } else if (node.getId().equals("4")) { // node2, should be 0.3
                    assertEquals(0.3, node.getBranchRate());
                }
            } else if (node.getAllLeafNodes().size() == 2){ // the node should be the parent of 2 and 3
                assertEquals(0.4, node.getBranchRate());
            }else if (node.getAllLeafNodes().size() == 3){ // the node should be the parent of ((2,3),1)
                assertEquals(rootRate, node.getBranchRate());
            }
        }
    }


    @Test
    void applyExcludeStem() {
        TimeTreeNode clade = null;

        for (int i = 0; i<tree.getNodes().size(); i++) {
            if (tree.getNodes().get(i).getAllLeafNodes().size() == 2) { //node is the parent of (2,3)
                clade = tree.getNodes().get(i);
            }
        }

        TimeTreeNode[] clades = {clade};
        Double[] cladeRates = {0.4};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<Object[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);
        Value<Boolean> includeStemValue = new Value<>("includeStem" , Boolean.FALSE);

        LocalClock instance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, includeStemValue);
        Value<TimeTree> observe = instance.apply();
        List<TimeTreeNode> allNodes = observe.value().getNodes();

        for (int i = 0; i<allNodes.size() - 1; i++){
            TimeTreeNode node = allNodes.get(i);
            if (node.getId() != null){
                if (node.getId().equals("2") || node.getId().equals("3")){ //leaf node 2 and 3 should have branch rate 0.4
                    assertEquals(0.4, node.getBranchRate());
                } else if (node.getId().equals("1") || node.getId().equals("4")){ // not specified, should be rootRate
                    assertEquals(rootRate, node.getBranchRate());
                }
            } else if (node.getAllLeafNodes().size() == 2 || node.getAllLeafNodes().size() == 3){ // internal nodes should all be root rate
                assertEquals(rootRate, node.getBranchRate());
            }
        }
    }
}
