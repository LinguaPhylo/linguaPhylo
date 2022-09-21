package lphy.evolution.tree;

import lphy.evolution.coalescent.Coalescent;
import lphy.graphicalModel.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Walter Xie
 */
class TimeTreeTest {

    final int nTaxa = 20;
    TimeTree tree;

    @BeforeEach
    void setUp() {
        Coalescent simulator = new Coalescent(new Value<>("Θ", 10.0), new Value<>("n", nTaxa), null);
        tree = Objects.requireNonNull(simulator.sample()).value();
    }

    // Root node must be the last element of the node list.
    @Test
    void getNodes() {
        List<TimeTreeNode> nodes = tree.getNodes();

        assertEquals(2*nTaxa-1, nodes.size(), "all nodes = 2 * n - 1");

        double rootAge =  tree.getRoot().getAge();
        assertTrue(rootAge > 0);

        // root must be the last element
        assertEquals(rootAge, nodes.get(nodes.size()-1).getAge(), "root age vs. the last element age");

        // first nTaxa elements must be leave nodes
        for (int i = 0; i < nTaxa; i++) {
            assertTrue(nodes.get(i).isLeaf(), "the " + i + "th element must be a leave node");
        }

    }

    // Root node must be the last element of the node list.
    @Test
    void getInternalNodes() {
        List<TimeTreeNode> nodes = tree.getInternalNodes();

        assertEquals(nTaxa-1, nodes.size(), "internal nodes = n - 1");

        double rootAge =  tree.getRoot().getAge();
        assertTrue(rootAge > 0);

        // root must be the last element
        assertEquals(rootAge, nodes.get(nodes.size()-1).getAge(), "root age vs. the last element age");
    }

}
