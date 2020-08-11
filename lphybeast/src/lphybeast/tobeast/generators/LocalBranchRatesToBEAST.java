package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.branchratemodel.RandomLocalClockModel;
import lphy.evolution.branchrates.LocalBranchRates;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class LocalBranchRatesToBEAST implements GeneratorToBEAST<LocalBranchRates, RandomLocalClockModel> {
    @Override
    public RandomLocalClockModel generatorToBEAST(LocalBranchRates localBranchRates, BEASTInterface value, BEASTContext context) {

        RandomLocalClockModel randomLocalClockModel = new RandomLocalClockModel();
        randomLocalClockModel.setInputValue("tree", context.getBEASTObject(localBranchRates.getTree()));
        randomLocalClockModel.setInputValue("indicators", context.getBEASTObject(localBranchRates.getIndicators()));
        randomLocalClockModel.setInputValue("rates", context.getBEASTObject(localBranchRates.getRates()));
        randomLocalClockModel.initAndValidate();
        return randomLocalClockModel;
    }

    @Override
    public Class<LocalBranchRates> getGeneratorClass() { return LocalBranchRates.class; }

    @Override
    public Class<RandomLocalClockModel> getBEASTClass() {
        return RandomLocalClockModel.class;
    }
}
