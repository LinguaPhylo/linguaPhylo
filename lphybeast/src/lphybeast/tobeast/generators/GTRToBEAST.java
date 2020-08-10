package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.evolution.substitutionmodel.GTR;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class GTRToBEAST implements GeneratorToBEAST<GTR, substmodels.nucleotide.GTR> {
    @Override
    public substmodels.nucleotide.GTR generatorToBEAST(GTR gtr, BEASTInterface value, BEASTContext context) {

        substmodels.nucleotide.GTR beastGTR = new substmodels.nucleotide.GTR();

        Value<Double[]> rates = gtr.getRates();

        RealParameter ratesParameter = (RealParameter)context.getBEASTObject(rates);
        ratesParameter.setInputValue("keys", "AC AG AT CG CT GT");
        ratesParameter.initAndValidate();

        beastGTR.setInputValue("rates", ratesParameter);
        beastGTR.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(gtr.getFreq()), "A C G T"));
        beastGTR.initAndValidate();
        return beastGTR;
    }

    @Override
    public Class<GTR> getGeneratorClass() { return GTR.class; }

    @Override
    public Class<substmodels.nucleotide.GTR> getBEASTClass() {
        return substmodels.nucleotide.GTR.class;
    }
}
