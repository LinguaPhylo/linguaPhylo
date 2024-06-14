package lphy.base.evolution.tree;

import lphy.base.function.tree.Newick;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MRCATest {
    String newickTree;

    @BeforeEach
    void setUp() {
        newickTree = "(((((1:2.0, (2:1.0, 3:1.0):1.0):2.0, (5:2.0, 6:2.0):2.0):2.0):0.0,4:6.0):6.0, 7:12.0)";
    }

    @Test
    void applyTest1() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"4", "7"};

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        MRCA instance = new MRCA(treeValue, taxaValue);

        TimeTreeNode observe = instance.apply().value();
        assertEquals(tree.getRoot(), observe);
    }

    @Test
    void applyTest2() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"5", "3"};

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        MRCA instance = new MRCA(treeValue, taxaValue);

        TimeTreeNode observe = instance.apply().value();
        for (TimeTreeNode node: tree.getInternalNodes()){
            if (node.getAllLeafNodes().size() == 5 && node.age == 4){
                assertEquals(node, observe);
            }
        }
    }

    @Test
    void applyTest3() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"5"};

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        MRCA instance = new MRCA(treeValue, taxaValue);

        TimeTreeNode observe = instance.apply().value();
        assertEquals(tree.getLeafNodes().get(3), observe);
    }

    @Test
    void applyTest4() {
        TimeTree tree = Newick.parseNewick(newickTree);
        String[] taxa = {"9"};

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<String[]> taxaValue = new Value<>("taxa", taxa);
        MRCA instance = new MRCA(treeValue, taxaValue);

        Throwable exception = assertThrows(IllegalArgumentException.class, instance::apply);

        // check whether the error message contain this content
        assert(exception.getMessage().contains("is not part of the given tree."));
    }
}
