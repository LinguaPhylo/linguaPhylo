package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Prior;
import lphy.core.distributions.NormalMulti;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class NormalMultiToBEAST implements GeneratorToBEAST<NormalMulti, Prior> {
    @Override
    public Prior generatorToBEAST(NormalMulti generator, BEASTInterface value, BEASTContext context) {
        beast.math.distributions.Normal normal = new beast.math.distributions.Normal();
        normal.setInputValue("mean", context.getBEASTObject(generator.getMean()));
        normal.setInputValue("sigma", context.getBEASTObject(generator.getSd()));
        normal.initAndValidate();

        return BEASTContext.createPrior(normal, (RealParameter)value);
    }

    @Override
    public Class<NormalMulti> getGeneratorClass() {
        return NormalMulti.class;
    }

    @Override
    public Class<Prior> getBEASTClass() {
        return Prior.class;
    }
}
