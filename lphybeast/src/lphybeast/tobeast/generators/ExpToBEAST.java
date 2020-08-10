package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Exponential;
import beast.math.distributions.Prior;
import lphy.core.distributions.Exp;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class ExpToBEAST implements GeneratorToBEAST<Exp> {
    @Override
    public BEASTInterface generatorToBEAST(Exp generator, BEASTInterface value, BEASTContext context) {
        Exponential exponential = new Exponential();
        exponential.setInputValue("mean", context.getBEASTObject(generator.getParams().get("mean")));
        exponential.initAndValidate();
        return BEASTContext.createPrior(exponential, (RealParameter) value);
    }

    @Override
    public Class<Exp> getGeneratorClass() {
        return Exp.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
