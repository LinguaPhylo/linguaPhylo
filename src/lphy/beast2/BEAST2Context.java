package lphy.beast2;

import beast.core.*;
import beast.core.parameter.RealParameter;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.evolution.branchratemodel.UCRelaxedClockModel;
import beast.evolution.datatype.Nucleotide;
import beast.evolution.likelihood.TreeLikelihood;
import beast.evolution.operators.ScaleOperator;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Tree;
import beast.math.distributions.LogNormalDistributionModel;
import beast.util.TreeParser;
import beast.util.XMLProducer;
import lphy.TimeTree;
import lphy.core.PhyloCTMC;
import lphy.core.distributions.LogNormalMulti;
import lphy.core.functions.HKY;
import lphy.core.functions.JukesCantor;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;

import java.util.*;

public class BEAST2Context {

    List<StateNode> state = new ArrayList<>();

    List<BEASTInterface> elements = new ArrayList<>();

    public TreeLikelihood createTreeLikelihood(RandomVariable<lphy.core.Alignment> lpAlignment) {

        if (lpAlignment.getGenerator() instanceof PhyloCTMC) {
            PhyloCTMC phyloCTMC = (PhyloCTMC)lpAlignment.getGenerator();
            return createTreeLikelihood(lpAlignment, phyloCTMC);
        } else {
            throw new IllegalArgumentException("Alignment Generator is not a PhyloCTMC");
        }
    }

    public TreeLikelihood createTreeLikelihood(RandomVariable<lphy.core.Alignment> lpAlignment, PhyloCTMC phyloCTMC) {

        TreeLikelihood treeLikelihood = new TreeLikelihood();

        Alignment alignment = createBEASTAlignment(lpAlignment);
        treeLikelihood.setInputValue("data", alignment);

        Tree tree = createBEASTTree(phyloCTMC.getParams().get("tree"), alignment);
        treeLikelihood.setInputValue("tree", tree);

        BranchRateModel branchRateModel = createBEASTBranchRateModel(tree, phyloCTMC);
        treeLikelihood.setInputValue("branchRateModel", branchRateModel);

        SiteModel siteModel = createBEASTSiteModel(phyloCTMC);
        treeLikelihood.setInputValue("siteModel", siteModel);

        treeLikelihood.initAndValidate();
        elements.add(treeLikelihood);

        return treeLikelihood;
    }

    public SiteModel createBEASTSiteModel(PhyloCTMC phyloCTMC) {
        return createBEASTSiteModel(phyloCTMC.getQ());
    }

    public SiteModel createBEASTSiteModel(Value<Double[][]> q) {
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
        elements.add(siteModel);
        return siteModel;
    }

    private SubstitutionModel createBEASTJukesCantor(JukesCantor generator) {
        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        elements.add(beastJC);
        return beastJC;
    }

