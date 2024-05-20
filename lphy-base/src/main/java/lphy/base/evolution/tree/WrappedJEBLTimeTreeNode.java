package lphy.base.evolution.tree;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import lphy.base.evolution.Taxa;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * To help convert JEBL tree into lphy TimeTree
 */
public class WrappedJEBLTimeTreeNode extends TimeTreeNode {

    protected jebl.evolution.graphs.Node jeblNode;

    /**
     * If jeblNode is the root, then this will recursively copy all child nodes.
     * Use {@link Utils#convert(Tree)} to covert the tree.
     * @param jeblNode          jebl node, such as root.
     * @param jeblRootedTree    jebl tree
     * @param timeTree          lphy tree
     */
    public WrappedJEBLTimeTreeNode(Node jeblNode, RootedTree jeblRootedTree, TimeTree timeTree) {
        //  this.tree = timeTree
        // set id later
        super("ToSet", timeTree);

        this.jeblNode = jeblNode;
        // jebl Node does not have id, but use "label" Attribute
        Object nodeID = jeblNode.getAttribute("label");
        // In PhyloCTMC, if internal nodes have id, then simulate sequences,
        // otherwise only sequences on tips.
        if (nodeID != null)
            this.setId(nodeID.toString());
        else
            this.setId(null);

        deepCopyJEBLNodesStartFrom(jeblNode, jeblRootedTree);
    }

    /**
     * Copy the information from JEBL {@link Node} to this {@link TimeTreeNode},
     * and to create children nodes.
     * @param jeblRootNode
     * @param jeblRootedTree
     */
    protected void deepCopyJEBLNodesStartFrom(Node jeblRootNode, RootedTree jeblRootedTree) {

        // length or height ?
        this.age = jeblRootedTree.getHeight(jeblRootNode);
        // TODO more ? Find the attribute label to map them, such as species ...

        // copy all attributes into MetaData map
        Map<String, Object> attrs = jeblRootNode.getAttributeMap();
        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            this.setMetaData(entry.getKey(), entry.getValue());
        }

        // recursive to all children
        if (jeblRootedTree.isExternal(jeblRootNode)) {
            String name = jeblRootedTree.getTaxon(jeblRootNode).getName();
            this.setId(name);
        } else {
            List<Node> children = jeblRootedTree.getChildren(jeblRootNode);

            // In PhyloCTMC, if internal nodes have id, then simulate sequences,
            // otherwise only sequences on tips.
            for (Node childJebl : children) {
                WrappedJEBLTimeTreeNode timeTreeChildNode =
                        new WrappedJEBLTimeTreeNode(childJebl, jeblRootedTree, this.tree);
                timeTreeChildNode.setParent(this);
                this.addChild(timeTreeChildNode);
            }
        }
    }

    public static class Utils {

        /**
         * @param jeblTree  a JEBL {@link Tree}.
         * @return          LPhy {@link TimeTree} converted from a JEBL {@link Tree}.
         */
        public static TimeTree convert(Tree jeblTree) {

            if (jeblTree instanceof SimpleRootedTree rootedTree) {

                Set<Taxon> jeblTaxa = rootedTree.getTaxa();
                Taxa taxa = Taxa.createTaxa(jeblTaxa.toArray());

                // init TimeTree
                TimeTree timeTree = new TimeTree(taxa);
                // start from root and recursively create all children nodes.
                TimeTreeNode root = new WrappedJEBLTimeTreeNode(rootedTree.getRootNode(), rootedTree, timeTree);
                timeTree.setRoot(root);

                return timeTree;

            } else throw new IllegalArgumentException("LPhy requires the rooted tree !");

        }

    }
}
