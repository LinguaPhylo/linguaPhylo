package lphy.base.evolution.tree;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MRCA extends DeterministicFunction<TimeTreeNode> {
    Value<TimeTree> tree;
    Value<String[]> taxa;
    public static final String treeName = "tree";
    public static final String taxaName = "taxa";
    public MRCA(@ParameterInfo(name = treeName, description = "the tree to look for most recent common ancestor") Value<TimeTree> tree,
                @ParameterInfo(name = taxaName, narrativeName = "leaf node names", description = "the array of taxa names to look for most recent common ancestor") Value<String[]> taxa){
        if (tree == null) throw new IllegalArgumentException("The tree cannot be null!");
        if (taxa == null) throw new IllegalArgumentException("The taxa names cannot be null!");
        setParam(treeName, tree);
        setParam(taxaName, taxa);
        this.tree = tree;
        this.taxa = taxa;
    }

    @GeneratorInfo(name = "mrca", description = "get the most recent common ancestor from given tree and taxa names.")
    @Override
    public Value<TimeTreeNode> apply() {
        TimeTree tree = getTree().value();
        String[] taxa = getTaxa().value();
        List<TimeTreeNode> leafNodes = tree.getRoot().getAllLeafNodes();

        //get the leaf nodes in a node list
        List<TimeTreeNode> nodeList = new ArrayList<>();
        Set<String> leafNodeIds = tree.getRoot().getAllLeafNodes().stream().map(TimeTreeNode::getId).collect(Collectors.toSet());
        for (int i = 0; i<taxa.length; i++){
            // if the given taxa is not belong to this tree, then throw exception
            if (!leafNodeIds.contains(taxa[i])) throw new IllegalArgumentException("Taxa "+ taxa[i] + "is not part of the given tree.");
            // get all the taxa names into nodes
            for (TimeTreeNode node : leafNodes) {
                if (node.getId().equals(taxa[i])) {
                    nodeList.add(node);
                }
            }
        }

        // find the most recent common ancestor with the node list
        TimeTreeNode current = findMRCA(nodeList);

        return new Value<>(current,this);
    }

    private static TimeTreeNode findMRCA(List<TimeTreeNode> nodeList) {
        // initialise a set to store all ancestors
        Set<TimeTreeNode> ancestors = new HashSet<>();

        // get all ancestors for the first node
        TimeTreeNode current = nodeList.get(0);
        while (current != null) { // run till get the parent of root
            ancestors.add(current);
            current = current.getParent();
        }

        // find other nodes' ancestors in the first node ancestors
        for (int i = 1; i < nodeList.size(); i++) {
            current = nodeList.get(i);
            Set<TimeTreeNode> currentAncestors = new HashSet<>();
            while (current != null) {
                currentAncestors.add(current);
                current = current.getParent();
            }
            // get the intersection of the two sets of ancestors
            ancestors.retainAll(currentAncestors);
        }

        // if ancestors contain more than one element
        TimeTreeNode mrca = null;
        if (ancestors.size() == 1){
            mrca = (TimeTreeNode) ancestors.toArray()[0];
        } else {
            // assume mrca be the first element then check
            mrca = (TimeTreeNode) ancestors.toArray()[0];
            for (TimeTreeNode node : ancestors){
                if (mrca.age > node.age){
                    mrca = node;
                }
            }
        }

        return mrca;
    }

    public Value<TimeTree> getTree() {
        return getParams().get(treeName);
    }

    public Value<String[]> getTaxa(){
        return getParams().get(taxaName);
    }
}
