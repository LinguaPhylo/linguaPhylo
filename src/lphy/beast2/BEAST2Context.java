package lphy.beast2;

import beast.core.*;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.Parameter;
import beast.core.parameter.RealParameter;
import beast.core.util.CompoundDistribution;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.evolution.likelihood.TreeLikelihood;
import beast.evolution.operators.*;
import beast.evolution.operators.Uniform;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.substitutionmodel.SubstitutionModel;
import beast.evolution.tree.Tree;
import beast.evolution.tree.coalescent.ConstantPopulation;
import beast.evolution.tree.coalescent.TreeIntervals;
import beast.math.distributions.Exponential;
import beast.math.distributions.LogNormalDistributionModel;
import beast.math.distributions.ParametricDistribution;
import beast.math.distributions.Prior;
import beast.util.TreeParser;
import beast.util.XMLProducer;
import lphy.Coalescent;
import lphy.TimeTree;
import lphy.core.LPhyParser;
import lphy.core.PhyloCTMC;
import lphy.core.distributions.*;
import lphy.core.functions.F81;
import lphy.core.functions.GTR;
import lphy.core.functions.HKY;
import lphy.core.functions.JukesCantor;
import lphy.graphicalModel.*;

import java.util.*;
import java.util.stream.Collectors;

public class BEAST2Context {

    List<StateNode> state = new ArrayList<>();

    Set<BEASTInterface> elements = new HashSet<>();

    // a map of graphical model nodes to equivalent BEASTInterface objects
    Map<GraphicalModelNode<?>, BEASTInterface> cloneMap = new HashMap<>();

    Map<BEASTInterface, GraphicalModelNode<?>> reverseCloneMap = new HashMap<>();

    LPhyParser parser;

    public BEAST2Context(LPhyParser phyParser) {
        parser = phyParser;
    }

    /**
     * Clone the current model to BEAST2
     */
    public void cloneAll() {

        Set<Value<?>> sinks = parser.getSinks();

        for (Value<?> value : sinks) {
            cloneAll(value);
        }
    }

    private void cloneAll(Value<?> value) {

        cloneValue(value);

        Generator<?> generator = value.getGenerator();

        if (generator != null) {

            for (Object inputObject : generator.getParams().values()) {
                Value<?> input = (Value<?>) inputObject;
                cloneAll(input);
            }

            cloneGenerator(value, generator);
        }
    }

    /**
     * This is called after cloneValue has been called on both the generated value and the input values.
     * Side-effect of this method is to create a clone object of the generator and put it in the cloneMap of this BEAST2Context.
     *
     * @param value
     * @param generator
     */
    private void cloneGenerator(Value value, Generator generator) {
        BEASTInterface clone = null;

        if (generator instanceof PhyloCTMC) {
            clone = createTreeLikelihood((PhyloCTMC) generator, (Alignment) cloneMap.get(value));
        } else if (generator instanceof Coalescent) {
            clone = createBEASTCoalescent((Coalescent) generator, (Tree) cloneMap.get(value));
        } else if (generator instanceof JukesCantor) {
            clone = createBEASTJukesCantor((JukesCantor) generator);
        } else if (generator instanceof F81) {
            clone = createBEASTF81((F81)generator);
        } else if (generator instanceof HKY) {
            clone = createBEASTHKY((HKY)generator);
        } else if (generator instanceof GTR) {
            clone = createBEASTGTR((GTR)generator);
        } else if (generator instanceof LogNormal) {
            clone = createBEASTDistribution((LogNormal)generator, (Parameter)cloneMap.get(value));
        } else if (generator instanceof Exp) {
            clone = createBEASTDistribution((Exp)generator, (Parameter)cloneMap.get(value));
        } else if (generator instanceof Beta) {
            clone = createBEASTDistribution((Beta)generator, (Parameter)cloneMap.get(value));
        } else if (generator instanceof Dirichlet) {
            clone = createBEASTDistribution((Dirichlet) generator, (Parameter)cloneMap.get(value));
        }

        if (clone == null) {
            throw new RuntimeException("Generator " + generator + " not handled in cloneGenerator()");
        } else {
            addToContext(generator, clone);
        }
    }

