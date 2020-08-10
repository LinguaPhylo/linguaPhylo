package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.evolution.substitutionmodel.F81;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class F81ToBEAST implements GeneratorToBEAST<F81, beast.evolution.substitutionmodel.HKY> {
    @Override
    public beast.evolution.substitutionmodel.HKY generatorToBEAST(F81 f81, BEASTInterface value, BEASTContext context) {

        beast.evolution.substitutionmodel.HKY beastF81 = new beast.evolution.substitutionmodel.HKY();
        beastF81.setInputValue("kappa", new RealParameter("1.0"));
        beastF81.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(f81.getFreq()),"A C G T"));
        beastF81.initAndValidate();
        return beastF81;
    }

    @Override
    public Class<F81> getGeneratorClass() { return F81.class; }

    @Override
    public Class<beast.evolution.substitutionmodel.HKY> getBEASTClass() {
        return beast.evolution.substitutionmodel.HKY.class;
    }
}
