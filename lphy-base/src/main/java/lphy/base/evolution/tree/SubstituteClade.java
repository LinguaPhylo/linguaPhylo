package lphy.base.evolution.tree;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SubstituteClade extends DeterministicFunction<TimeTree> {
    Value<TimeTree> baseTree;
    Value<TimeTree> cladeTree;
    Value<Double> time;
    Value<TimeTreeBranch> branch;
    Value<String> nodeLabel;
    public static final String baseTreeName = "baseTree";
    public static final String cladeTreeName = "cladeTree";
    public static final String branchName = "branch";
    public static final String mutationHappenTimeName = "time";
    public static final String nodeLabelName = "nodeLabel";

    public SubstituteClade(@ParameterInfo(name = baseTreeName, description = "the tree that we are going to add another tree onto.") Value<TimeTree> baseTree,
                           @ParameterInfo(name = cladeTreeName, description = "the tree that we are going to add it on the base tree") Value<TimeTree> cladeTree,
                           @ParameterInfo(name = branchName, description = "the branch that the branch tree would be add on to.") Value<TimeTreeBranch> branch,
                           @ParameterInfo(name = mutationHappenTimeName, description = "the mutation happen time that the branch tree would be add onto the base tree") Value<Double> time,
                           @ParameterInfo(name = nodeLabelName, description = "the name of added branch node.") Value<String> nodeLabel) {
        if (baseTree == null) throw new IllegalArgumentException("The base tree cannot be null!");
        if (cladeTree == null) throw new IllegalArgumentException("The clade tree cannot be null!");
        if (time == null) throw new IllegalArgumentException("The happening time cannot be null!");
        if (branch == null) throw new IllegalArgumentException("Please specify the branch!");
        if (nodeLabel == null) throw new IllegalArgumentException("Please label the root of cladeTree!");
        setParam(baseTreeName, baseTree);
        setParam(cladeTreeName, cladeTree);
        setParam(branchName, branch);
        setParam(mutationHappenTimeName, time);
        setParam(nodeLabelName,nodeLabel);
        this.baseTree = baseTree;
        this.cladeTree = cladeTree;
        this.branch = branch;
        this.time = time;
        this.nodeLabel = nodeLabel;
    }

    @GeneratorInfo(name = "substituteClade", description = "Substitute a clade in a tree with a given branch and time, as well as the label of the clade root node. The original child clade would be replaced by the give tree." )
    @Override
    public Value<TimeTree> apply() {
        // get parameters
        TimeTree baseTree = getBaseTree().value();
        TimeTree cladeTree = getCladeTree().value();
        TimeTreeBranch branch = getBranch().value();
        Double time = getMutationHappenTimeName().value();
        String nodeLabel = getNodeLabel().value();

        // make deep copy of trees
        TimeTree newTree = new TimeTree(baseTree);
        TimeTree newClade = new TimeTree(cladeTree);

        // give the nodes in clade tree new names
        for (TimeTreeNode leaf : newClade.getRoot().getAllLeafNodes()){
            leaf.setId("clade_" + leaf.getId());
        }

        // get the child node
        TimeTreeNode childNode = branch.getChildNode();
        Double childAge = childNode.getAge();
        // get the parent node
        TimeTreeNode parentNode = branch.getParentNode();
        Double parentAge = parentNode.getAge();

        // get nodes in the copy tree
        int parentNodeIndex = parentNode.getIndex();
        TimeTreeNode newParentNode = newTree.getNodeByIndex(parentNodeIndex);
        int childNodeIndex = childNode.getIndex();
        TimeTreeNode newChildNode = newTree.getNodeByIndex(childNodeIndex);

        // remove the original node
        newParentNode.removeChild(newChildNode);

        // add branch tree as clade
        TimeTreeNode cladeRoot = newClade.getRoot();
        newParentNode.addChild(cladeRoot);
        cladeRoot.setParent(newParentNode);

        Double branchLength = branch.getBranchLength();
        // how long the branch should be from parent node to the time
        Double fraction = (time - parentAge)/(childAge - parentAge); // kept branch length in whole branch length
        Double keptBranchLength = fraction * branchLength;
        // modify the stem length of clade node
        TimeTreeBranch newBranch = new TimeTreeBranch(newParentNode, cladeRoot);
        // new branch length is original length plus kept length
        newBranch.setBranchLength(keptBranchLength + newBranch.getBranchLength());

        // label the root of the clade
        cladeRoot.setId(nodeLabel);

        // reset the index in the new tree
        List<TimeTreeNode> allNodes = newTree.getNodes();
        for(int i = 0; i < allNodes.size(); i++){
            allNodes.get(i).setIndex(i);
        }

        List<TimeTreeBranch> allBranches = newTree.getBranches();
        for(int i = 0; i < allBranches.size(); i++){
            allBranches.get(i).setIndex(i);
        }

        return new Value<>(newTree, this);
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (baseTree != null) map.put(baseTreeName, baseTree);
        if (cladeTree != null) map.put(cladeTreeName, cladeTree);
        if (branch != null) map.put(branchName, branch);
        if (time != null) map.put(mutationHappenTimeName, time);
        if (nodeLabelName != null) map.put(nodeLabelName, nodeLabel);
        return map;
    }
    public Value<TimeTree> getBaseTree() {
        return getParams().get(baseTreeName);
    }
    public Value<TimeTree> getCladeTree() {
        return getParams().get(cladeTreeName);
    }
    public Value<TimeTreeBranch> getBranch(){
        return getParams().get(branchName);
    }
    public Value<Double> getMutationHappenTimeName() {
        return getParams().get(mutationHappenTimeName);
    }
    public Value<String> getNodeLabel(){
        return getParams().get(nodeLabelName);
    }
}
