package lphy.base.evolution.tree;

import lphy.base.function.tree.Newick;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LabelCladeTest {
    String newickTree;

    @BeforeEach
    void setUp() {
        newickTree = "(((((1:2.0, (2:1.0, 3:1.0):1.0):2.0, (5:2.0, 6:2.0):2.0):2.0):0.0,4:6.0):6.0, 7:12.0)";
    }

    @Test
    void applyTest1() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"4", "7"};
        String label = "label";

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        Value<String> labelValue = new Value<>("label", label);

        LabelClade labelCladeInstance = new LabelClade(treeValue, taxaValue, labelValue);
        MRCA mrcaInstance = new MRCA(treeValue,taxaValue);
        TimeTree observe = labelCladeInstance.apply().value();
        TimeTreeNode mrcaNode = mrcaInstance.apply().value();

        // only index should be same for mrca
        assertEquals(mrcaNode.getIndex(), observe.getRoot().getIndex());
        // check label
        assertEquals(label, observe.getRoot().getMetaData("label"));
        assertEquals(observe.getRoot(), observe.getLabeledNode(label));
        assertNull(observe.getRoot().getId());
    }

    @Test
    void applyTest2() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"3", "5"};
        String label = "label";

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        Value<String> labelValue = new Value<>("label", label);

        LabelClade labelCladeInstance = new LabelClade(treeValue, taxaValue, labelValue);
        MRCA mrcaInstance = new MRCA(treeValue,taxaValue);
        TimeTree observe = labelCladeInstance.apply().value();
        TimeTreeNode mrcaNode = mrcaInstance.apply().value();

        boolean found = false;
        for (TimeTreeNode node: observe.getInternalNodes()){
            if (node.getAllLeafNodes().size() == 5 && node.age == 4){
                assertEquals(node.getIndex(), mrcaNode.getIndex());
                assertEquals(label, node.getMetaData("label"));
                assertEquals(node, observe.getLabeledNode(label));
                found = true;
            }
        }
        assert(found);
    }
}
