package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Prior;
import lphy.core.distributions.Beta;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class BetaToBEAST implements GeneratorToBEAST<Beta> {
    @Override
    public BEASTInterface generatorToBEAST(Beta generator, BEASTInterface value, BEASTContext context) {
        beast.math.distributions.Beta betaDistribution = new beast.math.distributions.Beta();
        betaDistribution.setInputValue("alpha", context.getBEASTObject(generator.getParams().get("alpha")));
        betaDistribution.setInputValue("beta", context.getBEASTObject(generator.getParams().get("beta")));
        betaDistribution.initAndValidate();
        return BEASTContext.createPrior(betaDistribution, (RealParameter) value);
    }

    @Override
    public Class<Beta> getGeneratorClass() {
        return Beta.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
