package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import lphy.evolution.substitutionmodel.JukesCantor;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class JukesCantorToBEAST implements
        GeneratorToBEAST<JukesCantor, beast.evolution.substitutionmodel.JukesCantor> {
    @Override
    public beast.evolution.substitutionmodel.JukesCantor generatorToBEAST(JukesCantor jukesCantor, BEASTInterface value, BEASTContext context) {


        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        return beastJC;
    }

    @Override
    public Class<JukesCantor> getGeneratorClass() {
        return JukesCantor.class;
    }

    @Override
    public Class<beast.evolution.substitutionmodel.JukesCantor> getBEASTClass() {
        return beast.evolution.substitutionmodel.JukesCantor.class;
    }
}
