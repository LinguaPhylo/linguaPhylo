package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CPPTest {
    @Test
    public void testQdistBasic() {
        double result = CPPUtils.Qdist(2, 1, 1, 10);
        assertTrue(result > 0 && result < 1);
    }

    @Test
    void testCPPSimple() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<String[]> taxa = new Value<>("", new String[]{"1", "2", "3"});
        Value<Integer> n = new Value("", 3);
        CPPTree tree = new CPPTree(birthRate, deathRate,samplingProb, taxa, n, new Value<>("", 10), null);
        TimeTree sampledTree = tree.sample().value();
        assertEquals(3, sampledTree.getLeafNodes().size());
        assertEquals(10, sampledTree.getRoot().getAge());

        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("1"));
        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("3"));
        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("2"));
    }

    @Test
    void testCPP() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, null, null);
        TimeTree cppTree = cpp.sample().value();

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cpp.tree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && node.getAge() == 2) {
//                    System.out.println(children[0]);
//                    System.out.println(children[1]);
//                    System.out.println(children[2]);
                    assert (List.of(children).contains("1"));
                    assert (List.of(children).contains("2"));
                    assert (List.of(children).contains("3"));
                }
            }
        }

    }

    @Test
    void testCPP2() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, new Value<>("",10), null);
        TimeTree cppTree = cpp.sample().value();

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cpp.tree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && node.getAge() == 2) {
                    assert (List.of(children).contains("1"));
                    assert (List.of(children).contains("2"));
                    assert (List.of(children).contains("3"));
                }
            }
        }
        assertEquals(10, cppTree.getRoot().getAge());

    }

    @Test
    void testCPP3() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"},{"1","2"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0, 1.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, new Value<>("",10), null);
        TimeTree cppTree = cpp.sample().value();

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cpp.tree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && node.getAge() == 2) {
                    assert (List.of(children).contains("clade0_1"));
                    assert (List.of(children).contains("clade0_2"));
                    assert (List.of(children).contains("clade0_3"));
                }

                if (children.length == 2 && List.of(children).contains("clade0_1") && List.of(children).contains("clade0_2")){
                    //System.out.println("test nested");
                    assert(node.getAge() == 1);
                }
            }
        }

        assertEquals(10, cppTree.getRoot().getAge());
    }
}
