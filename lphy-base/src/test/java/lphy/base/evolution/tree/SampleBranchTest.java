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

public class SampleBranchTest {
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
    void sample() {
        Double age = 1.0;
        Value<Double> ageValue = new Value<>("age", age);
        Value<TimeTree> treeValue = new Value<>("tree", tree);
        SampleBranch instance = new SampleBranch(treeValue, ageValue);
        List<TimeTreeBranch> branches = tree.getBranches();

        int right = 0;
        int left = 0;

        for (int i = 0; i<10000; i++) {
            Value<TimeTreeBranch> branch = instance.sample();
            if (branch.value() == branches.get(4)){
                left ++;
            } else if (branch.value() == branches.get(5)){
                right ++;
            }
        }

        // allow 5% error margin
        double errorMargin = 0.05 * 10000;
        assertEquals(left, right, errorMargin);
    }
}
