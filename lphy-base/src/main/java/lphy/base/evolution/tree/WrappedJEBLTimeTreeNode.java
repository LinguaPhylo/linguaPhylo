package lphy.base.evolution.tree;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.util.List;
import java.util.Map;

public class WrappedJEBLTimeTreeNode extends TimeTreeNode {

    protected jebl.evolution.graphs.Node jeblNode;

    public WrappedJEBLTimeTreeNode(Node jeblNode, RootedTree rootedTree, TimeTree timeTree) {
        //  this.tree = timeTree
        //TODO how to get id in jebl nodes?
        super(jeblNode.toString(), timeTree);
        this.jeblNode = jeblNode;

        deepCopyJEBL(jeblNode, rootedTree);

    }



    public void deepCopyJEBL(Node jeblNode, RootedTree rootedTree) {

        // length or height ?
        this.age = rootedTree.getLength(jeblNode);
        // ? id , index  leafIndex

        Map<String, Object> attrs = jeblNode.getAttributeMap();
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            this.setMetaData(entry.getKey(), entry.getValue());
        }

        if (rootedTree.isExternal(jeblNode)) {
            String name = rootedTree.getTaxon(jeblNode).getName();
            this.setId(name);
        } else {
            List<Node> children = rootedTree.getChildren(jeblNode);

            for (Node childJebl : children) {
                WrappedJEBLTimeTreeNode timeTreeChildNode =
                        new WrappedJEBLTimeTreeNode(childJebl, rootedTree, this.tree);
                timeTreeChildNode.setParent(this);
                this.addChild(timeTreeChildNode);
            }
        }
//        return this;
    }


}
