package lphy.base.evolution.tree;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class SubstituteClade extends DeterministicFunction<TimeTree> {
    public static final String baseTreeName = "baseTree";
    public static final String cladeTreeName = "cladeTree";
    public static final String nodeName = "node";
    public static final String mutationHappenTimeName = "time";
    public static final String nodeLabelName = "nodeLabel";

    public SubstituteClade(@ParameterInfo(name = baseTreeName, description = "the tree that we are going to add another tree onto.") Value<TimeTree> baseTree,
                           @ParameterInfo(name = cladeTreeName, description = "the tree that we are going to add it on the base tree") Value<TimeTree> cladeTree,
                           @ParameterInfo(name = nodeName, narrativeName = "node under the branch for attachment", description = "the node with the branch that the branch tree would be add on to.") Value<TimeTreeNode> node,
                           @ParameterInfo(name = mutationHappenTimeName, description = "the mutation happen time that the branch tree would be add onto the base tree", optional = true) Value<Double> time,
                           @ParameterInfo(name = nodeLabelName, description = "the name of added branch node.") Value<String> nodeLabel) {
        if (baseTree == null) throw new IllegalArgumentException("The base tree cannot be null!");
        if (cladeTree == null) throw new IllegalArgumentException("The clade tree cannot be null!");
        if (node == null) throw new IllegalArgumentException("Please specify the node!");
        if (nodeLabel == null) throw new IllegalArgumentException("Please label the root of cladeTree!");
        if (time != null && cladeTree.value().getRoot().age > time.value()) throw new IllegalArgumentException("The root age of the clade tree should be equal to or smaller than the time!");
        setParam(baseTreeName, baseTree);
        setParam(cladeTreeName, cladeTree);
        setParam(nodeName, node);
        // for optional, it must check if not null, then setParam
        if (time != null)
            setParam(mutationHappenTimeName, time);
        setParam(nodeLabelName,nodeLabel);
    }

    @GeneratorInfo(name = "substituteClade",
            category = GeneratorCategory.TREE, examples = {"substituteClade.lphy"},
            description = "Substitute a clade in a tree with a given node and time, as well as the label of the clade root node. The original child clade would be replaced by the give tree." )
    @Override
    public Value<TimeTree> apply() {
        // get parameters
        TimeTree baseTree = getBaseTree().value();
        TimeTree cladeTree = getCladeTree().value();
        TimeTreeNode node = getNode().value();
        String nodeLabel = getNodeLabel().value();

        // make deep copy of trees
        TimeTree newTree = new TimeTree(baseTree);
        TimeTree newClade = new TimeTree(cladeTree);

        // give the nodes in clade tree new names
        for (TimeTreeNode leaf : newClade.getRoot().getAllLeafNodes()){
            leaf.setId("clade_" + leaf.getId());
        }

        // get nodes in the copy tree
        int parentNodeIndex = node.getParent().getIndex();
        TimeTreeNode newParentNode = newTree.getNodeByIndex(parentNodeIndex);
        int childNodeIndex = node.getIndex();
        TimeTreeNode newChildNode = newTree.getNodeByIndex(childNodeIndex);

        // remove the original node
        newParentNode.removeChild(newChildNode);

        // add branch tree as clade
        TimeTreeNode cladeRoot = newClade.getRoot();
        newParentNode.addChild(cladeRoot);

        if (newParentNode.getChildCount() == 1 && !newParentNode.isRoot()){
            cladeRoot.setParent(newParentNode.getParent());
            newParentNode.getParent().removeChild(newParentNode);
            newParentNode.getParent().addChild(cladeRoot);
        } else if (newParentNode.getChildCount() == 2 && !newParentNode.isRoot()) {
            cladeRoot.setParent(newParentNode);
        } else if (newParentNode.getChildCount() == 1 && newParentNode.isRoot()){
            newTree.setRoot(cladeRoot);
        }

        // fill the node list and reindex the nodes
        newTree.setRoot(newTree.getRoot(),true);

        // set age and label for clade root
        cladeRoot.setAge(cladeRoot.getAge());
        cladeRoot.setMetaData("label", nodeLabel);

        return new Value<>(null, newTree, this);
    }

    public Value<TimeTree> getBaseTree() {
        return getParams().get(baseTreeName);
    }
    public Value<TimeTree> getCladeTree() {
        return getParams().get(cladeTreeName);
    }
    public Value<TimeTreeNode> getNode(){
        return getParams().get(nodeName);
    }
    public Value<Double> getMutationHappenTime(){
        return getParams().get(mutationHappenTimeName);
    }
    public Value<String> getNodeLabel(){
        return getParams().get(nodeLabelName);
    }
}
