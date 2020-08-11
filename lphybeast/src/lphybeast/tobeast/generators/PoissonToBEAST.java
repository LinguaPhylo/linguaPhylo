package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.Parameter;
import beast.math.distributions.Prior;
import lphy.core.distributions.Poisson;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class PoissonToBEAST implements GeneratorToBEAST<Poisson, Prior> {
    @Override
    public Prior generatorToBEAST(Poisson generator, BEASTInterface value, BEASTContext context) {

        beast.math.distributions.Poisson poisson = new beast.math.distributions.Poisson();
        poisson.setInputValue("lambda", context.getBEASTObject(generator.getLambda()));
        poisson.initAndValidate();

        return BEASTContext.createPrior(poisson, (Parameter) value);
    }

    @Override
    public Class<Poisson> getGeneratorClass() {
        return Poisson.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
