package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel;
import beast.math.distributions.Prior;
import lphy.core.distributions.LogNormalMulti;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class LogNormalMultiToBEAST implements GeneratorToBEAST<LogNormalMulti> {
    @Override
    public BEASTInterface generatorToBEAST(LogNormalMulti generator, BEASTInterface value, BEASTContext context) {
        LogNormalDistributionModel logNormalDistributionModel = new LogNormalDistributionModel();
        logNormalDistributionModel.setInputValue("M", context.getBEASTObject(generator.getMeanLog()));
        logNormalDistributionModel.setInputValue("S", context.getBEASTObject(generator.getSDLog()));
        logNormalDistributionModel.initAndValidate();

        return BEASTContext.createPrior(logNormalDistributionModel, (RealParameter) value);
    }

    @Override
    public Class<LogNormalMulti> getGeneratorClass() {
        return LogNormalMulti.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
