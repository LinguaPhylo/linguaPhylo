package lphy.base.evolution.tree;

import lphy.base.evolution.coalescent.Coalescent;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeTreeBranchTest {
    final int nTaxa = 5;
    TimeTree tree;

    @BeforeEach
    void setUp() {
        Coalescent simulator = new Coalescent(new Value<>("Θ", 10.0), new Value<>("n", nTaxa), null);
        tree = Objects.requireNonNull(simulator.sample()).value();
    }

    @Test
    void branchLength() {
        List<TimeTreeBranch> branches = tree.getBranches();
        for (TimeTreeBranch branch : branches){
            branch.setBranchLength(2.0);
        }
        assertEquals(8,tree.getBranches().size());
        for (TimeTreeBranch branch: branches){
            assertEquals(2.0, branch.getBranchLength());
        }
    }

    @Test
    void ID() {
        List<TimeTreeBranch> branches = tree.getBranches();
        for (TimeTreeBranch branch : branches){
            branch.setId("id");
        }
        assertEquals(8,tree.getBranches().size());
        for (TimeTreeBranch branch: branches){
            assertEquals("id", branch.getId());
        }


    }

    @Test
    void node() {
        List<TimeTreeBranch> branches = tree.getBranches();
        List<TimeTreeNode> parentNodes = new ArrayList<>();
        for (TimeTreeBranch branch : branches){
            parentNodes.add(branch.getParentNode());
        }
        assertEquals(8, parentNodes.size());

        List<TimeTreeNode> ageZero = new ArrayList<>();
        for (TimeTreeNode parent : parentNodes){
            if (parent.age == 0){
                ageZero.add(parent);
            }
        }
        assertEquals(0,ageZero.size());

        // test for Exception
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> branches.get(0).setParentNode(null),
                "Expected IllegalArgumentException not thrown!"
        );

        List<TimeTreeNode> childrenNodes = new ArrayList<>();
        for (TimeTreeBranch branch : branches){
            childrenNodes.add(branch.getChildNode());
        }
        assertEquals(8, parentNodes.size());
        List<TimeTreeNode> ageZeroChildren = new ArrayList<>();
        for (TimeTreeNode child : childrenNodes){
            if (child.age == 0){
                ageZeroChildren.add(child);
            }
        }
        assertEquals(5, ageZeroChildren.size());

        // test for Exception
        IllegalArgumentException thrown_Child = assertThrows(
                IllegalArgumentException.class,
                () -> branches.get(0).setChildNode(null),
                "Expected IllegalArgumentException not thrown!"
        );
    }
}
