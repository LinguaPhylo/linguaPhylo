package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.math.distributions.MarkovChainDistribution;
import lphy.core.distributions.ExpMarkovChain;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class ExpMarkovChainToBEAST implements GeneratorToBEAST<ExpMarkovChain> {
    @Override
    public BEASTInterface generatorToBEAST(ExpMarkovChain generator, BEASTInterface value, BEASTContext context) {

        MarkovChainDistribution mcd = new MarkovChainDistribution();
        mcd.setInputValue("shape", 1.0);
        mcd.setInputValue("parameter", value);
        mcd.setInputValue("initialMean", context.getBEASTObject(generator.getInitialMean()));
        mcd.initAndValidate();

//        Value<Double> initialMean = generator.getInitialMean();
//        GenerativeDistribution initialMeanGenerator = (GenerativeDistribution)initialMean.getGenerator();
//
//        // replace prior on initialMean with excludable prior on the first element of value
//        beast.math.distributions.Prior prior = (beast.math.distributions.Prior)context.getBEASTObject(initialMeanGenerator);
//
//        ExcludablePrior excludablePrior = new ExcludablePrior();
//        BooleanParameter include = new BooleanParameter();
//        List<Boolean> includeList = new ArrayList<>();
//        int n = generator.getN().value();
//        includeList.add(true);
//        for (int i = 1; i < n; i++) {
//            includeList.add(false);
//        }
//        include.setInputValue("value", includeList);
//        include.setInputValue("dimension", n);
//        include.initAndValidate();
//        excludablePrior.setInputValue("xInclude", include);
//        excludablePrior.setInputValue("x", value);
//        excludablePrior.setInputValue("distr",prior.distInput.get());
//        excludablePrior.initAndValidate();
//
//        context.putBEASTObject(initialMeanGenerator, excludablePrior);

        return mcd;
    }

    @Override
    public Class<ExpMarkovChain> getGeneratorClass() {
        return ExpMarkovChain.class;
    }

    @Override
    public Class<MarkovChainDistribution> getBEASTClass() {
        return MarkovChainDistribution.class;
    }
}
