package lphy.base.evolution.tree;

import lphy.base.evolution.birthdeath.CalibratedYule;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CalibratedYuleTest {
    @Test
    void test1() { //test one clade taxa
        double birthRate = 0.25;
        int n = 520;
        String[] taxa = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        Number[] cladeAge = new Number[]{5.5};

        Value<Number> birthRateValue = new Value<>("birthRate", birthRate);
        Value<Integer> nValue = new Value<>("n", n);
        Value taxaValue = new Value("taxa", taxa);
        Value<Number[]> cladeAgeValue = new Value<>("cladeAge", cladeAge);

        CalibratedYule instance = new CalibratedYule(birthRateValue, nValue, taxaValue, cladeAgeValue, null, null);
        TimeTree observe = instance.sample().value();

        // node number should be same
        assertEquals(n , observe.getRoot().getAllLeafNodes().size());
        // randomly check the names for clade taxa
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_1"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_3"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_10"));

    }

    @Test
    void randomNodeTest() {
        TimeTreeNode node1 = new TimeTreeNode(1.0);
        TimeTreeNode node2 = new TimeTreeNode(1.1);
        TimeTreeNode node3 = new TimeTreeNode(1.2);
        TimeTreeNode node4 = new TimeTreeNode(1.3);

        List<TimeTreeNode> activeNodes = new ArrayList<>();
        activeNodes.add(node1);
        activeNodes.add(node2);
        activeNodes.add(node3);
        activeNodes.add(node4);

        for (int i = 0; i<30; i++) {
            List<TimeTreeNode> observe = CalibratedYule.randomTwoNodes(activeNodes);
            double age1 = observe.get(0).age;
            double age2 = observe.get(1).age;
            assertNotEquals(age1, age2, "The ages should not be equal");
        }
    }

    @Test
    void test2() { // test multiple clade taxa
        double birthRate = 0.25;
        int n = 520;
        String[] taxa1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] taxa2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[][] taxa = {taxa1, taxa2};
        Number[] cladeAge = new Number[]{5.5 , 7};

        Value<Number> birthRateValue = new Value<>("birthRate", birthRate);
        Value<Integer> nValue = new Value<>("n", n);
        Value taxaValue = new Value("taxa", taxa);
        Value<Number[]> cladeAgeValue = new Value<>("cladeAge", cladeAge);
        Value<Number> rootAgeValue = new Value<>("rootAge", 19);

        CalibratedYule instance = new CalibratedYule(birthRateValue, nValue, taxaValue, cladeAgeValue, null, rootAgeValue);
        TimeTree observe = instance.sample().value();

        // node number should be same
        assertEquals(n , observe.getRoot().getAllLeafNodes().size());
        // check root age
        assertEquals(19, observe.getRoot().age);
        // randomly draw and check leaf node names
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade0_1"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade1_3"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade0_10"));
    }

    @Test
    void test3() { // test same clade names
        double birthRate = 0.25;
        int n = 520;
        String[] taxa1 = {"taxa1", "taxa2", "taxa3", "taxa4", "taxa5", "taxa6", "taxa7", "taxa8", "taxa9", "taxa10"};
        String[] taxa2 = {"taxa1", "taxa2", "taxa3", "taxa4", "taxa5", "taxa6", "taxa7", "taxa8", "taxa9", "taxa10"};
        String[][] taxa = {taxa1, taxa2};
        Number[] cladeAge = new Number[]{5.5 , 7};

        Value<Number> birthRateValue = new Value<>("birthRate", birthRate);
        Value<Integer> nValue = new Value<>("n", n);
        Value taxaValue = new Value("taxa", taxa);
        Value<Number[]> cladeAgeValue = new Value<>("cladeAge", cladeAge);

        CalibratedYule instance = new CalibratedYule(birthRateValue, nValue, taxaValue, cladeAgeValue, null, null);
        TimeTree observe = instance.sample().value();

        // node number should be same
        assertEquals(n , observe.getRoot().getAllLeafNodes().size());

        // randomly draw and check leaf node names
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade0_taxa1"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade1_taxa3"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade0_taxa10"));
    }

    @Test
    void coalesceTest() {
        double birthRate = 0.1;
        int n = 20;
        String[] taxa = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        Number[] cladeAge = new Number[]{29};

        Value<Number> birthRateValue = new Value<>("birthRate", birthRate);
        Value<Integer> nValue = new Value<>("n", n);
        Value taxaValue = new Value("taxa", taxa);
        Value<Number[]> cladeAgeValue = new Value<>("cladeAge", cladeAge);
        Value<Number> rootAgeValue = new Value<>("rootAge", 31);

        CalibratedYule instance = new CalibratedYule(birthRateValue, nValue, taxaValue, cladeAgeValue, null, rootAgeValue);
        TimeTree observe = instance.sample().value();

        // node number should be same
        assertEquals(n , observe.getRoot().getAllLeafNodes().size());
        // randomly check the names for clade taxa
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_1"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_3"));
        assert observe.getRoot().getAllLeafNodes().stream().anyMatch(node -> node.getId().equals("clade_10"));
    }
}
