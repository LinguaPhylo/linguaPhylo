package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel;
import beast.math.distributions.Prior;
import lphy.core.distributions.LogNormal;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class LogNormalToBEAST implements GeneratorToBEAST<LogNormal, Prior> {
    @Override
    public Prior generatorToBEAST(LogNormal generator, BEASTInterface value, BEASTContext context) {
        LogNormalDistributionModel logNormalDistributionModel = new LogNormalDistributionModel();
        logNormalDistributionModel.setInputValue("M", context.getBEASTObject(generator.getMeanLog()));
        logNormalDistributionModel.setInputValue("S", context.getBEASTObject(generator.getSDLog()));
        logNormalDistributionModel.initAndValidate();

        return BEASTContext.createPrior(logNormalDistributionModel, (RealParameter) value);
    }

    @Override
    public Class<LogNormal> getGeneratorClass() {
        return LogNormal.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
