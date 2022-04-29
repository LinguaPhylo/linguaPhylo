package lphy.evolution.coalescent;

import lphy.core.distributions.Utils;
import lphy.evolution.Taxa;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleArray2DValue;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * The structured coalescent describes a coalescent process
 * in subpopulations between which individuals can migrate.
 */
@Citation(
        value="Müller, N. F., Rasmussen, D. A., & Stadler, T. (2017). " +
                "The structured coalescent and its approximations. " +
                "Molecular biology and evolution, 34(11), 2970-2981.",
        title = "The structured coalescent and its approximations",
        year = 2017, authors = {"Müller","Rasmussen","Stadler"},
        DOI="https://doi.org/10.1093/molbev/msx186")
public class StructuredCoalescent extends TaxaConditionedTreeGenerator {

    public static final String MParamName = "M";
    public static final String kParamName = "k";
    public static final String demesParamName = "demes";
    public static final String sortParamName = "sort";
    private Value<Double[][]> theta;
    private Value<Integer[]> k;
    private Value<Object[]> demes;
    private Value<Boolean> sort;

    RandomGenerator random;

    // convert demes String/Integer into String for sorting if required,
    // use demeIndex which is the key of reverseDemeToIndex.
    // the index of List is the key of reverseDemeToIndex.
    private List<String> uniqueDemes;
    private Map<Integer, String> reverseDemeToIndex;

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
                                @ParameterInfo(name = kParamName, description = "the number of taxa in each population. provide either this or a " + demesParamName + " argument.", optional = true) Value<Integer[]> k,
                                @ParameterInfo(name = taxaParamName, description = "the taxa.", optional = true) Value<Taxa> taxa,
                                @ParameterInfo(name = demesParamName, description = "the deme array, which runs parallel to the taxonArray in the taxa object.", optional = true) Value<Object[]> demes,
                                @ParameterInfo(name = sortParamName, description = "whether to sort the deme array, " +
                                        "before mapping them to the indices of the effective population sizes and migration rates. " +
                                        "If not, as default, the pop size indices are determined by the natural order of the deme array, " +
                                        "if true, then the indices are the order of sorted deme array.", optional = true) Value<Boolean> sort) {

        super(null, taxa, null);

        this.theta = theta;
        this.k = k;
        this.demes = demes;
        this.sort = sort;

        if (taxa == null && k == null)
            throw new IllegalArgumentException("One of " + taxaParamName + " and " + kParamName + " must be specified!");

        int count = ((k != null) ? 1 : 0) + ((demes != null) ? 1 : 0);
        if (count != 1)
            throw new IllegalArgumentException("Exactly one of " + demesParamName + " and " + kParamName + " must be specified!");

        this.random = Utils.getRandom();

        initDemes();
    }

    public int n() {
        if (k != null) {
            int[] sum = {0};
            Stream.of(k.value()).forEach(i -> sum[0] += i);
            return sum[0];
        } else return super.n();
    }

    @GeneratorInfo(name = "StructuredCoalescent",
            category = GeneratorCategory.TREE_PRIOR_COAL,
            examples = {"https://linguaphylo.github.io/tutorials/structured-coalescent/"},
            description = "The structured coalescent distribution over tip-labelled time trees.")
    public RandomVariable<TimeTree> sample() {


        Taxa taxa = getTaxa();
        TimeTree tree = new TimeTree(taxa);

        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();
        List<List<TimeTreeNode>> activeNodes = new ArrayList<>();

        double time = 0.0;

        if (k != null && !isSort()) { // the demes are indices from 0 to k.value().length-1

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
            // this includes k != null && isSort()
            List<String> uniqueDemes = getUniqueDemes();

            if (uniqueDemes.size() != theta.value().length)
                throw new RuntimeException("The number of unique demes " + uniqueDemes.size() +
                        " does not match the dimension of theta " + theta.value().length + " !");

            for (int i = 0; i < uniqueDemes.size(); i++)
                activeNodes.add(new ArrayList<>());

            Object[] demesVal = demes != null ? demes.value() : k.value();

            // if demes are Integer[], then after sort, they will be 0, 1, 10 ,...
            for (int i = 0; i < demesVal.length; i++) {

//                String deme = demesVal[i].toString();
                // covert to String
                String deme = String.valueOf(demesVal[i]);

                int demeIndex = uniqueDemes.indexOf(deme);
                if (demeIndex < 0)
                    throw new IllegalArgumentException();

                TimeTreeNode node = new TimeTreeNode(taxa.getTaxon(i), tree);
                node.setIndex(i);
                // demeIndex is required in simulateStructuredCoalescentForest
                node.setMetaData(populationLabel, demeIndex);

//                if (activeNodes.size() <= demeIndex) {
//                    activeNodes.add(new ArrayList<>());
//                } // this is not working if sorted

                if (node.getAge() <= time) {
                    activeNodes.get(demeIndex).add(node);
                } else {
                    leavesToBeAdded.add(node);
                }
            }
        }

        leavesToBeAdded.sort((o1, o2) -> Double.compare(o2.getAge(), o1.getAge())); // REVERSE ORDER - youngest age at end of list

        // this requires Integer as MetaData of populationLabel, which is used as index of activeNodes
        TimeTreeNode root = simulateStructuredCoalescentForest(tree, activeNodes, leavesToBeAdded, theta.value(), Double.POSITIVE_INFINITY).get(0);

        tree.setRoot(root);

        // this makes compatible tree (metadata) with BEAST related software
        // TODO always create demes first, and this code can be replaced to use reverseDemeToIndex
        sanitiseIntegerNames(tree);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    // if either k or demes not null, then convert their value to String, and then sort if isSort()==true
    private void initDemes() {
        uniqueDemes = new ArrayList<>();
        reverseDemeToIndex = new HashMap<>();

        Object[] demesVal = k != null ? k.value() : demes.value();

        Set<Object> demesSet = new LinkedHashSet<>(Arrays.asList(demesVal));
        // convert it to List and get by index from List
        for (Object d : demesSet)
            uniqueDemes.add(String.valueOf(d));

        if (isSort())
            Collections.sort(uniqueDemes);

        // fill in reverseDemeToIndex
        for (int i = 0; i < demesVal.length; i++) {

            // covert to String
            String deme = String.valueOf(demesVal[i]);

            int demeIndex = uniqueDemes.indexOf(deme);
            if (demeIndex < 0)
                throw new IllegalArgumentException();

            reverseDemeToIndex.put(demeIndex, deme);
        }

    }


    // TreeAnnotator cannot parse int for metadata names
    // if demes are using index as names, then convert to the original names
    // otherwise replace to populationLabel + "." + i
    private void sanitiseIntegerNames(TimeTree tree) {

        if (k != null) {

            for (TimeTreeNode node : tree.getNodes()) {
                Integer demeIndex = getDemeIndex(node);
                String properName = populationLabel + VectorUtils.INDEX_SEPARATOR + demeIndex;
                // replace to deme.i
                node.setMetaData(populationLabel, properName);
            }

        } else {
            // replace names by unique demes
            // convert demes.values to List and get by index from List
            List<String> uniqueDemes = getUniqueDemes();

            for (TimeTreeNode node : tree.getNodes()) {
                Integer demeIndex = getDemeIndex(node);
                // MetaData is also index of demes in the unique list
                String properName = uniqueDemes.get(demeIndex);

                // if name is still Integer
                try {
                    Integer.parseInt(properName);
                    properName = populationLabel + VectorUtils.INDEX_SEPARATOR + properName;
                } catch (NumberFormatException ex) { }

                // replace to demes[i]
                node.setMetaData(populationLabel, properName);
            }
        }
    }

    private Integer getDemeIndex(TimeTreeNode node) {
        Object demeIndex = node.getMetaData(populationLabel);
        // MetaData must be Integer
        if (! (demeIndex instanceof Integer) )
            throw new IllegalArgumentException("Metadata name should be Integer before this process !");
        return (Integer) demeIndex;
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
        if (sort != null) params.put(sortParamName, sort);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(MParamName)) theta = value;
        else if (paramName.equals(kParamName)) k = value;
        else if (paramName.equals(demesParamName)) demes = value;
        else if (paramName.equals(sortParamName)) sort = value;
        else super.setParam(paramName, value);
    }

    public Value<Double[][]> getM() {
        return theta;
    }

    public String getPopulationLabel() {
        return populationLabel;
    }

    public boolean isSort() {
        return sort != null && sort.value();
    }

    /**
     * @return  the unique demes. If sort is true, then the demes are sorted.
     */
    public List<String> getUniqueDemes() {
        if (uniqueDemes == null) // initDemes() has to be called before this
            throw new IllegalArgumentException();
        return uniqueDemes;
    }


    public String toString() {
        return getName();
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

                    StructuredCoalescent coalescent = new StructuredCoalescent(theta, k, null, null, null);

                    RandomVariable<TimeTree> tree = coalescent.sample();

                    Object meta = tree.value().getRoot().getMetaData(populationLabel);
                    String meta2 = String.valueOf(meta).substring(String.valueOf(meta).lastIndexOf(VectorUtils.INDEX_SEPARATOR) + 1);
                    Integer intLabel = Integer.parseInt(meta2);
                    count += intLabel  == 0 ? 1 : 0;
                }
                System.out.println(popSize1[i] + "\t" + popSize2[i] + "\t" + m + "\t" + ((double) count / (double) reps));
            }
        }
    }

}
