package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.TraitSet;
import beast.mascot.distribution.StructuredTreeIntervals;
import beast.mascot.dynamics.Constant;
import lphy.core.functions.MigrationMatrix;
import lphy.evolution.coalescent.StructuredCoalescent;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;
import lphybeast.tobeast.values.TimeTreeToBEAST;

import java.util.List;

public class StructuredCoalescentToBEAST implements GeneratorToBEAST<StructuredCoalescent> {

    @Override
    public BEASTInterface generatorToBEAST(StructuredCoalescent coalescent, BEASTInterface value, BEASTContext context) {

        beast.mascot.distribution.Mascot mascot = new beast.mascot.distribution.Mascot();

        Value<Double[][]> M = coalescent.getM();

        if (M.getGenerator() instanceof MigrationMatrix) {
            Value<Double[]> NeValue = ((MigrationMatrix) M.getGenerator()).getTheta();
            Value<Double[]> backwardsMigrationRates = ((MigrationMatrix) M.getGenerator()).getMigrationRates();

            Constant dynamics = new Constant();
            dynamics.setInputValue("Ne", context.getBEASTObject(NeValue));
            dynamics.setInputValue("backwardsMigration", context.getBEASTObject(backwardsMigrationRates));
            dynamics.setInputValue("dimension", NeValue.value().length);

            String popLabel = coalescent.getPopulationLabel();

            TimeTree timeTree = ((Value<TimeTree>)context.getGraphicalModelNode(value)).value();

            TraitSet traitSet = new TraitSet();
            traitSet.setInputValue("traitname", popLabel);
            traitSet.setInputValue("value", createTraitString(timeTree, popLabel));

            TaxonSet taxa = new TaxonSet();
            List<Taxon> taxonList = context.createTaxonList(TimeTreeToBEAST.getTaxaNames(timeTree));
            taxa.setInputValue("taxon", taxonList);
            taxa.initAndValidate();

            traitSet.setInputValue("taxa", taxa);
            traitSet.initAndValidate();

            dynamics.setInputValue("typeTrait", traitSet);
            dynamics.initAndValidate();

            mascot.setInputValue("dynamics", dynamics);

            StructuredTreeIntervals structuredTreeIntervals = new StructuredTreeIntervals();
            structuredTreeIntervals.setInputValue("tree", value);
            structuredTreeIntervals.initAndValidate();

            mascot.setInputValue("structuredTreeIntervals", structuredTreeIntervals);
            mascot.setInputValue("tree", value);

            mascot.initAndValidate();

            return mascot;
        }
        throw new RuntimeException("Can't convert StructuredCoalescent unless MigrationMatrix function is used to form M matrix");
    }

    private String createTraitString(TimeTree tree, String traitName) {
        StringBuilder builder = new StringBuilder();
        int leafCount = 0;
        for (TimeTreeNode node : tree.getNodes()) {
            if (node.isLeaf()) {
                if (leafCount > 0) builder.append(", ");
                builder.append(node.getId());
                builder.append("=");
                builder.append(node.getMetaData(traitName));
                leafCount += 1;
            }
        }
        return builder.toString();
    }

    @Override
    public Class<StructuredCoalescent> getGeneratorClass() {
        return StructuredCoalescent.class;
    }
}
