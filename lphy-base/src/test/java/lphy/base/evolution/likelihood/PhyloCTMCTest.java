package lphy.base.evolution.likelihood;

import lphy.base.evolution.branchrate.LocalClock;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.function.tree.Newick;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PhyloCTMCTest {
    String newickTree;

    @BeforeEach
    void setUp() {
        newickTree = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
    }

    @Test
    void apply() {
        // generate a tree with local clock
        TimeTree tree = Newick.parseNewick(newickTree);
        TimeTreeNode node1 = null;
        TimeTreeNode node2 = null;

        for (int i = 0; i<tree.getNodes().size(); i++){
            if (Objects.equals(tree.getNodes().get(i).getId(), "4")){ //node2 is the leaf node 4
                node2 = tree.getNodes().get(i);
            } else if (tree.getNodes().get(i).getAllLeafNodes().size() == 2){ //node1 is the parent of (2,3)
                node1 = tree.getNodes().get(i);
            }
        }

        assertNotNull(node1);
        assertNotNull(node2);

        TimeTreeNode[] clades = {node1, node2};
        Double[] cladeRates = {0.4, 0.3};
        double rootRate = 0.2;

        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Value<Object[]> cladesValue = new Value<>("clades", clades);
        Value<Double[]> cladeRatesValue = new Value<>("cladeRates", cladeRates);
        Value<Double> rootRateValue = new Value<>("rootRate" , rootRate);
        Value<Boolean> includeStemValue = new Value<>("includeStem" , Boolean.TRUE);

        LocalClock localClockInstance = new LocalClock(treeValue, cladesValue, cladeRatesValue, rootRateValue, includeStemValue);
        TimeTree newTree = localClockInstance.apply().value();

        // test PhyloCTMC
        Double[][] Q = {
                { -1.0,  0.5,  0.3,  0.2 },
                {  0.4, -1.0,  0.1,  0.5 },
                {  0.3,  0.2, -1.0,  0.5 },
                {  0.2,  0.3,  0.5, -1.0 }
        };


        Value<TimeTree> newTreeValue = new Value<>("newTree", newTree);
        Value<Double[][]> QValue = new Value<>("Q", Q);
        Value<Double[]> siteRatesValue = new Value<Double[]>("siteRates", new Double[]{0.1, 0.1, 0.1, 0.1, 0.1});
        Value<Integer> LValue = new Value<Integer>("L", 5);

        PhyloCTMC phyloCTMCInstance = new PhyloCTMC(
                newTreeValue,null,null,
                QValue,siteRatesValue,null,
                LValue,null, null);

        phyloCTMCInstance.sample();

        Double[] branchRates = phyloCTMCInstance.getBranchRates().value();

        List<TimeTreeNode> allNodes = newTree.getNodes();
        assertEquals(allNodes.size(), branchRates.length);

        // index 0 : node 2
        assertEquals(allNodes.get(0).getBranchRate(), branchRates[0]);
        // index 1 : node 3
        assertEquals(allNodes.get(1).getBranchRate(), branchRates[1]);
        // index 2 : node 1
        assertEquals(allNodes.get(2).getBranchRate(), branchRates[2]);
        // index 3 : node 4
        assertEquals("4" , allNodes.get(3).getId());
        assertEquals(0.3, allNodes.get(3).getBranchRate());
        assertEquals(allNodes.get(3).getBranchRate(), branchRates[3]);
        // index 4 : node (2,3)
        assertEquals(allNodes.get(4).getBranchRate(), branchRates[4]);
        // index 5 : node ((2,3),1)
        assertEquals(allNodes.get(5).getBranchRate(), branchRates[5]);
        // index 6 : node (((2,3),1),4)
        assertEquals(allNodes.get(6).getBranchRate(), branchRates[6]);
    }

    @Test
    void testWarning() {
        TimeTree tree = Newick.parseNewick(newickTree);
        for (TimeTreeNode node: tree.getNodes()){
            node.setBranchRate(0.5);
        }

        Double[][] Q = {
                { -1.0,  0.5,  0.3,  0.2 },
                {  0.4, -1.0,  0.1,  0.5 },
                {  0.3,  0.2, -1.0,  0.5 },
                {  0.2,  0.3,  0.5, -1.0 }
        };

        Value<TimeTree> newTreeValue = new Value<>("newTree", tree);
        Value<Double[][]> QValue = new Value<>("Q", Q);
        Value<Double[]> siteRatesValue = new Value<Double[]>("siteRates", new Double[]{0.1, 0.1, 0.1, 0.1, 0.1});
        Value<Integer> LValue = new Value<Integer>("L", 5);
        Value<Double[]> branchRatesValue = new Value<>("branchRates", new Double[]{0.2,0.2,0.2,0.2,0.2,0.2,0.2});

        PhyloCTMC phyloCTMCInstance = new PhyloCTMC(
                newTreeValue,null,null,
                QValue,siteRatesValue,branchRatesValue,
                LValue,null, null);

        phyloCTMCInstance.sample();
        // should have a warning msg

        Double[] branchRates = phyloCTMCInstance.getBranchRates().value();

        List<TimeTreeNode> allNodes = tree.getNodes();
        assertEquals(allNodes.size(), branchRates.length);

        // should use given branch rates and tree branch rates should not be changed
        for (int i = 0; i<allNodes.size(); i++){
            assertEquals(0.2, branchRates[i]);
            assertEquals(0.5, allNodes.get(i).getBranchRate());
        }
    }
}
