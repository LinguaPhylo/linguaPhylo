package lphy.evolution.tree;

import java.util.ArrayList;
import java.util.List;

public class TimeTreeUtils {

    private static String markLabel = "mark";

    public static TimeTreeNode getFirstNonSingleChildNode(TimeTreeNode node) {
        if (node.getChildCount() != 1) return node;
        return getFirstNonSingleChildNode(node.getChildren().get(0));
    }

    public static void removeSingleChildNodes(TimeTreeNode node) {
        if (node.getChildCount() == 1) {
            TimeTreeNode grandChild = node.getChildren().get(0);
            TimeTreeNode parent = node.getParent();
            parent.removeChild(node);
            node.removeChild(grandChild);
            parent.addChild(grandChild);
            removeSingleChildNodes(grandChild);
        } else {
            List<TimeTreeNode> copy = new ArrayList<>();
            copy.addAll(node.getChildren());
            for (TimeTreeNode child : copy) {
                removeSingleChildNodes(child);
            }
        }
    }

    public static void removeUnmarkedNodes(TimeTreeNode node) {
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
