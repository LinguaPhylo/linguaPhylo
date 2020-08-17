package lphybeast.tobeast.values;

import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.TraitSet;
import beast.util.TreeParser;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;
import lphybeast.tobeast.data.DataExchanger;

import java.util.ArrayList;
import java.util.List;

public class TimeTreeToBEAST implements ValueToBEAST<TimeTree, TreeParser> {

    protected DataExchanger dataExchanger;

    /**
     * Call this to use simulated alignment {@link Alignment}.
     */
    public TimeTreeToBEAST() { }

    /**
     * Call this to use given (real data) alignment.
     * @param dataExchanger using {@link beast.evolution.alignment.Alignment} (real data)
     */
    public TimeTreeToBEAST(DataExchanger dataExchanger) { this.dataExchanger = dataExchanger; }

    @Override
    public TreeParser valueToBEAST(Value<TimeTree> timeTreeValue, BEASTContext context) {

        TimeTree timeTree = timeTreeValue.value();

        //TODO replace taxa name wisely
        if (dataExchanger != null) {
            dataExchanger.replaceTaxaNamesByOrder(timeTree);
        }
        
        TreeParser tree = new TreeParser();
        tree.setInputValue("newick", timeTree.toNewick(false));
        tree.setInputValue("IsLabelledNewick", true);

        TaxonSet taxa = new TaxonSet();
        List<Taxon> taxonList = context.createTaxonList(getTaxaNames(timeTree));
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
        tree.setRoot(tree.parseNewick(tree.newickInput.get()));
        if (!timeTreeValue.isAnonymous()) tree.setID(timeTreeValue.getCanonicalId());
        return tree;
    }

    public static List<String> getTaxaNames(TimeTree timeTree) {
        List<String> taxaNames = new ArrayList<>();
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (node.isLeaf()) {
                taxaNames.add(node.getId());
            }
        }
        return taxaNames;
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

    @Override
    public Class getValueClass() {
        return TimeTree.class;
    }

    @Override
    public Class<TreeParser> getBEASTClass() {
        return TreeParser.class;
    }
}
