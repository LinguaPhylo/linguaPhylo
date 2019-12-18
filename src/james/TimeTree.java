package james;

import james.graphicalModel.RandomVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public class TimeTree {

    TimeTreeNode rootNode;

    List<TimeTreeNode> nodes;

    // number of leaves
    int n = 0;

    public void setRoot(TimeTreeNode timeTreeNode) {
        rootNode = timeTreeNode;
        nodes = new ArrayList<>();

        fillNodeList(rootNode);
    }

    private int fillNodeList(TimeTreeNode node) {
        if (node.isRoot()) {
            nodes.clear();
            n = 0;
        }

        if (node.isLeaf()) {
            nodes.add(node);
            n += 1;
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillNodeList(child);
            }
            nodes.add(node);
        }

        return nodes.size();
    }

    public int n() {
        return n;
    }
}
