package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.tree.TreeStatLogger;
import lphy.core.functions.TreeLength;
import lphy.evolution.substitutionmodel.K80;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class TreeLengthToBEAST implements GeneratorToBEAST<TreeLength, TreeStatLogger> {

    public TreeStatLogger generatorToBEAST(TreeLength treeLength, BEASTInterface tree, BEASTContext context) {

        TreeStatLogger treeStatLogger = new TreeStatLogger();
        treeStatLogger.setInputValue("logLength", true);
        treeStatLogger.setInputValue("logHeight", false);
        treeStatLogger.setInputValue("tree", tree);
        treeStatLogger.initAndValidate();

        context.addExtraLogger(treeStatLogger);

        return treeStatLogger;
    }

    @Override
    public Class<TreeLength> getGeneratorClass() {
        return TreeLength.class;
    }

    @Override
    public Class<TreeStatLogger> getBEASTClass() {
        return TreeStatLogger.class;
    }
}
