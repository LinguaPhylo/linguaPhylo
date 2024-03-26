package lphy.base.evolution.tree;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.evolution.EvolutionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;


public class RandomSample extends ParametricDistribution<TimeTree> {
    Value<TimeTree> tree;
    Value<String[]> taxaName;
    Value<Double[]> sampleFraction;
    // use the random generator in this class
    protected RandomGenerator random;

    public static final String taxaParamName = EvolutionConstants.taxaParamName;
    public static final String sampleFractionPara = "sampleFraction";
    public static final String treeParamName = "tree";

    public RandomSample(
            @ParameterInfo(name = treeParamName, narrativeName = "full tree", description = "the full tree to extract taxa from.") Value<TimeTree> tree,
            @ParameterInfo(name = taxaParamName, narrativeName = "taxa names", description = "the two taxa names that the function would sample") Value<String[]> taxaName,
            @ParameterInfo(name = sampleFractionPara, narrativeName = "fraction of sampling", description = "the two fractions that the function sample in the taxa") Value<Double[]> sampleFraction){
        if (tree == null) throw new IllegalArgumentException("The original tree cannot be null");
        setParam(treeParamName, tree);
        setParam(taxaParamName, taxaName);
        this.sampleFraction = sampleFraction;
        this.tree = tree;
        this.taxaName = taxaName;
        this.random = RandomUtils.getRandom();
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "RandomSample", description = "Generate the randomly sampled tree with given two sample fractions" +
            "and two clade taxa names within the given tree.")
    @Override
    public RandomVariable<TimeTree> sample() {
        Value<TimeTree> tree = getParams().get(treeParamName);
        Value<String[]> taxaName = getParams().get(taxaParamName);
        Value<Double[]> sampleFraction = getParams().get(sampleFractionPara);
        TimeTree originalTree = tree.value();

        // obtain tumour and normal taxa names
        String[] tumourName = new String[]{taxaName.value()[0]};
        String[] normalName = new String[]{taxaName.value()[1]};

        // get the leaf names
        String[] tumourLeafList = getLeafList(originalTree, tumourName);
        String[] normalLeafList = getLeafList(originalTree, normalName);

        // obtain tumour and normal sample fractions
        double tumourFraction = sampleFraction.value()[0];
        double normalFraction = sampleFraction.value()[1];

        // randomly pick the taxa names
        String[] sampledTumour = getSampleResult(tumourFraction, tumourLeafList);
        String[] sampledNormal = getSampleResult(normalFraction, normalLeafList);

        // merge the name arrays
        String[] sampledNames = combineTwoArray(sampledTumour, sampledNormal);
        List<String> sampledNamesList = Arrays.asList(sampledNames);

        // make a deep copy of original tree
        TimeTree newTree = new TimeTree(originalTree);

        // obtain all the taxa
        Taxa[] allTaxa = new Taxa[]{newTree.getTaxa()};

        // check each node
        getSampledTree(newTree, sampledNamesList);

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

    public static void getSampledTree(TimeTree newTree, List<String> sampledNamesList) {
        while (newTree.getTaxa().getTaxaNames().length != sampledNamesList.size()){
            for (int i = 0; i< newTree.getNodeCount(); i++){
                TimeTreeNode parentNode = newTree.getNodes().get(i);
                // only deal with nodes with two taxa
                if (parentNode.getChildCount() == 2){
                    // give each child taxa names
                    TimeTreeNode child1 = parentNode.getLeft();
                    TimeTreeNode child2 = parentNode.getRight();
                    // only deal with tips
                    removeTaxa(child1, sampledNamesList, parentNode, child2);
                    // reset the root if child1 is removed when parent node is origin
                    if (parentNode.isOrigin() && parentNode.getChildCount() == 1){
                        newTree.setRoot(child2);
                    }
                    removeTaxa(child2, sampledNamesList, parentNode, child1);
                    if (parentNode.isOrigin() && parentNode.getChildCount() == 1){
                        newTree.setRoot(child1);
                    }
                }
            }
        }
    }

    public static void removeTaxa(TimeTreeNode child1, List<String> sampledNamesList, TimeTreeNode parentNode, TimeTreeNode child2) {
        if (child1.isLeaf()){
            boolean nameExists = sampledNamesList.contains(child1.getId());
            // if the taxa is not what we want, then remove it and set sibling's parent nodes
            if (!nameExists && parentNode != null) {
                // remove the taxa
                parentNode.removeChild(child1);
                if (parentNode.getChildCount() == 1 && !parentNode.isOrigin()) {
                    // set sibling's parent node to grandparent node
                    TimeTreeNode tempParent = parentNode;
                    child2.setParent(parentNode.getParent());
                    parentNode.removeChild(tempParent);
                }
            }
        }
    }

    public static String[] combineTwoArray(String[] array1, String[] array2) {
        String[] sampledNames = new String[array1.length + array2.length];

        // do copying
        System.arraycopy(array1, 0, sampledNames, 0, array1.length);
        System.arraycopy(array2, 0, sampledNames, array1.length, array2.length);

        return sampledNames;
    }

    public String[] getSampleResult(double fraction, String[] name) {
        // calculate the num of taxa names to get
        int sampleNumber = (int)Math.round(fraction * name.length);
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

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (sampleFraction != null) map.put(String.valueOf(sampleFraction), sampleFraction);
        if (tree != null) map.put(treeParamName, tree);
        if (taxaName != null) map.put(taxaParamName, taxaName);
        return map;
    }
}
