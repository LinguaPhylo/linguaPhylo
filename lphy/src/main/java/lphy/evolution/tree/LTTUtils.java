package lphy.evolution.tree;

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
    public static NavigableMap<Double, Integer> getLTTFromUltrametricTree(TimeTree tree) {
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
            // skip if not root and age > 0
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
                    }
                } // end if

            } // end if isRoot
        } // end for

        return lttMap;
    }

}
