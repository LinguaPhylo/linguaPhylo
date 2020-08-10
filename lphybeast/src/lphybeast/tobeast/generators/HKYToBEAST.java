package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.evolution.substitutionmodel.HKY;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class HKYToBEAST implements GeneratorToBEAST<HKY, beast.evolution.substitutionmodel.HKY> {
    @Override
    public beast.evolution.substitutionmodel.HKY generatorToBEAST(HKY hky, BEASTInterface value, BEASTContext context) {

        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", context.getBEASTObject(hky.getKappa()));
        beastHKY.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(hky.getFreq()),"A C G T"));
        beastHKY.initAndValidate();
        return beastHKY;
    }

    @Override
    public Class<HKY> getGeneratorClass() {
        return HKY.class;
    }

    @Override
    public Class<beast.evolution.substitutionmodel.HKY> getBEASTClass() {
        return beast.evolution.substitutionmodel.HKY.class;
    }
}
