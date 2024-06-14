package lphy.base.evolution.tree;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.evolution.EvolutionConstants;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;


public class SubsampledTree extends ParametricDistribution<TimeTree> {
    Value<TimeTree> tree;
    Value<Object[][]> taxaName;
    Value<Double[]> sampleFraction;

    public static final String taxaParamName = EvolutionConstants.taxaParamName;
    public static final String sampleFractionParamName = "sampleFraction";
    public static final String treeParamName = "tree";

    public SubsampledTree(
            @ParameterInfo(name = treeParamName, narrativeName = "full tree", description = "the full tree to extract taxa from.") Value<TimeTree> tree,
            @ParameterInfo(name = taxaParamName, narrativeName = "taxa names", description = "the taxa name arrays that the function would sample") Value<Object[][]> taxaName,
            @ParameterInfo(name = sampleFractionParamName, narrativeName = "fraction of sampling", description = "the fractions that the function sample in the taxa") Value<Double[]> sampleFraction){
        if (tree == null) throw new IllegalArgumentException("The original tree cannot be null");
        if (taxaName.value().length != sampleFraction.value().length) throw new IllegalArgumentException("The sample fraction number should be same as the number of taxa name arrays!");
        this.sampleFraction = sampleFraction;
        this.tree = tree;
        this.taxaName = taxaName;
        if (taxaName.value() != null) {
            for (Object taxa : taxaName.value()) {
                if (!(taxa instanceof String[])) {
                    throw new IllegalArgumentException("The taxa array should be names!");
                }
            }
        }
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "SubsampledTree", description = "Generate the randomly sampled tree with given sample fractions and clade taxa name arrays within the given tree. The order of sample fractions are respectively matching the name arrays.")
    @Override
    public RandomVariable<TimeTree> sample() {
        TimeTree tree = getTree().value();
        String[][] taxaName = (String[][]) getTaxaName().value();
        Double[] sampleFraction = getSampleFraction().value();

        int sampleNumber = 0;
        // calculate the sampled names number
        for (int i = 0; i<taxaName.length; i++){
            sampleNumber += (int) Math.round(sampleFraction[i] * taxaName[i].length);
        }

        // initialise the sampledNames array
        String[] sampledNames = new String[sampleNumber];
        // let temp remember the index of last time sampledNames writing
        int temp = 0;

        // obtain the names for each taxaName array
        for (int i = 0; i<taxaName.length; i++){
            // get the leaf names
            String[] leafList = getLeafList(tree, taxaName[i]);
            // obtain corresponding sample fraction
            double fraction = sampleFraction[i];
            // randomly pick the taxa names
            String[] sample = getSampleResult(fraction, leafList);
            // write the result in the sampledNames array
            for(int j = 0; j < sample.length; j++){
                sampledNames[temp] = sample[j];
                temp ++;
            }
        }

        // make a deep copy of original tree
        TimeTree newTree = new TimeTree(tree);

        // remove unsampled taxa and reset parents
        getSampledTree(newTree, sampledNames);

        return new RandomVariable<>(null, newTree, this);
    }

    public static String[] getLeafList(TimeTree originalTree, String[] tumourName) {
        List<String> tumourLeafList = new ArrayList<>();
        TimeTreeNode[] allNodes = originalTree.getNodes().toArray(new TimeTreeNode[0]);
        // check which is a leaf
        for (int i = 0; i<allNodes.length; i++){
            // if the node is tumour node, and the node is a leaf, add to the list
            if (Arrays.asList(tumourName).contains(allNodes[i].getId()) && allNodes[i].isLeaf()){
                tumourLeafList.add(allNodes[i].getId());
            }
        }
        return tumourLeafList.toArray(new String[0]);
    }

    public static void getSampledTree(TimeTree newTree, String[] sampledNames) {
        List<String> sampledNamesList = Arrays.asList(sampledNames);
        TimeTreeNode rootNode = newTree.getRoot();
        List<TimeTreeNode> leafNodes = rootNode.getAllLeafNodes();
        for (TimeTreeNode node: leafNodes) {
            // get parent node for this leaf node
            TimeTreeNode parentNode = node.getParent();
            // if this node is not sampled
            if (!sampledNamesList.contains(node.getId())){
                // remove the non-sampled node
                parentNode.removeChild(node);
                // if only one child left and parent is not root
                if (parentNode.getChildCount() == 1 && !parentNode.isRoot()){
                    // if left child is removed, then set the right child's parent
                    TimeTreeNode grandparentNode = parentNode.getParent();
                    TimeTreeNode childNode = parentNode.getChild(0);
                    if (!Objects.equals(childNode.getBranchRate(), parentNode.getBranchRate())){
                        averageBranchRate(childNode, parentNode);
                    }
                    childNode.setParent(grandparentNode);
                    grandparentNode.removeChild(parentNode);
                    grandparentNode.addChild(childNode);
                } else if (parentNode.getChildCount() == 1 && parentNode.isRoot()) {
                    // if only one child left and parent is root
                    newTree.setRoot(parentNode.getChild(0), true);
                }
            }
        }

        // set the root anyway
        newTree.setRoot(newTree.getRoot(), true);

        // set the indices for all nodes
        TimeTreeNode[] allNodes = newTree.getNodes().toArray(new TimeTreeNode[0]);
        for (int i = 0; i<allNodes.length;i++){
            allNodes[i].setIndex(i);
        }
    }

    public static void averageBranchRate(TimeTreeNode childNode, TimeTreeNode parentNode) {
        // branchLength = branchDuration * branchRate
        double childRate = childNode.getBranchRate();
        double childDuration = childNode.getBranchDuration();
        double parentRate = parentNode.getBranchRate();
        double parentDuration = parentNode.getBranchDuration();
        // new rate = overall branchLength / overall branchDuration
        double newRate = (childRate*childDuration + parentRate*parentDuration) / (childDuration + parentDuration);
        childNode.setBranchRate(newRate);
    }

    public String[] getSampleResult(double fraction, String[] name) {
        // calculate the num of taxa names to get
        int sampleNumber = (int) Math.round(fraction * name.length);
        // create a list to write result in
        List<String> sampleResult = new ArrayList<>();
        while (sampleResult.size() < sampleNumber){
            int index;
            do{
                index = random.nextInt(name.length); // get a random index
            } while (sampleResult.contains(name[index])); // check the index is a new one

            sampleResult.add(name[index]); // add the name to the result list
        }
        return sampleResult.toArray(new String[0]);
    }

    public Value<TimeTree> getTree(){
        return getParams().get(treeParamName);
    }
    public Value<Double[]> getSampleFraction(){
        return getParams().get(sampleFractionParamName);
    }

    public Value<Object[][]> getTaxaName(){
        return getParams().get(taxaParamName);
    }
    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(treeParamName, tree);
            put(taxaParamName, taxaName);
            put(sampleFractionParamName, sampleFraction);
        }};
    }


    public void setParam(String paramName, Value value) {
        if (paramName.equals(sampleFractionParamName)) sampleFraction = value;
        else if (paramName.equals(treeParamName)) tree = value;
        else if (paramName.equals(taxaParamName)) taxaName = value;
        else super.setParam(paramName, value);
    }

}