package lphy.base.evolution.tree;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.coalescent.Coalescent;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
        List<TimeTreeNode> nodes = tree.getNodes();
        Double[] ages = new Double[nodes.size()];
        for (int i = 0; i<nodes.size();i++){
            TimeTreeNode node = nodes.get(i);
            double age = node.getAge();
            ages[i] = age;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(ages.length);
        double randomAge = ages[randomIndex];

        TimeTreeNode observe = tree.getOldestNode(randomAge);

        double expected = 0;
        for (int i = 0; i<ages.length; i++){
            if (ages[i] <= randomAge && ages[i] > expected){
                expected = ages[i];
            }
        }
        assertEquals(expected, observe.getAge());
    }

    // the difference in name arrays be complete
    @Test
    void getDifferentTaxaNames() {
        String[] allNames = tree.getTaxaNames();
        String[] sampledNames = Arrays.copyOf(allNames, allNames.length - 1);

        Taxa allTaxa = Taxa.createTaxa(allNames);
        Taxa sampledTaxa = Taxa.createTaxa(sampledNames);

        String[] expected = {allNames[allNames.length - 1]};
        String[] observed = tree.getDifferentTaxaNames(allTaxa, sampledTaxa);

        assertArrayEquals(expected,observed);
    }
}
