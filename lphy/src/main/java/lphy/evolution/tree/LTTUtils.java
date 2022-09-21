package lphy.evolution.tree;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Lineages through time (LTT)
 * @author Walter Xie
 */
public class LTTUtils {

    /**
     * @param tree  Ultrametric tree
     * @return      Lineages through time (LTT) in a {@link NavigableMap},
     *              whose key is age, and value is number of lineages.
     *              Note: before plotting by time in x-axis, ages need to multiply -1.
     */
    public static NavigableMap<Double, Integer> getLTTFromUltrametricTree(final TimeTree tree) {
        if (tree.leafCount() < 3)
            throw new IllegalArgumentException("It is not a legal time tree !\n" +
                    "leaf count = " + tree.leafCount() + ", tree = " + tree);
        if (!tree.isUltrametric())
            throw new IllegalArgumentException("Ultrametric tree is expected ! ");

        // ordered age , lineages
        NavigableMap<Double, Integer> lttMap = new TreeMap<>();

        List<TimeTreeNode> nodes = tree.getInternalNodes();
        // must add root first
        lttMap.put(tree.getRoot().getAge(), 2);

        // assuming the nodes list contains tips and then nodes, and the last is root.
        for (int i = nodes.size(); i-- > 0; ) {
            TimeTreeNode n = nodes.get(i);
            // all ages > or == 0
            final double age = n.getAge();
            // proceed if not root and age > 0
            if (!n.isRoot() && age > 0) {

                if (lttMap.containsKey(age)) {
                    // existing age
                    final int l = lttMap.get(age);
                    lttMap.put(age, l + 1);
                } else if (age < lttMap.firstKey()) {
                    // new age, but younger (closer to 0) than all existing ages
                    lttMap.put(age, lttMap.firstEntry().getValue() + 1);
                } else {
                    // new age, but between existing ages
                    for (var entry : lttMap.entrySet()) {
                        // compare to existing ages in descending order
                        final int l = lttMap.get(entry.getKey());
                        if (age > entry.getKey()) {
                            lttMap.put(entry.getKey(), l + 1);
                        } else {
                            // +1 lineage
                            lttMap.put(age, l + 1);
                            break;
                        }
                    } // end for
                } // end if

            } // end if isRoot
        } // end for

        return lttMap;
    }


    public static NavigableMap<Double, Integer> getLTTFromTimeTree(final TimeTree tree) {
        // ordered age , lineages
        NavigableMap<Double, Integer> lttMap = new TreeMap<>();
        // include tips
        List<TimeTreeNode> nodes = tree.getNodes();
        // must add root first
        lttMap.put(tree.getRoot().getAge(), 2);

        // assuming the nodes list contains tips and then nodes, and the last is root.
        for (int i = nodes.size(); i-- > 0; ) {
            TimeTreeNode n = nodes.get(i);
            // all ages > or == 0
            final double age = n.getAge();
            // proceed if not root
            if (!n.isRoot()) {

                if (lttMap.containsKey(age)) {
                    // existing age
                    final int l = lttMap.get(age);
                    lttMap.put(age, l + 1);
                } else if (age < lttMap.firstKey()) {
                    // new age, but younger (closer to 0) than all existing ages
                    // lineage of the youngest existing age
                    final int l = lttMap.firstEntry().getValue();
                    if (!n.isLeaf())
                        // +1 if it is not leaf node
                        lttMap.put(age,  l + 1);
                    else
                        // -1 if it is leaf node, in this case these leaf nodes are always younger than internal nodes
                        lttMap.put(age,  l - 1);
                } else {
                    // new age, but between existing ages
                    for (var entry : lttMap.entrySet()) {
                        // compare to existing ages in descending order
                        final int l = lttMap.get(entry.getKey());
                        if (age > entry.getKey()) {
                            // +1 lineage for every existing ages if it is not leaf node
                            // here leaf node age > 0
                            if (!n.isLeaf())
                                lttMap.put(entry.getKey(), l + 1);
                            else
                                lttMap.put(entry.getKey(), l - 1);
                        } else {
                            // add this new count
                            if (!n.isLeaf())
                                // +1 lineage if it is not leaf node
                                lttMap.put(age, l + 1);
                            else
                                // -1 lineage if it is leaf node
                                lttMap.put(age, l - 1);
                            break;
                        }
                    }
                } // end if
            } // end if isRoot
        } // end for loop

        System.out.println(Arrays.toString(nodes.stream().mapToDouble(TimeTreeNode::getAge).toArray()));

        return lttMap;
    }
}
