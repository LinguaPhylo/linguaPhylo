package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;
import lphy.evolution.substitutionmodel.K80;

public class K80ToBEAST implements GeneratorToBEAST<K80> {
    @Override
    public BEASTInterface generatorToBEAST(K80 k80, BEASTInterface value, BEASTContext context) {

        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", context.getBEASTObject(k80.getKappa()));
        beastHKY.setInputValue("frequencies", BEASTContext.createBEASTFrequencies(BEASTContext.createRealParameter(new Double[]{0.25, 0.25, 0.25, 0.25}),"A C G T"));
        beastHKY.initAndValidate();
        return beastHKY;
    }

    @Override
    public Class<K80> getGeneratorClass() {
        return K80.class;
    }
}
