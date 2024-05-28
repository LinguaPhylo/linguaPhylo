package lphy.base.evolution.tree;

import lphy.base.function.tree.Newick;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstituteCladeTest {
    String baseTreeNewick;
    String cladeTreeNewick;

    @BeforeEach
    void setUp() {
        cladeTreeNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
        baseTreeNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):6.0, 4:8.0)";
    }

    @Test
    void applyTest() {
        TimeTree baseTree = Newick.parseNewick(baseTreeNewick);
        TimeTree cladeTree = Newick.parseNewick(cladeTreeNewick);
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
        assertEquals(6, observe.value().getInternalNodes().size());

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
        TimeTree baseTree = Newick.parseNewick(baseTreeNewick);
        TimeTree cladeTree = Newick.parseNewick(cladeTreeNewick);
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
        List<Integer> indices = new ArrayList<>();
        for (TimeTreeNode anyNode: observe.value().getNodes()){
            String metaData = (String) anyNode.getMetaData("label");
            indices.add(anyNode.getIndex());
            if (nodeLabel.equals(metaData)){
                assertEquals(4.0, anyNode.age);
                assertEquals(4.0, observe.value().getLabeledNode(nodeLabel).age);
            }
        }
        Collections.sort(indices);
        // check the indicies
        assertEquals(List.of(0,1,2,3,4,5,6,7,8,9,10,11,12), indices);
    }
}

