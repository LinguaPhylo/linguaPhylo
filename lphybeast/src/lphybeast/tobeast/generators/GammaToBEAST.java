package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Prior;
import lphy.core.distributions.Gamma;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class GammaToBEAST implements GeneratorToBEAST<Gamma, Prior> {
    @Override
    public Prior generatorToBEAST(Gamma generator, BEASTInterface value, BEASTContext context) {
        beast.math.distributions.Gamma gammaDistribution = new beast.math.distributions.Gamma();
        gammaDistribution.setInputValue("shape", context.getBEASTObject(generator.getShape()));
        gammaDistribution.setInputValue("scale", context.getBEASTObject(generator.getScale()));
        gammaDistribution.initAndValidate();
        return BEASTContext.createPrior(gammaDistribution, (RealParameter) value);
    }

    @Override
    public Class<Gamma> getGeneratorClass() {
        return Gamma.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
