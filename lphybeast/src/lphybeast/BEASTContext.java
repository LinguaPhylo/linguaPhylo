package lphybeast;

import beast.core.*;
import beast.core.parameter.Parameter;
import beast.core.parameter.RealParameter;
import beast.core.util.CompoundDistribution;
import beast.evolution.alignment.Taxon;
import beast.evolution.operators.*;
import beast.evolution.operators.Uniform;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.tree.Tree;
import beast.math.distributions.ExcludablePrior;
import beast.math.distributions.ParametricDistribution;
import beast.math.distributions.Prior;
import beast.util.XMLProducer;
import lphybeast.tobeast.generators.*;
import lphybeast.tobeast.values.*;
import lphy.core.LPhyParser;
import lphy.core.distributions.*;
import lphy.graphicalModel.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BEASTContext {

    List<StateNode> state = new ArrayList<>();

    Set<BEASTInterface> elements = new HashSet<>();

    // a map of graphical model nodes to equivalent BEASTInterface objects
    private Map<GraphicalModelNode<?>, BEASTInterface> beastObjects = new HashMap<>();

    // a map of BEASTInterface to graphical model nodes that they represent
    Map<BEASTInterface, GraphicalModelNode<?>> BEASTToLPHYMap = new HashMap<>();

    Map<Class, ValueToBEAST> valueToBEASTMap = new HashMap<>();
    Map<Class, GeneratorToBEAST> generatorToBEASTMap = new HashMap<>();

    private List<Operator> extraOperators = new ArrayList<>();

    SortedMap<String, Taxon> allTaxa = new TreeMap<>();

    LPhyParser parser;

    public BEASTContext(LPhyParser phyParser) {
        parser = phyParser;

        valueToBEASTMap.put(lphy.evolution.alignment.Alignment.class, new AlignmentToBEAST());
        valueToBEASTMap.put(lphy.evolution.tree.TimeTree.class, new TimeTreeToBEAST());
        valueToBEASTMap.put(java.util.Map.class, new MapValueToBEAST());
        valueToBEASTMap.put(Double.class, new DoubleValueToBEAST());
        valueToBEASTMap.put(Double[].class, new DoubleArrayValueToBEAST());
        valueToBEASTMap.put(Double[][].class, new DoubleArray2DValueToBEAST());
        valueToBEASTMap.put(Integer.class, new IntegerValueToBEAST());
        valueToBEASTMap.put(Integer[].class, new IntegerArrayValueToBEAST());

        Class[] generatorToBEASTs = {
                BetaToBEAST.class,
                BirthDeathSampleTreeDTToBEAST.class,
                CoalescentToBEAST.class,
                DirichletToBEAST.class,
                ExpToBEAST.class,
                F81ToBEAST.class,
                GammaToBEAST.class,
                GTRToBEAST.class,
                HKYToBEAST.class,
                JukesCantorToBEAST.class,
                K80ToBEAST.class,
                LogNormalMultiToBEAST.class,
                LogNormalToBEAST.class,
                MultispeciesCoalescentToBEAST.class,
                NormalMultiToBEAST.class,
                NormalToBEAST.class,
                PhyloCTMCToBEAST.class,
                SkylineToBSP.class,
                SerialCoalescentToBEAST.class,
                StructuredCoalescentToMascot.class,
                TN93ToBEAST.class,
                YuleToBEAST.class,
                ExpMarkovChainToBEAST.class
        };

        for (Class c : generatorToBEASTs) {
            try {
                GeneratorToBEAST generatorToBEAST = (GeneratorToBEAST)c.newInstance();
                generatorToBEASTMap.put(generatorToBEAST.getGeneratorClass(), generatorToBEAST);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public BEASTInterface getBEASTObject(GraphicalModelNode<?> node) {
        return beastObjects.get(node);
    }

    public GraphicalModelNode getGraphicalModelNode(BEASTInterface beastInterface) {
        return BEASTToLPHYMap.get(beastInterface);
    }

    public void addBEASTObject(BEASTInterface newBEASTObject) {
        elements.add(newBEASTObject);
    }

    public void removeBEASTObject(BEASTInterface beastObject) {
        elements.remove(beastObject);
        state.remove(beastObject);
        BEASTToLPHYMap.remove(beastObject);

        GraphicalModelNode matchingKey = null;
        for (GraphicalModelNode key : beastObjects.keySet()) {
            if (getBEASTObject(key) == beastObject) {
                matchingKey = key;
                break;
            }
        }
        if (matchingKey != null) beastObjects.remove(matchingKey);
    }

    public static RealParameter createRealParameter(Double[] value) {
        return new RealParameter(value);
    }

    public static RealParameter createRealParameter(double value) {
        RealParameter parameter = new RealParameter();
        parameter.setInputValue("value", value);
        parameter.initAndValidate();
        return parameter;
    }

    /**
     * Make a BEAST2 model from the current model in parser.
     */
    public void createBEASTObjects() {

        Set<Value<?>> sinks = parser.getSinks();

        for (Value<?> value : sinks) {
            createBEASTObjects(value);
        }
    }

    private void createBEASTObjects(Value<?> value) {

        if (beastObjects.get(value) == null) {
            valueToBEAST(value);
        }

        Generator<?> generator = value.getGenerator();
        if (generator != null) {

            for (Object inputObject : generator.getParams().values()) {
                Value<?> input = (Value<?>) inputObject;
                createBEASTObjects(input);
            }

            generatorToBEAST(value, generator);
        }
    }

    /**
     * This is called after valueToBEAST has been called on both the generated value and the input values.
     * Side-effect of this method is to create an equivalent BEAST object of the generator and put it in the beastObjects map of this BEASTContext.
     *
     * @param value
     * @param generator
     */
    private void generatorToBEAST(Value value, Generator generator) {

        if (getBEASTObject(generator) == null) {

            BEASTInterface beastGenerator = null;

            GeneratorToBEAST toBEAST = generatorToBEASTMap.get(generator.getClass());

            if (toBEAST != null) {
                beastGenerator = toBEAST.generatorToBEAST(generator, beastObjects.get(value), this);
            }

            if (beastGenerator == null) {
                System.err.println("Unhandled generator in generatorToBEAST(): " + generator);
            } else {
                addToContext(generator, beastGenerator);
            }
        }
    }

    private BEASTInterface valueToBEAST(Value<?> val) {

        BEASTInterface beastValue = null;

        ValueToBEAST toBEAST = valueToBEASTMap.get(val.value().getClass());

        if (toBEAST != null) {
            beastValue = toBEAST.valueToBEAST(val, this);
        } else {
            for (Class c : valueToBEASTMap.keySet()) {
                if (c.isAssignableFrom(val.value().getClass())) {
                    toBEAST = valueToBEASTMap.get(c);
                    beastValue = toBEAST.valueToBEAST(val, this);
                }
            }
        }
        if (beastValue == null) {
            System.err.println("Unhandled value in valueToBEAST(): " + val + " of type " + val.value().getClass());
        } else {
            addToContext(val, beastValue);
        }
        return beastValue;
    }

    private void addToContext(GraphicalModelNode node, BEASTInterface beastInterface) {
        beastObjects.put(node, beastInterface);
        BEASTToLPHYMap.put(beastInterface, node);
        elements.add(beastInterface);

        if (node instanceof RandomVariable) {
            RandomVariable<?> var = (RandomVariable<?>) node;

            if (var.getOutputs().size() > 0 && !state.contains(beastInterface)) {
                state.add((StateNode) beastInterface);
            }
        }
    }

    /**
     *
     * @param freqParameter
     * @param stateNames the names of the states in a space-delimited string
     * @return
     */
    public static Frequencies createBEASTFrequencies(RealParameter freqParameter, String stateNames) {
        Frequencies frequencies = new Frequencies();
        frequencies.setInputValue("frequencies", freqParameter);
        freqParameter.setInputValue("keys", stateNames);
        freqParameter.initAndValidate();
        frequencies.initAndValidate();
        return frequencies;
    }

    public static Prior createPrior(ParametricDistribution distr, Parameter parameter) {
        Prior prior = new Prior();
        prior.setInputValue("distr", distr);
        prior.setInputValue("x", parameter);
        prior.initAndValidate();
        prior.setID(parameter.getID() + ".prior");
        return prior;
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

        operators.addAll(extraOperators);
        operators.sort(Comparator.comparing(BEASTObject::getID));

        return operators;
    }

    private List<Logger> createLoggers(int logEvery, String fileName) {
        List<Logger> loggers = new ArrayList<>();

        loggers.add(createScreenLogger(logEvery));
        loggers.add(createLogger(logEvery, fileName + ".log"));
        loggers.addAll(createTreeLoggers(logEvery, fileName));

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
        return logger;
    }

    private List<Logger> createTreeLoggers(int logEvery, String fileNameStem) {

        List<Tree> trees = state.stream()
                .filter(stateNode -> stateNode instanceof Tree)
                .map(stateNode -> (Tree) stateNode)
                .sorted(Comparator.comparing(BEASTObject::getID))
                .collect(Collectors.toList());

        boolean multipleTrees = trees.size() > 1;

        List<Logger> treeLoggers = new ArrayList<>();

        for (Tree tree : trees) {
            Logger logger = new Logger();
            logger.setInputValue("logEvery", logEvery);
            logger.setInputValue("log", tree);

            String fileName = fileNameStem + ".trees";

            if (multipleTrees) {
                fileName = fileNameStem + "_" + tree.getID() + ".trees";
            }

            if (fileNameStem != null) logger.setInputValue("fileName", fileName);
            logger.initAndValidate();
            logger.setID(tree.getID() + ".treeLogger");
            treeLoggers.add(logger);
            elements.add(logger);
        }
        return treeLoggers;
    }

    private Logger createScreenLogger(int logEvery) {
        return createLogger(logEvery, null);
    }

    public static double getOperatorWeight(int size) {
        return Math.pow(size, 0.7);
    }

    private Operator createTreeScaleOperator(Tree tree) {
        ScaleOperator operator = new ScaleOperator();
        operator.setInputValue("tree", tree);
        operator.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        operator.initAndValidate();
        operator.setID(tree.getID() + "." + "scale");
        elements.add(operator);

        return operator;
    }

    private Operator createTreeUniformOperator(Tree tree) {
        Uniform uniform = new Uniform();
        uniform.setInputValue("tree", tree);
        uniform.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        uniform.initAndValidate();
        uniform.setID(tree.getID() + "." + "uniform");
        elements.add(uniform);

        return uniform;
    }

    private Operator createSubtreeSlideOperator(Tree tree) {
        SubtreeSlide subtreeSlide = new SubtreeSlide();
        subtreeSlide.setInputValue("tree", tree);
        subtreeSlide.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        subtreeSlide.setInputValue("size", tree.getRoot().getHeight() / 10.0);
        subtreeSlide.initAndValidate();
        subtreeSlide.setID(tree.getID() + "." + "subtreeSlide");
        elements.add(subtreeSlide);

        return subtreeSlide;
    }

    private Operator createExchangeOperator(Tree tree, boolean isNarrow) {
        Exchange exchange = new Exchange();
        exchange.setInputValue("tree", tree);
        exchange.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        exchange.setInputValue("isNarrow", isNarrow);
        exchange.initAndValidate();
        exchange.setID(tree.getID() + "." + ((isNarrow) ? "narrow" : "wide") + "Exchange");
        elements.add(exchange);

        return exchange;
    }

    private Operator createBEASTOperator(RealParameter parameter) {
        RandomVariable<?> variable = (RandomVariable<?>) BEASTToLPHYMap.get(parameter);

        Operator operator;
        if (variable.getGenerativeDistribution() instanceof Dirichlet) {
            Double[] value = (Double[])variable.value();
            operator = new DeltaExchangeOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", getOperatorWeight(parameter.getDimension()-1));
            operator.setInputValue("delta", 1.0/value.length);
            operator.initAndValidate();
            operator.setID(parameter.getID() + ".deltaExchange");
        } else {
            operator = new ScaleOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", getOperatorWeight(parameter.getDimension()));
            operator.setInputValue("scaleFactor", 0.75);
            operator.initAndValidate();
            operator.setID(parameter.getID() + ".scale");
        }
        elements.add(operator);

        return operator;
    }

    private CompoundDistribution createBEASTPosterior() {

        createBEASTObjects();

        List<Distribution> priorList = new ArrayList<>();

        List<Distribution> likelihoodList = new ArrayList<>();

        for (Map.Entry<GraphicalModelNode<?>, BEASTInterface> entry : beastObjects.entrySet()) {
            if (entry.getValue() instanceof Distribution) {
                GenerativeDistribution g = (GenerativeDistribution) entry.getKey();

                if (generatorOfSink(g)) {
                    likelihoodList.add((Distribution) entry.getValue());
                } else {
                    priorList.add((Distribution) entry.getValue());
                }
            }
        }

        for (BEASTInterface beastInterface : elements) {
            if (beastInterface instanceof Distribution && !likelihoodList.contains(beastInterface) && !priorList.contains(beastInterface)) {
                priorList.add((Distribution) beastInterface);
            }
        }

        System.out.println("Found " + likelihoodList.size() + " likelihoods.");
        System.out.println("Found " + priorList.size() + " priors.");

        CompoundDistribution priors = new CompoundDistribution();
        priors.setInputValue("distribution", priorList);
        priors.initAndValidate();
        priors.setID("prior");
        elements.add(priors);

        CompoundDistribution likelihoods = new CompoundDistribution();
        likelihoods.setInputValue("distribution", likelihoodList);
        likelihoods.initAndValidate();
        likelihoods.setID("likelihood");
        elements.add(likelihoods);

        List<Distribution> posteriorList = new ArrayList<>();
        posteriorList.add(priors);
        posteriorList.add(likelihoods);

        CompoundDistribution posterior = new CompoundDistribution();
        posterior.setInputValue("distribution", posteriorList);
        posterior.initAndValidate();
        posterior.setID("posterior");
        elements.add(posterior);

        return posterior;
    }

    private boolean generatorOfSink(GenerativeDistribution g) {
        for (Value<?> var : parser.getSinks()) {
            if (var.getGenerator() == g) {
                return true;
            }
        }
        return false;
    }

    public MCMC createMCMC(long chainLength, int logEvery, String fileName) {

        CompoundDistribution posterior = createBEASTPosterior();

        MCMC mcmc = new MCMC();
        mcmc.setInputValue("distribution", posterior);
        mcmc.setInputValue("chainLength", chainLength);

        mcmc.setInputValue("operator", createOperators());
        mcmc.setInputValue("logger", createLoggers(logEvery, fileName));

        State state = new State();
        state.setInputValue("stateNode", this.state);
        state.initAndValidate();
        elements.add(state);

        // TODO make sure the stateNode list is being correctly populated
        mcmc.setInputValue("state", state);

        mcmc.initAndValidate();
        return mcmc;
    }

    public void clear() {
        state.clear();
        elements.clear();
        beastObjects.clear();
        extraOperators.clear();
    }

    public void runBEAST(String fileNameStem) {

        MCMC mcmc = createMCMC(1000000, 1000, fileNameStem);

        try {
            mcmc.run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String toBEASTXML(String fileNameStem) {

        MCMC mcmc = createMCMC(1000000, 1000, fileNameStem);

        String xml = new XMLProducer().toXML(mcmc, elements);

        return xml;
    }

    public void addExtraOperator(Operator operator) {
        extraOperators.add(operator);
    }

    public void addTaxon(String taxonID) {
        if (!allTaxa.containsKey(taxonID)) {
            allTaxa.put(taxonID, new Taxon(taxonID));
        }
    }

    public List<Taxon> createTaxonList(List<String> ids) {
        List<Taxon> taxonList = new ArrayList<>();
        for (String id : ids) {
            Taxon taxon = allTaxa.get(id);
            if (taxon == null) {
                addTaxon(id);
                taxonList.add(allTaxa.get(id));
            } else {
                taxonList.add(taxon);
            }
        }
        return taxonList;
    }

    public void putBEASTObject(GraphicalModelNode node, BEASTInterface beastInterface) {
        beastObjects.put(node, beastInterface);
    }
}