    private BEASTInterface cloneValue(Value<?> val) {

        BEASTInterface clone = null;

        if (val.value() instanceof TimeTree) {
            clone = createBEASTTree((Value<TimeTree>) val, null);
        } else if (val.value() instanceof Double || val.value() instanceof Double[] || val.value() instanceof Double[][]) {
            clone = createBEASTRealParameter(val);
        } else if (val.value() instanceof Integer || val.value() instanceof Integer[]) {
            clone = createBEASTIntegerParameter(val);
        } else if (val.value() instanceof lphy.core.Alignment) {
            clone = createBEASTAlignment((Value<lphy.core.Alignment>) val);
        }
        if (clone == null) {
            throw new RuntimeException("Unhandled value in cloneValue(): " + val);
        }

        addToContext(val, clone);
        return clone;
    }

    private void addToContext(GraphicalModelNode node, BEASTInterface beastInterface) {
        cloneMap.put(node, beastInterface);
        reverseCloneMap.put(beastInterface, node);
        elements.add(beastInterface);

        if (node instanceof RandomVariable) {
            RandomVariable<?> var = (RandomVariable<?>)node;

            if (var.getOutputs().size() > 0 && !state.contains(beastInterface)) {
                state.add((StateNode) beastInterface);
            }
        }
    }

    private beast.evolution.tree.coalescent.Coalescent createBEASTCoalescent(lphy.Coalescent coalescent, Tree tree) {

        beast.evolution.tree.coalescent.Coalescent beastCoalescent = new beast.evolution.tree.coalescent.Coalescent();

        TreeIntervals treeIntervals = new TreeIntervals();
        treeIntervals.setInputValue("tree", tree);
        treeIntervals.initAndValidate();

        beastCoalescent.setInputValue("treeIntervals", treeIntervals);

        ConstantPopulation populationFunction = new ConstantPopulation();
        populationFunction.setInputValue("popSize", cloneMap.get(coalescent.getTheta()));
        populationFunction.initAndValidate();

        beastCoalescent.setInputValue("populationModel", populationFunction);

        beastCoalescent.initAndValidate();
        elements.add(beastCoalescent);

        return beastCoalescent;
    }

    public TreeLikelihood createTreeLikelihood(PhyloCTMC phyloCTMC, Alignment alignment) {

        TreeLikelihood treeLikelihood = new TreeLikelihood();

        treeLikelihood.setInputValue("data", alignment);

        Tree tree = (Tree) cloneMap.get(phyloCTMC.getTree());
        treeLikelihood.setInputValue("tree", tree);

        if (phyloCTMC.getBranchRates() != null) {
            throw new RuntimeException("Relaxed clock models not handled yet.");
        } else {
            StrictClockModel clockModel = new StrictClockModel();
            Value<Double> clockRate = phyloCTMC.getClockRate();
            if (clockRate != null) {
                clockModel.setInputValue("clock.rate", cloneMap.get(clockRate));
            } else {
                clockModel.setInputValue("clock.rate", createRealParameter(1.0));
            }
            treeLikelihood.setInputValue("branchRateModel", clockModel);
        }

        Generator qGenerator = phyloCTMC.getQ().getGenerator();
        if (qGenerator == null) {
            throw new RuntimeException("BEAST2 does not support a fixed Q matrix.");
        } else {
            SubstitutionModel substitutionModel = (SubstitutionModel)cloneMap.get(qGenerator);

            SiteModel siteModel = new SiteModel();
            siteModel.setInputValue("substModel", substitutionModel);
            siteModel.initAndValidate();

            treeLikelihood.setInputValue("siteModel", siteModel);
        }

        treeLikelihood.initAndValidate();
        elements.add(treeLikelihood);

        return treeLikelihood;
    }

