package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel;
import beast.math.distributions.Prior;
import lphy.core.distributions.BernoulliMulti;
import lphy.core.distributions.LogNormalMulti;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class BernoulliMultiToBEAST implements GeneratorToBEAST<BernoulliMulti, Prior> {
    @Override
    public Prior generatorToBEAST(BernoulliMulti generator, BEASTInterface value, BEASTContext context) {

        // TODO need to add a Bernoulli or Binomial prior to BEAST2 so this can be converted.

        return null;
    }

    @Override
    public Class<BernoulliMulti> getGeneratorClass() {
        return BernoulliMulti.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
