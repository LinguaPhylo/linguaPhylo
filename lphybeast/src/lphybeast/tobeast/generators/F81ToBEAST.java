package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;
import lphy.evolution.substitutionmodel.F81;

public class F81ToBEAST implements GeneratorToBEAST<F81> {
    @Override
    public BEASTInterface generatorToBEAST(F81 f81, BEASTInterface value, BEASTContext context) {

        beast.evolution.substitutionmodel.HKY beastF81 = new beast.evolution.substitutionmodel.HKY();
        beastF81.setInputValue("kappa", new RealParameter("1.0"));
        beastF81.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(f81.getFreq())));
        beastF81.initAndValidate();
        return beastF81;
    }

    @Override
    public Class<F81> getGeneratorClass() { return F81.class; }
}
