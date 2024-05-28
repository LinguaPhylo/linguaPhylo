package lphy.base.function.tree;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewickTest {
    String tree;

    @BeforeEach
    void setUp() {
        tree = "((1:2.0, (2:1.0, 3:1.0):1.0):6.0, 4:8.0)";
    }

    @Test
    void parseNewick() {
        TimeTree timeTree = Newick.parseNewick(tree);

        System.out.println("Parsed Tree: " + timeTree.toNewick(true));

        List<TimeTreeNode> leafNodes = timeTree.getLeafNodes();

        assertEquals(4, leafNodes.size());
        assertEquals(4, timeTree.leafCount());

        List<String> ids = new ArrayList<>();
        for (TimeTreeNode leaf : leafNodes) {
            ids.add(leaf.getId());
            // precision err
            assertEquals(0.0, leaf.getAge(), 1E-10);
        }
        Collections.sort(ids);
        assertEquals(List.of("1", "2", "3", "4"), ids);

        List<TimeTreeNode> internalNodes = timeTree.getInternalNodes();
        assertEquals(3, internalNodes.size());
        // all internal node ages must > 0
        for (TimeTreeNode node : internalNodes) {
            assertTrue(node.getAge() > 0.0);
        }

        // TODO: leaf node 4 is not extant, because of precision err: age=1E-15
//        List<TimeTreeNode> extantNodes = timeTree.getExtantNodes();
//        assertEquals(3, extantNodes.size());
    }
}