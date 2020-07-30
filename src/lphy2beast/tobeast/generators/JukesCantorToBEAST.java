package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;
import lphy.evolution.substitutionmodel.JukesCantor;

public class JukesCantorToBEAST implements GeneratorToBEAST<JukesCantor> {
    @Override
    public BEASTInterface generatorToBEAST(JukesCantor jukesCantor, BEASTInterface value, BEASTContext context) {


        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        return beastJC;
    }

    @Override
    public Class<JukesCantor> getGeneratorClass() {
        return JukesCantor.class;
    }
}
