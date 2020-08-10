package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.evolution.branchratemodel.UCRelaxedClockModel;
import beast.evolution.likelihood.TreeLikelihood;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Tree;
import beast.math.distributions.Prior;
import consoperators.BigPulley;
import consoperators.InConstantDistanceOperator;
import consoperators.SimpleDistance;
import consoperators.SmallPulley;
import lphy.core.distributions.LogNormalMulti;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class PhyloCTMCToBEAST implements GeneratorToBEAST<PhyloCTMC> {

    public BEASTInterface generatorToBEAST(PhyloCTMC phyloCTMC, BEASTInterface value, BEASTContext context) {

        TreeLikelihood treeLikelihood = new TreeLikelihood();

        assert value instanceof beast.evolution.alignment.Alignment;
        beast.evolution.alignment.Alignment alignment = (beast.evolution.alignment.Alignment)value;
        treeLikelihood.setInputValue("data", alignment);

        Tree tree = (Tree) context.getBEASTObject(phyloCTMC.getTree());
        tree.setInputValue("taxa", value);
        tree.initAndValidate();

        treeLikelihood.setInputValue("tree", tree);

        Value<Double[]> branchRates = phyloCTMC.getBranchRates();

        if (branchRates != null) {

            if (branchRates.getGenerator() instanceof LogNormalMulti) {

                UCRelaxedClockModel relaxedClockModel = new UCRelaxedClockModel();

                Prior logNormalPrior = (Prior) context.getBEASTObject(branchRates.getGenerator());

                RealParameter beastBranchRates = (RealParameter) context.getBEASTObject(branchRates);

                relaxedClockModel.setInputValue("rates", beastBranchRates);
                relaxedClockModel.setInputValue("tree", tree);
                relaxedClockModel.setInputValue("distr", logNormalPrior.distInput.get());
                relaxedClockModel.initAndValidate();
                treeLikelihood.setInputValue("branchRateModel", relaxedClockModel);

                addRelaxedClockOperators(tree, relaxedClockModel, beastBranchRates, context);

            } else {
                throw new RuntimeException("Only lognormal relaxed clock model currently supported in BEAST2 conversion");
            }

        } else {
            StrictClockModel clockModel = new StrictClockModel();
            Value<Double> clockRate = phyloCTMC.getClockRate();
            if (clockRate != null) {
                clockModel.setInputValue("clock.rate", context.getBEASTObject(clockRate));
            } else {
                clockModel.setInputValue("clock.rate", BEASTContext.createRealParameter(1.0));
            }
            treeLikelihood.setInputValue("branchRateModel", clockModel);
        }

        Generator qGenerator = phyloCTMC.getQ().getGenerator();
        if (qGenerator == null) {
            throw new RuntimeException("BEAST2 does not support a fixed Q matrix.");
        } else {
            SubstitutionModel substitutionModel = (SubstitutionModel) context.getBEASTObject(qGenerator);

            SiteModel siteModel = new SiteModel();
            siteModel.setInputValue("substModel", substitutionModel);
            siteModel.initAndValidate();

            treeLikelihood.setInputValue("siteModel", siteModel);
        }

        treeLikelihood.initAndValidate();
        treeLikelihood.setID(alignment.getID() + ".treeLikelihood");

        return treeLikelihood;
    }

    private void addRelaxedClockOperators(Tree tree, UCRelaxedClockModel relaxedClockModel, RealParameter rates, BEASTContext context) {

        double tWindowSize = tree.getRoot().getHeight() / 10.0;

        InConstantDistanceOperator inConstantDistanceOperator = new InConstantDistanceOperator();
        inConstantDistanceOperator.setInputValue("clockModel", relaxedClockModel);
        inConstantDistanceOperator.setInputValue("tree", tree);
        inConstantDistanceOperator.setInputValue("rates", rates);
        inConstantDistanceOperator.setInputValue("twindowSize", tWindowSize);
        inConstantDistanceOperator.setInputValue("weight", BEASTContext.getOperatorWeight(tree.getNodeCount()));
        inConstantDistanceOperator.initAndValidate();
        context.addExtraOperator(inConstantDistanceOperator);

        SimpleDistance simpleDistance = new SimpleDistance();
        simpleDistance.setInputValue("clockModel", relaxedClockModel);
        simpleDistance.setInputValue("tree", tree);
        simpleDistance.setInputValue("rates", rates);
        simpleDistance.setInputValue("twindowSize", tWindowSize);
        simpleDistance.setInputValue("weight", BEASTContext.getOperatorWeight(2));
        simpleDistance.initAndValidate();
        context.addExtraOperator(simpleDistance);

        BigPulley bigPulley = new BigPulley();
        bigPulley.setInputValue("tree", tree);
        bigPulley.setInputValue("rates", rates);
        bigPulley.setInputValue("twindowSize", tWindowSize);
        bigPulley.setInputValue("dwindowSize", 0.1);
        bigPulley.setInputValue("weight", BEASTContext.getOperatorWeight(2));
        bigPulley.initAndValidate();
        context.addExtraOperator(bigPulley);

        SmallPulley smallPulley = new SmallPulley();
        smallPulley.setInputValue("clockModel", relaxedClockModel);
        smallPulley.setInputValue("tree", tree);
        smallPulley.setInputValue("rates", rates);
        smallPulley.setInputValue("dwindowSize", 0.1);
        smallPulley.setInputValue("weight", BEASTContext.getOperatorWeight(2));
        smallPulley.initAndValidate();
        context.addExtraOperator(smallPulley);
    }

    @Override
    public Class<PhyloCTMC> getGeneratorClass() {
        return PhyloCTMC.class;
    }

    @Override
    public Class<TreeLikelihood> getBEASTClass() {
        return TreeLikelihood.class;
    }
}
