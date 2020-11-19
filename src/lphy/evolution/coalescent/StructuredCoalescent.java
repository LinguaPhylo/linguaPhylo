package lphy.evolution.coalescent;

import lphy.evolution.Taxa;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.core.distributions.Utils;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.*;
import java.util.stream.Stream;

import static lphy.core.distributions.DistributionConstants.*;

public class StructuredCoalescent extends TaxaConditionedTreeGenerator {

    public static final String MParamName = "M";
    public static final String kParamName = "k";
    public static final String demesParamName = "demes";
    private Value<Double[][]> theta;
    private Value<Integer[]> k;
    private Value<Object[]> demes;

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

    // TODO need to allow StructuredCoalescent to be generated from Taxa + deme Metadata
    public StructuredCoalescent(@ParameterInfo(name = MParamName, description = "The population process rate matrix which contains the effective population sizes and migration rates. " +
            "Off-diagonal migration rates are in units of expected migrants per *generation* backwards in time.") Value<Double[][]> theta,
                                @ParameterInfo(name = kParamName, description = "the number of taxa in each population. provide either this or a " + demesParamName + " argument.", type = Integer[].class, optional = true) Value<Integer[]> k,
                                @ParameterInfo(name = taxaParamName, description = "the taxa.", type = Taxa.class, optional = true) Value<Taxa> taxa,
                                @ParameterInfo(name = demesParamName, description = "the deme array, which runs parallel to the taxonArray in the taxa object.", type = Object[].class, optional = true) Value<Object[]> demes) {

        super(null, taxa, null);

        this.theta = theta;
        this.k = k;
        this.demes = demes;

        if (taxa == null && k == null)
            throw new IllegalArgumentException("One of " + taxaParamName + " and " + kParamName + " must be specified!");

        int count = ((k != null) ? 1 : 0) + ((demes != null) ? 1 : 0);
        if (count != 1)
            throw new IllegalArgumentException("Exactly one of " + demesParamName + " and " + kParamName + " must be specified!");

        this.random = Utils.getRandom();
    }

    public int n() {
        if (k != null) {
            int[] sum = {0};
            Stream.of(k.value()).forEach(i -> sum[0] += i);
            return sum[0];
        } else return super.n();
    }

