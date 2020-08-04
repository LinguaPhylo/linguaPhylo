package lphy.evolution.coalescent;

import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.*;

public class StructuredCoalescent implements GenerativeDistribution<TimeTree> {

    private final String thetaParamName;
    private final String nParamName;
    private Value<Double[][]> theta;
    private Value<Integer[]> n;

    RandomGenerator random;

    public static int countMigrations(TimeTree timeTree) {
        int migrationCount = 0;
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (node.getChildCount() == 1) {
                if (!node.getMetaData(populationLabel).equals(node.getChildren().get(0).getMetaData(populationLabel))) {
                    migrationCount += 1;
                }
            }
        }
        return migrationCount;
    }

    enum EventType {coalescent, migration}

    public static final String populationLabel = "deme";


    public StructuredCoalescent(@ParameterInfo(name = "M", description = "The population process rate matrix which contains the effective population sizes and migration rates. " +
            "Off-diagonal migration rates are in units of expected migrants per *generation* backwards in time.") Value<Double[][]> theta,
                                @ParameterInfo(name = "n", description = "the number of taxa in each population.") Value<Integer[]> n) {
        this.theta = theta;
        this.n = n;
        this.random = Utils.getRandom();

        thetaParamName = getParamName(0);
        nParamName = getParamName(1);
    }

    @GeneratorInfo(name = "StructuredCoalescent", description = "The structured coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        int count = 0;
        List<List<TimeTreeNode>> nodes = new ArrayList<>();
        for (int i = 0; i < n.value().length; i++) {
            nodes.add(new ArrayList<>());
            for (int j = 0; j < n.value()[i]; j++) {
                TimeTreeNode node = new TimeTreeNode(count + "", tree);
                node.setIndex(count);
                node.setMetaData(populationLabel, i);
                node.setAge(0);
                nodes.get(i).add(node);
                count += 1;
            }
        }

        TimeTreeNode root = simulateStructuredCoalescentForest(tree, nodes, theta.value(), Double.POSITIVE_INFINITY).get(0);

        tree.setRoot(root);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private List<TimeTreeNode> simulateStructuredCoalescentForest(TimeTree tree, List<List<TimeTreeNode>> nodes, Double[][] popSizesMigrationRates, double stopTime) {

        //diagonals are coalescent rates, off-diagonals are migration rates
        double[][] rates = new double[nodes.size()][nodes.size()];
        double totalRate = populateRateMatrix(nodes, popSizesMigrationRates, rates);

        double time = 0.0;

        int nodeNumber = getTotalNodeCount(nodes);

        while (time < stopTime && getTotalNodeCount(nodes) > 1) {

            SCEvent event = selectRandomEvent(rates, totalRate, time);
            //System.out.println("Recieved an event of type " + event.type);

            if (event.type == EventType.coalescent) {

                //System.out.println("doing a coalescent event in population " + event.pop + " which has " + nodes.get(event.pop).size() + " nodes.");

                // coalescent
                TimeTreeNode node1 = selectRandomNode(nodes.get(event.pop));
                TimeTreeNode node2 = selectRandomNode(nodes.get(event.pop));

                TimeTreeNode parent = new TimeTreeNode(nodeNumber + "", tree);
                parent.setIndex(nodeNumber);
                parent.setAge(event.time);
                parent.setMetaData(populationLabel, event.pop);
                parent.addChild(node1);
                parent.addChild(node2);

                time = event.time;

                nodes.get(event.pop).add(parent);

            } else {
                // migration

                if (event.pop == event.toPop)
                    throw new RuntimeException("migration must be between distinct populations");

                TimeTreeNode migrant = selectRandomNode(nodes.get(event.pop));

                TimeTreeNode migrantsParent = new TimeTreeNode(nodeNumber + "", tree);
                migrantsParent.setIndex(nodeNumber);
                migrantsParent.setAge(event.time);
                migrantsParent.setMetaData(populationLabel, event.toPop);

                migrantsParent.addChild(migrant);

                time = event.time;

                nodes.get(event.toPop).add(migrantsParent);
            }
            totalRate = populateRateMatrix(nodes, popSizesMigrationRates, rates);
            nodeNumber += 1;
        }

        List<TimeTreeNode> rootNodes = new ArrayList<>();
        for (List<TimeTreeNode> nodeList : nodes) {
            rootNodes.addAll(nodeList);
        }

        return rootNodes;
    }

    private int getTotalNodeCount(List<List<TimeTreeNode>> nodes) {
        int count = 0;
        for (List<TimeTreeNode> nodeList : nodes) {
            count += nodeList.size();
        }
        return count;
    }

    private TimeTreeNode selectRandomNode(List<TimeTreeNode> nodes) {
        int index = Utils.getRandom().nextInt(nodes.size());
        TimeTreeNode node = nodes.remove(index);
        return node;
    }

    SCEvent selectRandomEvent(double[][] rates, double totalRate, double time) {

        double U = random.nextDouble() * totalRate;

        for (int i = 0; i < rates.length; i++) {
            for (int j = 0; j < rates.length; j++) {
                if (U > rates[i][j]) {
                    U -= rates[i][j];
                } else {
                    double V = random.nextDouble();
                    double etime = time + (-Math.log(V) / totalRate);
                    return new SCEvent(i, j, etime);
                }
            }
        }
        throw new RuntimeException();
    }

    static double populateRateMatrix(List<List<TimeTreeNode>> nodes, Double[][] popSizesMigrationRates, double[][] rates) {

        double totalRate = 0.0;

        // coalescent rates
        for (int i = 0; i < rates.length; i++) {
            double popSizei = popSizesMigrationRates[i][i];
            int sampleSizei = nodes.get(i).size();
            if (sampleSizei < 2) {
                rates[i][i] = 0.0;
            } else {
                rates[i][i] = (double) CombinatoricsUtils.binomialCoefficient(sampleSizei, 2) / popSizei;
            }
            for (int j = 0; j < rates[i].length; j++) {
                double popSizej = popSizesMigrationRates[j][j];
                if (i != j) {
                    // off-diagonal migration rates are in units of expected migrants per generation (thus division by popSizei)
                    rates[i][j] = (double) nodes.get(i).size() * (popSizesMigrationRates[i][j] * popSizej) / popSizei;
                }
                totalRate += rates[i][j];
            }
        }

        return totalRate;
    }

    class SCEvent {

        int pop;
        int toPop;
        double time;
        EventType type;

        public SCEvent(int pop1, int pop2, double time) {
            this.pop = pop1;
            this.toPop = pop2;
            this.time = time;
            if (pop == toPop) {
                type = EventType.coalescent;
            } else {
                type = EventType.migration;
            }
        }

    }

    @Override
    public double logDensity(TimeTree timeTree) {

        //TODO
        return Double.NaN;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        map.put(thetaParamName, theta);
        map.put(nParamName, n);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(nParamName)) n = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public Value<Double[][]> getM() {
        return theta;
    }

    public String getPopulationLabel() {
        return populationLabel;
    }

    public static void main(String[] args) {

        for (int n = 2; n < 10; n++) {
            System.out.println(CombinatoricsUtils.binomialCoefficient(n, 2));
        }

        long reps = 1000;

        double[] popSize1 = new double[]{1,1,1,1,1};
        double[] popSize2 = new double[]{1,2,4,8,16};

        System.out.println("pop0.leaf, pop1.leaf, pop0.mig, pop1.mig, pop0.coal, pop1.coal");

        for (double m = 0.125; m < 32; m *= 2) {
            for (int i = 0; i < popSize1.length; i++) {
                long count = 0;
                long migrations = 0;
                for (int j = 0; j < reps; j++) {

                    DoubleArray2DValue theta = new DoubleArray2DValue("theta", new Double[][]{{popSize1[i], m}, {m, popSize2[i]}});
                    Value<Integer[]> n = new Value<>("n", new Integer[]{2, 2});

                    StructuredCoalescent coalescent = new StructuredCoalescent(theta, n);

                    RandomVariable<TimeTree> tree = coalescent.sample();

                    count += (Integer)tree.value().getRoot().getMetaData(populationLabel) == 0 ? 1: 0;
                }
                System.out.println(popSize1[i] + "\t" + popSize2[i] + "\t" + m + "\t" + ((double) count / (double) reps));
            }
        }
    }

    public String toString() {
        return getName();
    }
    
}
