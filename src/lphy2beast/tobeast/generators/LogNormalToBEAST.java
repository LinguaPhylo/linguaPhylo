package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel;
import lphy.core.distributions.Gamma;
import lphy.core.distributions.LogNormal;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;

public class LogNormalToBEAST implements GeneratorToBEAST<LogNormal> {
    @Override
    public BEASTInterface generatorToBEAST(LogNormal generator, BEASTInterface value, BEASTContext context) {
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
}