    private beast.evolution.substitutionmodel.JukesCantor createBEASTJukesCantor(JukesCantor generator) {
        beast.evolution.substitutionmodel.JukesCantor beastJC = new beast.evolution.substitutionmodel.JukesCantor();
        beastJC.initAndValidate();
        elements.add(beastJC);
        return beastJC;
    }

    public beast.evolution.substitutionmodel.GTR createBEASTGTR(GTR gtr) {
        beast.evolution.substitutionmodel.GTR beastGTR = new beast.evolution.substitutionmodel.GTR();

        Value<Double[]> rates = gtr.getRates();

        beastGTR.setInputValue("rates", cloneMap.get(rates));
        beastGTR.setInputValue("frequencies", createBEASTFrequencies((RealParameter)cloneMap.get(gtr.getFreq())));
        beastGTR.initAndValidate();
        elements.add(beastGTR);
        return beastGTR;
    }

    public beast.evolution.substitutionmodel.HKY createBEASTHKY(HKY hky) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", cloneMap.get(hky.getKappa()));
        beastHKY.setInputValue("frequencies", createBEASTFrequencies((RealParameter)cloneMap.get(hky.getFreq())));
        beastHKY.initAndValidate();
        elements.add(beastHKY);
        return beastHKY;
    }

    public beast.evolution.substitutionmodel.HKY createBEASTF81(F81 f81) {
        beast.evolution.substitutionmodel.HKY beastHKY = new beast.evolution.substitutionmodel.HKY();
        beastHKY.setInputValue("kappa", new RealParameter("1.0"));
        beastHKY.setInputValue("frequencies", createBEASTFrequencies((RealParameter)cloneMap.get(f81.getFreq())));
        beastHKY.initAndValidate();
        elements.add(beastHKY);
        return beastHKY;
    }

    public Frequencies createBEASTFrequencies(RealParameter freqParameter) {
        Frequencies frequencies = new Frequencies();
        frequencies.setInputValue("frequencies", freqParameter);
        frequencies.initAndValidate();
        elements.add(frequencies);
        return frequencies;
    }

    public Prior createBEASTDistribution(LogNormal logNormal, Parameter parameter) {
        LogNormalDistributionModel logNormalDistributionModel = new LogNormalDistributionModel();
        logNormalDistributionModel.setInputValue("M", cloneMap.get(logNormal.getParams().get("meanlog")));
        logNormalDistributionModel.setInputValue("S", cloneMap.get(logNormal.getParams().get("sdlog")));
        logNormalDistributionModel.initAndValidate();
        elements.add(logNormalDistributionModel);

        return createPrior(logNormalDistributionModel, parameter);
    }

    public Prior createPrior(ParametricDistribution distr, Parameter parameter) {
        Prior prior = new Prior();
        prior.setInputValue("distr", distr);
        prior.setInputValue("x", parameter);
        prior.initAndValidate();

        return prior;
    }

    public Prior createBEASTDistribution(Beta beta, Parameter parameter) {
        beast.math.distributions.Beta betaDistribution = new beast.math.distributions.Beta();
        betaDistribution.setInputValue("alpha", cloneMap.get(beta.getParams().get("alpha")));
        betaDistribution.setInputValue("beta", cloneMap.get(beta.getParams().get("beta")));
        betaDistribution.initAndValidate();
        elements.add(betaDistribution);

        return createPrior(betaDistribution, parameter);
    }

    public Prior createBEASTDistribution(Exp exp, Parameter parameter) {
        Exponential exponential = new Exponential();
        exponential.setInputValue("mean", cloneMap.get(exp.getParams().get("mean")));
        exponential.initAndValidate();
        elements.add(exponential);

        return createPrior(exponential, parameter);
    }

    public Prior createBEASTDistribution(Dirichlet dirichlet, Parameter parameter) {
        beast.math.distributions.Dirichlet beastDirichlet = new beast.math.distributions.Dirichlet();
        beastDirichlet.setInputValue("alpha", cloneMap.get(dirichlet.getConcentration()));
        beastDirichlet.initAndValidate();
        elements.add(beastDirichlet);

        return createPrior(beastDirichlet, parameter);
    }

    private RealParameter createRealParameter(double value) {
        RealParameter parameter = new RealParameter();
        parameter.setInputValue("value", value);
        parameter.initAndValidate();

        return parameter;
    }

    public RealParameter createBEASTRealParameter(Value value) {

        RealParameter parameter = new RealParameter();
        if (value.value() instanceof Double) {
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);
            parameter.initAndValidate();
        } else if (value.value() instanceof Double[]) {
            List<Double> values = Arrays.asList((Double[]) value.value());
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.initAndValidate();
        } else if (value.value() instanceof Double[][]) {

            Double[][] val = (Double[][]) value.value();

            List<Double> values = new ArrayList<>(val.length * val[0].length);
            for (int i = 0; i < val.length; i++) {
                for (int j = 0; j < val[0].length; j++) {
                    values.add(val[i][j]);
                }
            }
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.setInputValue("minordimension", val[0].length); // TODO check this!
            parameter.initAndValidate();
        } else {
            throw new IllegalArgumentException();
        }
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());
        elements.add(parameter);

        return parameter;
    }

    private IntegerParameter createBEASTIntegerParameter(Value<?> value) {
        IntegerParameter parameter = new IntegerParameter();
        if (value.value() instanceof Integer) {
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);
            parameter.initAndValidate();
        } else if (value.value() instanceof Integer[]) {
            List<Integer> values = Arrays.asList((Integer[]) value.value());
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.initAndValidate();
        } else {
            throw new IllegalArgumentException();
        }
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());
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
        addToContext(timeTree, tree);

        return tree;
    }

    public Alignment createBEASTAlignment(Value<lphy.core.Alignment> lpAlignment) {

        List<Sequence> sequences = new ArrayList<>();

        lphy.core.Alignment alignment = lpAlignment.value();

        String[] taxaNames = alignment.getTaxaNames();

        for (int i = 0; i < alignment.getTaxonCount(); i++) {
            sequences.add(createBEASTSequence(taxaNames[i], alignment.getSequence(i)));
        }

        Alignment beastAlignment = new Alignment();
        beastAlignment.setInputValue("sequence", sequences);
        beastAlignment.initAndValidate();

        beastAlignment.setID(lpAlignment.getCanonicalId());
        addToContext(lpAlignment, beastAlignment);

        return beastAlignment;
    }

    public Sequence createBEASTSequence(String taxon, String sequence) {
        Sequence seq = new Sequence();
        seq.setInputValue("taxon", taxon);
        seq.setInputValue("value", sequence);
        seq.initAndValidate();

        elements.add(seq);

        return seq;
    }

    public List<Operator> createOperators() {

        List<Operator> operators = new ArrayList<>();

        for (StateNode stateNode : state) {
            System.out.println("State node" + stateNode);
            if (stateNode instanceof RealParameter) {
                operators.add(createBEASTOperator((RealParameter) stateNode));
            } else if (stateNode instanceof Tree) {
                operators.add(createTreeScaleOperator((Tree) stateNode));
                operators.add(createExchangeOperator((Tree) stateNode, true));
                operators.add(createExchangeOperator((Tree) stateNode, false));
                operators.add(createSubtreeSlideOperator((Tree) stateNode));
                operators.add(createTreeUniformOperator((Tree) stateNode));
            }
        }

        return operators;
    }

    private List<Logger> createLoggers(int logEvery, String fileName) {
        List<Logger> loggers = new ArrayList<>();

        loggers.add(createScreenLogger(logEvery));
        loggers.add(createLogger(logEvery, fileName + ".log"));
        loggers.add(createTreeLogger(logEvery, fileName + ".trees"));

        return loggers;
    }

    private Logger createLogger(int logEvery, String fileName) {

        List<StateNode> nonTrees = state.stream()
                .filter(stateNode -> !(stateNode instanceof Tree))
                .collect(Collectors.toList());

        Logger logger = new Logger();
        logger.setInputValue("logEvery", logEvery);
        logger.setInputValue("log", nonTrees);
        if (fileName != null) logger.setInputValue("fileName", fileName);
        logger.initAndValidate();
        elements.add(logger);
        return logger;    }

    private Logger createTreeLogger(int logEvery, String fileName) {

        List<Tree> trees = state.stream()
                .filter(stateNode -> stateNode instanceof Tree)
                .map (stateNode -> (Tree) stateNode)
                .collect(Collectors.toList());

        Logger logger = new Logger();
        logger.setInputValue("logEvery", logEvery);
        logger.setInputValue("log", trees);
        if (fileName != null) logger.setInputValue("fileName", fileName);
        logger.initAndValidate();
        elements.add(logger);
        return logger;
    }

    private Logger createScreenLogger(int logEvery) {
        return createLogger(logEvery, null);
    }

    private Operator createTreeScaleOperator(Tree tree) {
        ScaleOperator operator = new ScaleOperator();
        operator.setInputValue("tree", tree);
        operator.setInputValue("weight", 1.0);
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    private Operator createTreeUniformOperator(Tree tree) {
        Uniform uniform = new Uniform();
        uniform.setInputValue("tree", tree);
        uniform.setInputValue("weight", 1.0);
        uniform.initAndValidate();
        elements.add(uniform);

        return uniform;
    }

    private Operator createSubtreeSlideOperator(Tree tree) {
        SubtreeSlide subtreeSlide = new SubtreeSlide();
        subtreeSlide.setInputValue("tree", tree);
        subtreeSlide.setInputValue("weight", 1.0);
        subtreeSlide.setInputValue("size", tree.getRoot().getHeight()/10.0);
        subtreeSlide.initAndValidate();
        elements.add(subtreeSlide);

        return subtreeSlide;
    }

    private Operator createExchangeOperator(Tree tree, boolean isNarrow) {
        Exchange exchange = new Exchange();
        exchange.setInputValue("tree", tree);
        exchange.setInputValue("weight", 1.0);
        exchange.setInputValue("isNarrow", isNarrow);
        exchange.initAndValidate();
        elements.add(exchange);

        return exchange;
    }

    private Operator createBEASTOperator(RealParameter parameter) {
        RandomVariable<?> variable = ( RandomVariable<?>)reverseCloneMap.get(parameter);

        Operator operator;
        if (variable.getGenerativeDistribution() instanceof Dirichlet) {
            operator = new DeltaExchangeOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", 1.0);
        } else {
            operator = new ScaleOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", 1.0);
            operator.setInputValue("scaleFactor", 0.75);
        }
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    private CompoundDistribution createBEASTPosterior() {

        cloneAll();

        List<Distribution> distributions = new ArrayList<>();
        for (BEASTInterface beastInterface : cloneMap.values()) {
            if (beastInterface instanceof Distribution) {
                distributions.add((Distribution) beastInterface);
            }
        }

        CompoundDistribution posterior = new CompoundDistribution();
        posterior.setInputValue("distribution", distributions);
        posterior.initAndValidate();
        posterior.setID("posterior");

        return posterior;
    }


    public MCMC createMCMC(long chainLength, int logEvery, String fileName) {

        CompoundDistribution posterior = createBEASTPosterior();

        MCMC mcmc = new MCMC();
        mcmc.setInputValue("distribution", posterior);
        mcmc.setInputValue("chainLength", chainLength);

        mcmc.setInputValue("operator", createOperators());
        mcmc.setInputValue("logger", createLoggers(logEvery, fileName));

        mcmc.initAndValidate();
        return mcmc;
    }

    public void clear() {
        state.clear();
        elements.clear();
        cloneMap.clear();
    }

    public String toBEASTXML(String fileNameStem) {

        MCMC mcmc = createMCMC(1000000, 1000, fileNameStem);

        String xml = new XMLProducer().toXML(mcmc, elements);

        return xml;
    }
}
