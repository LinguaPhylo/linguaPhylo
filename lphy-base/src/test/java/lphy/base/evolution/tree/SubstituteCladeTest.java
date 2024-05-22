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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstituteCladeTest {
    TimeTree baseTree;
    TimeTree cladeTree;

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
    }

    @Test
    void applyTest() {
        TimeTreeNode node = baseTree.getNodeByIndex(3);
        Double time = 4.0;
        String nodeLabel = "tumourNode";

        Value<TimeTree> baseTreeValue = new Value<>("baseTree", baseTree);
        Value<TimeTree> cladeTreeValue = new Value<>("cladeTree", cladeTree);
        Value<TimeTreeNode> nodeValue = new Value<>("node", node);
        Value<Double> timeValue = new Value<>("time", time);
        Value<String> nodeLabelValue = new Value<>("nodeLabel", nodeLabel);

        SubstituteClade instance = new SubstituteClade(baseTreeValue, cladeTreeValue, nodeValue, timeValue, nodeLabelValue);
        Value<TimeTree> observe = instance.apply();

        // check num of leaf nodes
        assertEquals(7, observe.value().getRoot().getAllLeafNodes().size());

        List<TimeTreeNode> nodes = observe.value().getNodes();
        // check index of nodes
        for (int i = 0; i< nodes.size(); i++){
            int index = observe.value().getNodes().get(i).getIndex();
            assertEquals(i , index);
        }

        List<TimeTreeNode> leafNodes = observe.value().getRoot().getAllLeafNodes();
        // check each name of leaf nodes
        assertEquals("2", leafNodes.get(0).getId());
        assertEquals("3", leafNodes.get(1).getId());
        assertEquals("1", leafNodes.get(2).getId());
        assertEquals("clade_2", leafNodes.get(3).getId());
        assertEquals("clade_3", leafNodes.get(4).getId());
        assertEquals("clade_1", leafNodes.get(5).getId());
        assertEquals("clade_4", leafNodes.get(6).getId());

        // check the label of tumour root
        // find the tumour root
        for (TimeTreeNode sample : nodes){
            if (Objects.equals(sample.getId(), nodeLabel)){
                assertEquals(4.0, sample.getAge());
                assertEquals(sample, observe.value().getNodeById(nodeLabel));
            }
        }
    }
}
