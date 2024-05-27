package lphy.base.evolution.tree;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstituteCladeTest {
    TimeTree baseTree;
    TimeTree cladeTree;
//    final int nTaxa = 4;

//    @BeforeEach
//    void setUp() {
//        Coalescent simulator = new Coalescent(new Value<>("Θ", 10.0), new Value<>("n", nTaxa), null);
//        baseTree = Objects.requireNonNull(simulator.sample()).value();
//        cladeTree = Objects.requireNonNull(simulator.sample()).value();
//    }
    @BeforeEach
    void setUp() {
        String cladeTreeNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
        CharStream charStream = CharStreams.fromString(cladeTreeNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        this.cladeTree = new TimeTree();
        this.cladeTree.setRoot(root);

        String baseTreeNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):6.0, 4:8.0)";
        CharStream base_charStream = CharStreams.fromString(baseTreeNewick);
        NewickLexer base_lexer = new NewickLexer(base_charStream);
        CommonTokenStream base_tokens = new CommonTokenStream(base_lexer);
        NewickParser base_parser = new NewickParser(base_tokens);
        ParseTree base_parseTree = base_parser.tree();
        NewickASTVisitor base_visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode base_root = base_visitor.visit(base_parseTree);
        this.baseTree = new TimeTree();
        this.baseTree.setRoot(base_root);

        System.out.println("Original tree: " + baseTreeNewick);
        System.out.println("Parsed Tree: " + this.baseTree.toNewick(true));
    }

    @Test
    void applyTest() {
        TimeTreeNode node = baseTree.getNodeByIndex(3);
        String nodeLabel = "tumourNode";

        Value<TimeTree> baseTreeValue = new Value<>("baseTree", baseTree);
        Value<TimeTree> cladeTreeValue = new Value<>("cladeTree", cladeTree);
        Value<TimeTreeNode> nodeValue = new Value<>("node", node);
        Value<String> nodeLabelValue = new Value<>("nodeLabel", nodeLabel);

        SubstituteClade instance = new SubstituteClade(baseTreeValue, cladeTreeValue, nodeValue, null, nodeLabelValue);
        Value<TimeTree> observe = instance.apply();

        List<TimeTreeNode> leafNodes = observe.value().getRoot().getAllLeafNodes();

        // check num of nodes
        assertEquals(7, leafNodes.size());
        assertEquals(7 + 6, observe.value().getNodeCount());
        assertEquals(observe.value().getNodeCount(), observe.value().getNodes().size());

        // check each name of leaf nodes
        assertEquals("2", leafNodes.get(0).getId());
        assertEquals("3", leafNodes.get(1).getId());
        assertEquals("1", leafNodes.get(2).getId());
        assertEquals("clade_2", leafNodes.get(3).getId());
        assertEquals("clade_3", leafNodes.get(4).getId());
        assertEquals("clade_1", leafNodes.get(5).getId());
        assertEquals("clade_4", leafNodes.get(6).getId());

        for (TimeTreeNode anyNode: observe.value().getNodes()){
            String metaData = (String) anyNode.getMetaData("label");
            if (nodeLabel.equals(metaData)){
                assertEquals(4.0, anyNode.age);
                assertEquals(4.0, observe.value().getLabeledNode(nodeLabel).age);
            }
        }
    }

    @Test
    void timeTest() {
        TimeTreeNode node = baseTree.getNodeByIndex(3);
        String nodeLabel = "tumourNode";
        Double time = 5.0;

        Value<TimeTree> baseTreeValue = new Value<>("baseTree", baseTree);
        Value<TimeTree> cladeTreeValue = new Value<>("cladeTree", cladeTree);
        Value<TimeTreeNode> nodeValue = new Value<>("node", node);
        Value<String> nodeLabelValue = new Value<>("nodeLabel", nodeLabel);
        Value<Double> timeValue = new Value<>("time", time);

        SubstituteClade instance = new SubstituteClade(baseTreeValue, cladeTreeValue, nodeValue, timeValue, nodeLabelValue);
        Value<TimeTree> observe = instance.apply();

        List<TimeTreeNode> leafNodes = observe.value().getRoot().getAllLeafNodes();

        // check num of nodes
        assertEquals(7, leafNodes.size());
        assertEquals(7 + 6, observe.value().getNodeCount());
        assertEquals(observe.value().getNodeCount(), observe.value().getNodes().size());
        for (TimeTreeNode anyNode: observe.value().getNodes()){
            String metaData = (String) anyNode.getMetaData("label");
            if (nodeLabel.equals(metaData)){
                assertEquals(4.0, anyNode.age);
                assertEquals(4.0, observe.value().getLabeledNode(nodeLabel).age);
            }
        }
    }
}
