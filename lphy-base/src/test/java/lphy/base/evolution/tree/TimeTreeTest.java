package lphy.base.evolution.tree;

import lphy.base.evolution.coalescent.Coalescent;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


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

    // the node should be the oldest with the age
    @Test
    void getOldestNode() {
        // for internal nodes, cherries throw the exception, others then generate the oldest node
        List<TimeTreeNode> nodes = tree.getInternalNodes();
        Double[] ages = new Double[nodes.size()];
        for (int i = 0; i<nodes.size();i++){
            TimeTreeNode node = nodes.get(i);
            double age = node.getAge();
            ages[i] = age;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(ages.length);
        double randomAge = ages[randomIndex];
        TimeTreeNode randomNode = nodes.get(randomIndex);

        TimeTreeNode observe = tree.getOldestInternalNode(randomAge);

        double expected = 0;
        for (int i = 0; i<ages.length; i++){
            if (ages[i] <= randomAge && ages[i] > expected){
                expected = ages[i];
            } else if (randomNode.getLeft().getChildCount() == 0 && randomNode.getRight().getChildCount() == 0) {
                String expectedErrorMessage = "The input age should be older than the lowest ancestor, maxAge = " + randomAge;
                try {
                    tree.getOldestInternalNode(randomAge);
                    throw new IllegalArgumentException(expectedErrorMessage);
                } catch (Exception e) {
                    assertEquals(expectedErrorMessage, e.getMessage());
                }
            }
        }
        assertEquals(expected, observe.getAge());


        // test for Exception
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> tree.getOldestInternalNode(0.0),
                "Expected IllegalArgumentException not thrown!"
        );
    }

}
