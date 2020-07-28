package lphy.beast.tobeast;

import beast.core.BEASTInterface;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.TraitSet;
import beast.util.TreeParser;
import lphy.beast.ValueToBEAST;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeTreeToBEAST implements ValueToBEAST<TimeTree> {

    @Override
    public BEASTInterface valueToBEAST(Value<TimeTree> timeTreeValue, Map<GraphicalModelNode, BEASTInterface> beastObjects) {

        TimeTree timeTree = timeTreeValue.value();
        
        TreeParser tree = new TreeParser();
        tree.setInputValue("newick", timeTree.toString());
        tree.setInputValue("IsLabelledNewick", true);

        TaxonSet taxa = new TaxonSet();
        List<Taxon> taxonList = createTaxonList(timeTree);
        taxa.setInputValue("taxon", taxonList);
        taxa.initAndValidate();
        tree.setInputValue("taxonset", taxa);

        if (!timeTreeValue.value().isUlrametric()) {

            TraitSet traitSet = new TraitSet();
            traitSet.setInputValue("traitname", TraitSet.AGE_TRAIT);
            traitSet.setInputValue("value",createAgeTraitString(timeTree));
            traitSet.setInputValue("taxa", taxa);
            traitSet.initAndValidate();

            tree.setInputValue("trait", traitSet);
        }

        tree.initAndValidate();
        tree.setID(timeTreeValue.getCanonicalId());
        return tree;
    }

    private List<Taxon> createTaxonList(TimeTree timeTree) {
        List<Taxon> taxonList = new ArrayList<>();
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (node.isLeaf()) {
                taxonList.add(new Taxon(node.getId()));
            }
        }
        return taxonList;
    }

    private String createAgeTraitString(TimeTree tree) {

        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (TimeTreeNode node : tree.getNodes()) {
            if (node.isLeaf()) {
                if (count > 0) builder.append(",\n");
                builder.append(node.getId());
                builder.append("=");
                builder.append(node.getAge());
                count += 1;
            }
        }
        return builder.toString();
    }

    public Class getValueClass() {
        return TimeTree.class;
    }
}
