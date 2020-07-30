package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;
import lphy.evolution.substitutionmodel.HKY;

public class HKYToBEAST implements GeneratorToBEAST<HKY> {
    @Override
    public BEASTInterface generatorToBEAST(HKY hky, BEASTInterface value, BEASTContext context) {

        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", context.getBEASTObject(hky.getKappa()));
        beastHKY.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(hky.getFreq())));
        beastHKY.initAndValidate();
        return beastHKY;
    }

    @Override
    public Class<HKY> getGeneratorClass() {
        return HKY.class;
    }
}
