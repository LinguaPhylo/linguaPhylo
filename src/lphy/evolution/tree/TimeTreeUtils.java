package lphy.evolution.tree;

import java.util.ArrayList;
import java.util.List;

public class TimeTreeUtils {

    private static String markLabel = "mark";

    public static TimeTreeNode getFirstNonSingleChildNode(TimeTree tree) {
        return getFirstNonSingleChildNode(tree.getRoot());
    }

    private static TimeTreeNode getFirstNonSingleChildNode(TimeTreeNode node) {
        if (node.getChildCount() != 1) return node;
        return getFirstNonSingleChildNode(node.getChildren().get(0));
    }

    public static void removeSingleChildNodes(TimeTree tree) {
        removeSingleChildNodes(tree, false);
    }

    /**
     *
     * @param tree the tree to remove single-child nodes from
     * @param onlyAnonymous if true then remove only the single-child nodes that have a null id. These are generally produced by pruning one of the other children of an internal node.
     */
    public static void removeSingleChildNodes(TimeTree tree, boolean onlyAnonymous) {
        removeSingleChildNodes(tree.getRoot(), onlyAnonymous);
    }

    /**
     *
     * @param node
     * @param onlyAnonymous if true then emove only the single-child nodes that have a null id. These are generally produced by pruning one of the other children of an internal node.
     */
    public static void removeSingleChildNodes(TimeTreeNode node, boolean onlyAnonymous) {
        if (node.isSingleChildNonOrigin() && (!onlyAnonymous || node.getId() == null)) {
            TimeTreeNode grandChild = node.getChildren().get(0);
            TimeTreeNode parent = node.getParent();
            parent.removeChild(node);
            node.removeChild(grandChild);
            parent.addChild(grandChild);
            removeSingleChildNodes(grandChild, onlyAnonymous);
        } else {
            List<TimeTreeNode> copy = new ArrayList<>();
            copy.addAll(node.getChildren());
            for (TimeTreeNode child : copy) {
                removeSingleChildNodes(child, onlyAnonymous);
            }
        }
    }

    public static void removeUnmarkedNodes(TimeTree tree) {
        removeUnmarkedNodes(tree.getRoot());
    }

    private static void removeUnmarkedNodes(TimeTreeNode node) {
        if (!isMarked(node)) {
            if (node.isRoot()) throw new RuntimeException("Root should always be marked! Something is very wrong!");
            node.getParent().removeChild(node);
        } else if (!node.isLeaf()) {
            List<TimeTreeNode> copy = new ArrayList<>();
            copy.addAll(node.getChildren());
            for (TimeTreeNode child : copy) {
                removeUnmarkedNodes(child);
            }
        }

    }

    private static boolean isMarked(TimeTreeNode node) {
        Object markObject = node.getMetaData(markLabel);
        return markObject != null;
    }

    public static void markNodeAndDirectAncestors(TimeTreeNode node) {
        if (node != null) {
            node.setMetaData(markLabel, true);
            markNodeAndDirectAncestors(node.getParent());
        }
    }
}
