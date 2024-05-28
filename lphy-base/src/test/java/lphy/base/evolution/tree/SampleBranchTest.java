package lphy.base.evolution.tree;

import lphy.base.function.tree.Newick;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleBranchTest {
    String newickTree;

    @BeforeEach
    void setUp() {
        newickTree = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
    }


    @Test
    void sample() {
        TimeTree tree = Newick.parseNewick(newickTree);
        Double age = 1.0;
        Value<Double> ageValue = new Value<>("age", age);
        Value<TimeTree> treeValue = new Value<>("tree", tree);
        SampleBranch instance = new SampleBranch(treeValue, ageValue);
        List<TimeTreeNode> nodes = tree.getNodes();

        int right = 0;
        int left = 0;

        for (int i = 0; i<10000; i++) {
            Value<TimeTreeNode> node = instance.sample();
            if (node.value() == nodes.get(4)){
                left ++;
            } else if (node.value() == nodes.get(5)){
                right ++;
            }
            if (!node.value().isRoot()) {
                assertEquals(node.value().age <= age, node.value().getParent().age >= age);
            }
        }

        // allow 5% error margin
        double errorMargin = 0.05 * 10000;
        assertEquals(left, right, errorMargin);
    }
}
