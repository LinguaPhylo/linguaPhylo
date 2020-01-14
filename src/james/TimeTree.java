package james;

import james.swing.HasComponentView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adru001 on 17/12/19.
 */
public class TimeTree implements HasComponentView {

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

    TimeTreeComponent component;
    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = new TimeTreeComponent(this);
        } else {
            component.setTimeTree(this);
        }
        return component;
    }
}
