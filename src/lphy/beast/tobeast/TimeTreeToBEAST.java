package lphy.beast.tobeast;

import beast.core.BEASTInterface;
import beast.evolution.alignment.Sequence;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import lphy.beast.ValueToBEAST;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeTreeToBEAST implements ValueToBEAST<TimeTree> {

    @Override
    public BEASTInterface valueToBEAST(Value<TimeTree> timeTreeValue, Map<GraphicalModelNode, BEASTInterface> beastObjects) {

        TreeParser tree = new TreeParser();
        tree.setInputValue("newick", timeTreeValue.value().toString());
        tree.setInputValue("IsLabelledNewick", true);

        tree.initAndValidate();
        tree.setID(timeTreeValue.getCanonicalId());
        return tree;
    }

    public Class getValueClass() {
        return TimeTree.class;
    }
}
