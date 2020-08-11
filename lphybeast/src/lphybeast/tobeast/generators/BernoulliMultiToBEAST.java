package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.math.distributions.BernoulliDistribution;
import lphy.core.distributions.BernoulliMulti;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class BernoulliMultiToBEAST implements GeneratorToBEAST<BernoulliMulti, BernoulliDistribution> {
    @Override
    public BernoulliDistribution generatorToBEAST(BernoulliMulti generator, BEASTInterface value, BEASTContext context) {

        BernoulliDistribution bernoulliDistribution = new BernoulliDistribution();
        bernoulliDistribution.setInputValue("p", context.getBEASTObject(generator.getP()));
        bernoulliDistribution.setInputValue("parameter", value);
        bernoulliDistribution.initAndValidate();
        return bernoulliDistribution;
    }

    @Override
    public Class<BernoulliMulti> getGeneratorClass() {
        return BernoulliMulti.class;
    }

    @Override
    public Class<BernoulliDistribution> getBEASTClass() {
        return BernoulliDistribution.class;
    }
}