    public SubstitutionModel createBEASTHKY(HKY hky) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", hky.getKappa());
        beastHKY.setInputValue("frequencies", createBEASTFrequencies(hky.getFreq()));
        beastHKY.initAndValidate();
        elements.add(beastHKY);
        return beastHKY;
    }

    public Frequencies createBEASTFrequencies(Value<Double[]> freq) {
        Frequencies frequencies = new Frequencies();
        frequencies.setInputValue("frequencies", createBEASTRealParameter(freq));
        frequencies.initAndValidate();
        elements.add(frequencies);
        return frequencies;
    }

    public BranchRateModel createBEASTBranchRateModel(Tree tree, PhyloCTMC phyloCTMC) {
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

    public BranchRateModel createBEASTStrictClock(RealParameter clockRate) {
        StrictClockModel clockModel = new StrictClockModel();
        clockModel.setInputValue("clock.rate", clockRate);
        clockModel.initAndValidate();
        elements.add(clockModel);
        return clockModel;
    }

    public BranchRateModel createBEASTUCRelaxedClockModel(Tree tree, RealParameter siteRates, LogNormalMulti generator) {
        UCRelaxedClockModel relaxedClockModel = new UCRelaxedClockModel();
        relaxedClockModel.setInputValue("distr", createBEASTLogNormalDistributionModel(generator));
        relaxedClockModel.setInputValue("tree", tree);
        relaxedClockModel.setInputValue("rates", siteRates);
        relaxedClockModel.initAndValidate();
        elements.add(relaxedClockModel);
        return relaxedClockModel;
    }

    public LogNormalDistributionModel createBEASTLogNormalDistributionModel(LogNormalMulti logNormalMulti) {
        LogNormalDistributionModel logNormalDistributionModel = new LogNormalDistributionModel();
        logNormalDistributionModel.setInputValue("M", createBEASTRealParameter(logNormalMulti.getParams().get("meanlog")));
        logNormalDistributionModel.setInputValue("S", createBEASTRealParameter(logNormalMulti.getParams().get("sdlog")));
        logNormalDistributionModel.initAndValidate();
        elements.add(logNormalDistributionModel);
        return logNormalDistributionModel;
    }

    public RealParameter createBEASTRealParameter(Value value) {

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
        elements.add(parameter);

        if (value instanceof RandomVariable) {
            state.add(parameter);
        }
        return parameter;
    }


    public Tree createBEASTTree(Value<TimeTree> timeTree, Alignment alignment) {

        TreeParser tree = new TreeParser();
        tree.setInputValue("newick", timeTree.value().toString());
        tree.setInputValue("IsLabelledNewick", true);
        tree.setInputValue("taxa", alignment);

        tree.initAndValidate();
        tree.setID(timeTree.getCanonicalId());
        state.add(tree);
        elements.add(tree);

        return tree;
    }

    public Alignment createBEASTAlignment(RandomVariable<lphy.core.Alignment> lpAlignment) {

        List<Sequence> sequences = new ArrayList<>();

        lphy.core.Alignment alignment = lpAlignment.value();

        String[] taxaNames = alignment.getTaxaNames();

        for (int i = 0; i < alignment.getTaxonCount(); i++) {
            sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
        }

        Alignment beastAlignment = new Alignment(sequences, "nucleotide");
        beastAlignment.setID(lpAlignment.getCanonicalId());
        elements.add(beastAlignment);

        return beastAlignment;
    }

    public Sequence createBEASTSequence(String taxon, String sequence) {
        Sequence seq = new Sequence(taxon, sequence);

        elements.add(seq);

        return seq;
    }

    public OperatorSchedule createOperatorSchedule() {

        OperatorSchedule schedule = new OperatorSchedule();
        schedule.setInputValue("operator", createOperators());
        schedule.initAndValidate();
        elements.add(schedule);

        return schedule;
    }

    public List<Operator> createOperators() {

        List<Operator> operators = new ArrayList<>();

        for (StateNode  stateNode : state) {
            System.out.println("State node" + stateNode);
            if (stateNode instanceof RealParameter) {
                operators.add(createBEASTOperator((RealParameter)stateNode));
            } else if (stateNode instanceof Tree) {
                operators.add(createTreeScaleOperator((Tree)stateNode));
            }
        }

        return operators;
    }

    private List<Logger> createLoggers(int logEvery) {
        List<Logger> loggers = new ArrayList<>();

        loggers.add(createScreenLogger(logEvery));

        return loggers;
    }

    private Logger createScreenLogger(int logEvery) {
        Logger logger = new Logger();
        logger.setInputValue("logEvery", logEvery);
        logger.setInputValue("log", state);
        logger.initAndValidate();
        elements.add(logger);
        return logger;
    }

    private Operator createTreeScaleOperator(Tree tree) {
        ScaleOperator operator = new ScaleOperator();
        operator.setInputValue("tree", tree);
        operator.setInputValue("weight", 1.0);
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    private Operator createBEASTOperator(RealParameter parameter) {
        ScaleOperator operator = new ScaleOperator();
        operator.setInputValue("parameter", parameter);
        operator.setInputValue("weight", 1.0);
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    public MCMC createMCMC(long chainLength, int logEvery, RandomVariable<lphy.core.Alignment> alignment) {
        clear();

        TreeLikelihood treeLikelihood = createTreeLikelihood(alignment);

        MCMC mcmc = new MCMC();
        mcmc.setInputValue("distribution", treeLikelihood);
        mcmc.setInputValue("chainLength", chainLength);

        mcmc.setInputValue("operator", createOperators());
        mcmc.setInputValue("logger", createLoggers(logEvery));

        mcmc.initAndValidate();
        return mcmc;
    }

    public void clear() {
        state.clear();
        elements.clear();
    }

    public String toBEASTXML(RandomVariable<lphy.core.Alignment> alignment) {

        MCMC mcmc = createMCMC(1000000, 1000, alignment);

        String xml = new XMLProducer().toXML(mcmc, elements);

        return xml;
    }
}