    @GeneratorInfo(name = "StructuredCoalescent", description = "The structured coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {


        Taxa taxa = getTaxa();
        TimeTree tree = new TimeTree(taxa);

        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();
        List<List<TimeTreeNode>> activeNodes = new ArrayList<>();

        double time = 0.0;

        if (k != null) {
            int count = 0;
            for (int i = 0; i < k.value().length; i++) {
                activeNodes.add(new ArrayList<>());
                for (int j = 0; j < k.value()[i]; j++) {
                    TimeTreeNode node = new TimeTreeNode(count + "", tree);
                    node.setIndex(count);
                    node.setMetaData(populationLabel, i);
                    node.setAge(0);
                    activeNodes.get(i).add(node);
                    count += 1;
                }
            }
        } else {
            List<String> demeNames = new ArrayList<>();
            for (int i = 0; i < demes.value().length; i++) {

                String deme = demes.value()[i].toString();
                int demeIndex = demeNames.indexOf(deme);

                if (demeIndex < 0) {
                    demeNames.add(deme);
                    demeIndex = demeNames.size() - 1;
                }

                TimeTreeNode node = new TimeTreeNode(taxa.getTaxon(i), tree);
                node.setIndex(i);
                node.setMetaData(populationLabel, demeIndex);

                if (activeNodes.size() <= demeIndex) {
                    activeNodes.add(new ArrayList<>());
                }

                if (node.getAge() <= time) {
                    activeNodes.get(demeIndex).add(node);
                } else {
                    leavesToBeAdded.add(node);
                }
            }
        }

        leavesToBeAdded.sort((o1, o2) -> Double.compare(o2.getAge(), o1.getAge())); // REVERSE ORDER - youngest age at end of list


        TimeTreeNode root = simulateStructuredCoalescentForest(tree, activeNodes, leavesToBeAdded, theta.value(), Double.POSITIVE_INFINITY).get(0);

        tree.setRoot(root);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private List<TimeTreeNode> simulateStructuredCoalescentForest(TimeTree tree, List<List<TimeTreeNode>> activeNodes, List<TimeTreeNode> leavesToBeAdded, Double[][] popSizesMigrationRates, double stopTime) {

        //diagonals are coalescent rates, off-diagonals are migration rates
        double[][] rates = new double[activeNodes.size()][activeNodes.size()];
        double totalRate = populateRateMatrix(activeNodes, popSizesMigrationRates, rates);

        double time = 0.0;

        int nodeNumber = getTotalNodeCount(activeNodes);

        while (time < stopTime && (getTotalNodeCount(activeNodes)+leavesToBeAdded.size()) > 1) {
            int k = getTotalNodeCount(activeNodes);

            if (k == 1) {
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else {
                SCEvent event = selectRandomEvent(rates, totalRate, time);

                // if event passes the next node to be added then update the time and try again
                if (leavesToBeAdded.size() > 0 && event.time > leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge()) {
                    time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
                } else {

                    if (event.type == EventType.coalescent) {

                        //System.out.println("doing a coalescent event in population " + event.pop + " which has " + nodes.get(event.pop).size() + " nodes.");

                        // coalescent
                        TimeTreeNode node1 = selectRandomNode(activeNodes.get(event.pop));
                        TimeTreeNode node2 = selectRandomNode(activeNodes.get(event.pop));

                        TimeTreeNode parent = new TimeTreeNode((String) null, tree);
                        parent.setIndex(nodeNumber);
                        parent.setAge(event.time);
                        parent.setMetaData(populationLabel, event.pop);
                        parent.addChild(node1);
                        parent.addChild(node2);

                        time = event.time;

                        activeNodes.get(event.pop).add(parent);

                    } else {
                        // migration

                        if (event.pop == event.toPop)
                            throw new RuntimeException("migration must be between distinct populations");

                        TimeTreeNode migrant = selectRandomNode(activeNodes.get(event.pop));

                        TimeTreeNode migrantsParent = new TimeTreeNode((String) null, tree);
                        migrantsParent.setIndex(nodeNumber);
                        migrantsParent.setAge(event.time);
                        migrantsParent.setMetaData(populationLabel, event.toPop);

                        migrantsParent.addChild(migrant);

                        time = event.time;

                        activeNodes.get(event.toPop).add(migrantsParent);
                    }
                    nodeNumber += 1;
                }
            }

            while (leavesToBeAdded.size() > 0 && leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() == time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
                activeNodes.get((Integer)youngest.getMetaData(populationLabel)).add(youngest);
            }
            totalRate = populateRateMatrix(activeNodes, popSizesMigrationRates, rates);
        }

        List<TimeTreeNode> rootNodes = new ArrayList<>();
        for (List<TimeTreeNode> nodeList : activeNodes) {
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
    public Map<String, Value> getParams() {
        Map<String, Value> params = super.getParams();
        params.put(MParamName, theta);
        if (k != null) params.put(kParamName, k);
        if (demes != null) params.put(demesParamName, demes);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(MParamName)) theta = value;
        else if (paramName.equals(kParamName)) k = value;
        else if (paramName.equals(demesParamName)) demes = value;
        else super.setParam(paramName, value);
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

        double[] popSize1 = new double[]{1, 1, 1, 1, 1};
        double[] popSize2 = new double[]{1, 2, 4, 8, 16};

        System.out.println("pop0.leaf, pop1.leaf, pop0.mig, pop1.mig, pop0.coal, pop1.coal");

        for (double m = 0.125; m < 32; m *= 2) {
            for (int i = 0; i < popSize1.length; i++) {
                long count = 0;
                long migrations = 0;
                for (int j = 0; j < reps; j++) {

                    DoubleArray2DValue theta = new DoubleArray2DValue("theta", new Double[][]{{popSize1[i], m}, {m, popSize2[i]}});
                    Value<Integer[]> k = new Value<>("k", new Integer[]{2, 2});

                    StructuredCoalescent coalescent = new StructuredCoalescent(theta, k, null, null);

                    RandomVariable<TimeTree> tree = coalescent.sample();

                    count += (Integer) tree.value().getRoot().getMetaData(populationLabel) == 0 ? 1 : 0;
                }
                System.out.println(popSize1[i] + "\t" + popSize2[i] + "\t" + m + "\t" + ((double) count / (double) reps));
            }
        }
    }

    public String toString() {
        return getName();
    }

}
