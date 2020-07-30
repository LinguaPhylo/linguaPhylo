package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel;
import lphy.core.distributions.LogNormal;
import lphy.core.distributions.LogNormalMulti;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;

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
}
