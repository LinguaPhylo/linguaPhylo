package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.tree.coalescent.ConstantPopulation;
import beast.evolution.tree.coalescent.TreeIntervals;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;
import lphy.evolution.coalescent.Coalescent;

public class CoalescentToBEAST implements GeneratorToBEAST<Coalescent> {
    @Override
    public BEASTInterface generatorToBEAST(Coalescent coalescent, BEASTInterface value, BEASTContext context) {

            beast.evolution.tree.coalescent.Coalescent beastCoalescent = new beast.evolution.tree.coalescent.Coalescent();

            TreeIntervals treeIntervals = new TreeIntervals();
            treeIntervals.setInputValue("tree", value);
            treeIntervals.initAndValidate();

            beastCoalescent.setInputValue("treeIntervals", treeIntervals);

            ConstantPopulation populationFunction = new ConstantPopulation();
            populationFunction.setInputValue("popSize", context.getBEASTObject(coalescent.getTheta()));
            populationFunction.initAndValidate();

            beastCoalescent.setInputValue("populationModel", populationFunction);

            beastCoalescent.initAndValidate();

            return beastCoalescent;
        }

    @Override
    public Class<Coalescent> getGeneratorClass() {
        return Coalescent.class;
    }
}
