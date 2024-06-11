package lphy.base.evolution.tree;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class LabelClade extends DeterministicFunction<TimeTree> {
    public static final String treeParamName = "tree";
    public static final String taxaParamName = "taxa";
    public static final String labelParamName = "label";
    public LabelClade(@ParameterInfo(name = treeParamName, description = "the tree to label")Value<TimeTree> tree,
                      @ParameterInfo(name = taxaParamName, description = "the root of the taxa names would be labelled") Value<String[]> taxa,
                      @ParameterInfo(name = labelParamName, description = "the label") Value<String> label){
        if (tree == null) throw new IllegalArgumentException("The tree cannot be null!");
        if (taxa == null) throw new IllegalArgumentException("The taxa name cannot be null!");
        if (label == null) throw new IllegalArgumentException("Please label the mrca of the taxa!");
        setParam(treeParamName, tree);
        setParam(taxaParamName, taxa);
        setParam(labelParamName, label);
    }
    @GeneratorInfo(name = "labelClade", description = "Find the most recent common ancestor of given taxa names in the tree and give it a label.")
    @Override
    public Value<TimeTree> apply() {
        // make a deep copy of the tree
        TimeTree tree = getTree().value();
        TimeTree newTree = new TimeTree(tree);

        // find mrca node
        Value<TimeTree> treeValue = new Value<>("newTree", newTree);
        MRCA mrcaInstance = new MRCA(treeValue, getTaxa());
        TimeTreeNode mrca = mrcaInstance.apply().value();

        // set label metadata
        String label = getLabel().value();
        mrca.setMetaData("label", label);

        return new Value<>(null, newTree,this);
    }
    public Value<TimeTree> getTree(){
       return getParams().get(treeParamName);
    }
    public Value<String[]> getTaxa(){
        return getParams().get(taxaParamName);
    }
    public Value<String> getLabel(){
        return getParams().get(labelParamName);
    }
}
