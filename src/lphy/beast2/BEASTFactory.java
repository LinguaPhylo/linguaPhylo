package lphy.beast2;

import beast.core.parameter.RealParameter;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.evolution.branchratemodel.UCRelaxedClockModel;
import beast.evolution.datatype.Nucleotide;
import beast.evolution.likelihood.TreeLikelihood;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Tree;
import beast.math.distributions.LogNormalDistributionModel;
import lphy.TimeTree;
import lphy.core.PhyloCTMC;
import lphy.core.distributions.LogNormalMulti;
import lphy.core.functions.HKY;
import lphy.core.functions.JukesCantor;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BEASTFactory {

    public static TreeLikelihood createTreeLikelihood(RandomVariable<lphy.core.Alignment> lpAlignment) {

        if (lpAlignment.getGenerator() instanceof PhyloCTMC) {
            PhyloCTMC phyloCTMC = (PhyloCTMC)lpAlignment.getGenerator();
            return createTreeLikelihood(lpAlignment, phyloCTMC);
        } else {
            throw new IllegalArgumentException("Alignment Generator is not a PhyloCTMC");
        }
    }

    public static TreeLikelihood createTreeLikelihood(RandomVariable<lphy.core.Alignment> lpAlignment, PhyloCTMC phyloCTMC) {

        TreeLikelihood treeLikelihood = new TreeLikelihood();

        Tree tree = createBEASTTree(phyloCTMC.getParams().get("tree"));
        treeLikelihood.setInputValue("tree", tree);

        Alignment alignment = createBEASTAlignment(lpAlignment);
        treeLikelihood.setInputValue("data", alignment);

        BranchRateModel branchRateModel = createBEASTBranchRateModel(tree, phyloCTMC);
        treeLikelihood.setInputValue("branchRateModel", branchRateModel);

        SiteModel siteModel = createBEASTSiteModel(phyloCTMC);
        treeLikelihood.setInputValue("siteModel", siteModel);

        treeLikelihood.initAndValidate();

        return treeLikelihood;
    }

    public static SiteModel createBEASTSiteModel(PhyloCTMC phyloCTMC) {
        return createBEASTSiteModel(phyloCTMC.getQ());
    }

    public static SiteModel createBEASTSiteModel(Value<Double[][]> q) {
        Generator generator = q.getGenerator();

        SiteModel siteModel = new SiteModel();

        SubstitutionModel substModel = null;
        if (generator instanceof JukesCantor) {
            substModel = createBEASTJukesCantor((JukesCantor)generator);
        } else if (generator instanceof HKY) {
            substModel = createBEASTHKY((HKY)generator);
        } else {
            throw new IllegalArgumentException("Only HKY model supported currently");
        }
        siteModel.setInputValue("substModel", substModel);
        siteModel.initAndValidate();
        return siteModel;
    }

    private static SubstitutionModel createBEASTJukesCantor(JukesCantor generator) {
        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        return beastJC;
    }

    public static SubstitutionModel createBEASTHKY(HKY hky) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", hky.getKappa());
        beastHKY.setInputValue("frequencies", createBEASTFrequencies(hky.getFreq()));
        beastHKY.initAndValidate();
        return beastHKY;
    }

    public static Frequencies createBEASTFrequencies(Value<Double[]> freq) {
        Frequencies frequencies = new Frequencies();
        frequencies.setInputValue("frequencies", createBEASTRealParameter(freq));
        frequencies.initAndValidate();
        return frequencies;
    }

    public static BranchRateModel createBEASTBranchRateModel(Tree tree, PhyloCTMC phyloCTMC) {
        if (phyloCTMC.getSiteRates() != null) {
            Generator generator = phyloCTMC.getSiteRates().getGenerator();
            if (generator instanceof LogNormalMulti) {
                return createBEASTUCRelaxedClockModel(tree, createBEASTRealParameter(phyloCTMC.getSiteRates()), (LogNormalMulti)generator);
            } else {
                throw new IllegalArgumentException("Only lognormal relaxed clocks currently supported!");
            }
        } else {
            Value<Double> clockRate = phyloCTMC.getClockRate();
            if (clockRate == null) clockRate = new Value<Double>("mu", 1.0);
            return createBEASTStrictClock(createBEASTRealParameter(clockRate));
        }
    }

    public static BranchRateModel createBEASTStrictClock(RealParameter clockRate) {
        StrictClockModel clockModel = new StrictClockModel();
        clockModel.setInputValue("clock.rate", clockRate);
        clockModel.initAndValidate();
        return clockModel;
    }

    public static BranchRateModel createBEASTUCRelaxedClockModel(Tree tree, RealParameter siteRates, LogNormalMulti generator) {
        UCRelaxedClockModel relaxedClockModel = new UCRelaxedClockModel();
        relaxedClockModel.setInputValue("distr", createBEASTLogNormalDistributionModel(generator));
        relaxedClockModel.setInputValue("tree", tree);
        relaxedClockModel.setInputValue("rates", siteRates);
        relaxedClockModel.initAndValidate();
        return relaxedClockModel;
    }

    public static LogNormalDistributionModel createBEASTLogNormalDistributionModel(LogNormalMulti logNormalMulti) {
        LogNormalDistributionModel logNormalDistributionModel = new LogNormalDistributionModel();
        logNormalDistributionModel.setInputValue("M", createBEASTRealParameter(logNormalMulti.getParams().get("meanlog")));
        logNormalDistributionModel.setInputValue("S", createBEASTRealParameter(logNormalMulti.getParams().get("sdlog")));
        logNormalDistributionModel.initAndValidate();
        return logNormalDistributionModel;
    }

    public static RealParameter createBEASTRealParameter(Value value) {

        RealParameter parameter = new RealParameter();
        if (value.value() instanceof Double) {
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);
            parameter.initAndValidate();
        } else if (value.value() instanceof Double[]) {
            List<Double> values = Arrays.asList((Double[])value.value());
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.initAndValidate();
        } else {
            throw new IllegalArgumentException();
        }
        parameter.setID(value.getCanonicalId());
        return parameter;
    }


    public static Tree createBEASTTree(Value<TimeTree> timeTree) {

        Tree tree = new Tree(timeTree.value().toString());
        tree.setID(timeTree.getCanonicalId());
        return tree;
    }

    public static Alignment createBEASTAlignment(RandomVariable<lphy.core.Alignment> lpAlignment) {

        List<Sequence> sequences = new ArrayList<>();

        lphy.core.Alignment alignment = lpAlignment.value();

        String[] taxaNames = alignment.getTaxaNames();

        for (int i = 0; i < alignment.getTaxonCount(); i++) {
            sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
        }

        Alignment beastAlignment = new Alignment(sequences, "nucleotide");
        beastAlignment.setID(lpAlignment.getCanonicalId());

        return beastAlignment;
    }

    public static Sequence createBEASTSequence(String taxon, String sequence) {
        return new Sequence(taxon, sequence);
    }
}
