package lphy.base.evolution.coalescent;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.vectorization.VectorUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.*;

/**
 * Structured coalescent with time-varying (piecewise constant) migration rates and
 * effective population sizes. Rate shifts occur at fixed times specified by the user.
 *
 * <h2>IMPORTANT: Deme Ordering Contract</h2>
 *
 * <p>Demes are <b>always sorted alphabetically</b> to ensure consistency with BEAST2/MASCOT.
 * This determines how the {@code theta} and {@code m} arrays are indexed:</p>
 *
 * <h3>Example with demes = ["B", "B", "A", "A", "C", "C"]</h3>
 * <p>Unique demes sorted alphabetically: A=0, B=1, C=2</p>
 *
 * <h3>Theta (effective population sizes) ordering:</h3>
 * <p>For each time interval, provide Ne values in alphabetical deme order:</p>
 * <pre>
 * theta = [
 *   // Interval 0 (time 0 to rateShiftTimes[1])
 *   Ne_A, Ne_B, Ne_C,
 *   // Interval 1 (time rateShiftTimes[1] to rateShiftTimes[2])
 *   Ne_A, Ne_B, Ne_C,
 *   ...
 * ]
 * </pre>
 *
 * <h3>Migration rates (m) ordering:</h3>
 * <p>For S demes, there are S*(S-1) migration rates per interval.
 * The order is: for source deme i (0 to S-1), for dest deme j (0 to S-1, j != i):</p>
 * <pre>
 * m = [
 *   // Interval 0
 *   m_0→1, m_0→2, m_1→0, m_1→2, m_2→0, m_2→1,
 *   // Interval 1
 *   m_0→1, m_0→2, m_1→0, m_1→2, m_2→0, m_2→1,
 *   ...
 * ]
 * </pre>
 * <p>Where indices 0, 1, 2 correspond to alphabetically sorted deme names (A, B, C).</p>
 *
 * <h3>Rate shift times:</h3>
 * <p>Times are the <b>start</b> of each interval, going backward from the present:</p>
 * <pre>
 * rateShiftTimes = [0.0, 5.0, 10.0]  // 3 intervals
 * // Interval 0: time 0.0 to 5.0 (most recent)
 * // Interval 1: time 5.0 to 10.0
 * // Interval 2: time 10.0 to infinity (most ancient)
 * </pre>
 *
 * @see StructuredCoalescent
 */
@Citation(
        value="Müller, N. F., Rasmussen, D. A., & Stadler, T. (2017). " +
                "The structured coalescent and its approximations. " +
                "Molecular biology and evolution, 34(11), 2970-2981.",
        title = "The structured coalescent and its approximations",
        year = 2017, authors = {"Müller","Rasmussen","Stadler"},
        DOI="https://doi.org/10.1093/molbev/msx186")
public class StructuredCoalescentRateShifts extends TaxaConditionedTreeGenerator {

    public static final String thetaParamName = "theta";
    public static final String mParamName = "m";
    public static final String rateShiftTimesParamName = "rateShiftTimes";
    public static final String demesParamName = "demes";

    private Value<Double[]> theta;
    private Value<Double[]> m;
    private Value<Double[]> rateShiftTimes;
    private Value<Object[]> demes;

    // Demes are always sorted alphabetically (required for MASCOT compatibility)
    private List<String> uniqueDemes;
    private int nDemes;
    private int nIntervals;

    public static final String populationLabel = "deme";

    enum EventType {coalescent, migration}

    /**
     * Structured coalescent with time-varying rates.
     *
     * @param theta effective population sizes, flattened array of [nIntervals * nDemes] values.
     *              Order: for each interval (0 to nIntervals-1), provide Ne for each deme
     *              in alphabetical order.
     * @param m migration rates, flattened array of [nIntervals * nDemes * (nDemes-1)] values.
     *          Order: for each interval, for source deme i, for dest deme j (j != i),
     *          where i,j are indices of alphabetically sorted demes.
     * @param rateShiftTimes start times of each interval going backward from present.
     *                       First element should be 0.0. Length determines number of intervals.
     * @param taxa the taxa with ages (for serially sampled data)
     * @param demes the deme assignment for each taxon, parallel to taxa array
     */
    public StructuredCoalescentRateShifts(
            @ParameterInfo(name = thetaParamName,
                    description = "Effective population sizes for all intervals. " +
                            "Flattened array of [nIntervals * nDemes] values. " +
                            "For each interval, provide Ne for each deme in ALPHABETICAL order of deme names.")
            Value<Double[]> theta,

            @ParameterInfo(name = mParamName,
                    description = "Migration rates for all intervals. " +
                            "Flattened array of [nIntervals * nDemes * (nDemes-1)] values. " +
                            "For each interval, rates are ordered: for source i, for dest j (j!=i), " +
                            "where i,j are indices of ALPHABETICALLY sorted deme names. " +
                            "Rates are in units of expected migrants per generation backward in time.")
            Value<Double[]> m,

            @ParameterInfo(name = rateShiftTimesParamName,
                    description = "Start times of each interval, going backward from present. " +
                            "First element should be 0.0 (present). " +
                            "Example: [0.0, 5.0, 10.0] defines 3 intervals.")
            Value<Double[]> rateShiftTimes,

            @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName,
                    description = "The taxa.")
            Value<Taxa> taxa,

            @ParameterInfo(name = demesParamName,
                    description = "Deme assignment for each taxon, parallel to the taxa array. " +
                            "Demes will be sorted alphabetically to determine indices.")
            Value<Object[]> demes) {

        super(null, taxa, null);

        this.theta = theta;
        this.m = m;
        this.rateShiftTimes = rateShiftTimes;
        this.demes = demes;

        if (taxa == null)
            throw new IllegalArgumentException("taxa must be specified!");
        if (demes == null)
            throw new IllegalArgumentException("demes must be specified!");
        if (rateShiftTimes == null)
            throw new IllegalArgumentException("rateShiftTimes must be specified!");

        initDemes();
        validateArraySizes();
    }

    private void initDemes() {
        Object[] demesVal = demes.value();

        // Get unique demes and sort alphabetically
        Set<String> demesSet = new TreeSet<>(); // TreeSet automatically sorts
        for (Object d : demesVal) {
            demesSet.add(String.valueOf(d));
        }
        uniqueDemes = new ArrayList<>(demesSet);
        nDemes = uniqueDemes.size();
        nIntervals = rateShiftTimes.value().length;
    }

    private void validateArraySizes() {
        int expectedThetaSize = nIntervals * nDemes;
        int expectedMSize = nIntervals * nDemes * (nDemes - 1);

        if (theta.value().length != expectedThetaSize) {
            throw new IllegalArgumentException(
                    String.format("theta array size mismatch: expected %d (nIntervals=%d * nDemes=%d), got %d",
                            expectedThetaSize, nIntervals, nDemes, theta.value().length));
        }

        if (m.value().length != expectedMSize) {
            throw new IllegalArgumentException(
                    String.format("m array size mismatch: expected %d (nIntervals=%d * nDemes=%d * (nDemes-1)=%d), got %d",
                            expectedMSize, nIntervals, nDemes, nDemes - 1, m.value().length));
        }

        // Validate rate shift times are in increasing order
        Double[] times = rateShiftTimes.value();
        for (int i = 1; i < times.length; i++) {
            if (times[i] <= times[i - 1]) {
                throw new IllegalArgumentException(
                        "rateShiftTimes must be in strictly increasing order. Got: " + Arrays.toString(times));
            }
        }
    }

    @GeneratorInfo(name = "StructuredCoalescentRateShifts",
            category = GeneratorCategory.COAL_TREE,
            description = "Structured coalescent with piecewise constant (time-varying) migration rates " +
                    "and effective population sizes. Rate shifts occur at fixed times. " +
                    "IMPORTANT: Demes are always sorted alphabetically - provide theta and m " +
                    "arrays with values ordered according to alphabetically sorted deme names.")
    public RandomVariable<TimeTree> sample() {

        Taxa taxa = getTaxa();
        TimeTree tree = new TimeTree(taxa);

        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();
        List<List<TimeTreeNode>> activeNodes = new ArrayList<>();

        // Initialize active node lists for each deme
        for (int i = 0; i < nDemes; i++) {
            activeNodes.add(new ArrayList<>());
        }

        // Assign taxa to demes
        Object[] demesVal = demes.value();
        for (int i = 0; i < demesVal.length; i++) {
            String deme = String.valueOf(demesVal[i]);
            int demeIndex = uniqueDemes.indexOf(deme);
            if (demeIndex < 0) {
                throw new IllegalArgumentException("Unknown deme: " + deme);
            }

            TimeTreeNode node = new TimeTreeNode(taxa.getTaxon(i), tree);
            node.setIndex(i);
            node.setMetaData(populationLabel, demeIndex);

            if (node.getAge() <= 0.0) {
                activeNodes.get(demeIndex).add(node);
            } else {
                leavesToBeAdded.add(node);
            }
        }

        // Sort leaves by age (youngest at end)
        leavesToBeAdded.sort((o1, o2) -> Double.compare(o2.getAge(), o1.getAge()));

        // Simulate the structured coalescent with time-varying rates
        TimeTreeNode root = simulateStructuredCoalescentForest(tree, activeNodes, leavesToBeAdded);

        tree.setRoot(root);

        // Convert integer deme indices to proper names for compatibility
        sanitiseDemeNames(tree);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private TimeTreeNode simulateStructuredCoalescentForest(
            TimeTree tree,
            List<List<TimeTreeNode>> activeNodes,
            List<TimeTreeNode> leavesToBeAdded) {

        double time = 0.0;
        int nodeNumber = getTotalNodeCount(activeNodes);

        // Get the current interval based on time
        int currentInterval = 0;
        Double[][] rateMatrix = buildRateMatrix(currentInterval);

        double[][] rates = new double[nDemes][nDemes];
        double totalRate = populateRateMatrix(activeNodes, rateMatrix, rates);

        while ((getTotalNodeCount(activeNodes) + leavesToBeAdded.size()) > 1) {
            int k = getTotalNodeCount(activeNodes);

            if (k == 1 && leavesToBeAdded.size() > 0) {
                // Wait for next leaf
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else if (k > 1) {
                SCEvent event = selectRandomEvent(rates, totalRate, time);

                // Check if we cross into the next interval before the event
                int nextInterval = getIntervalAtTime(event.time);
                double nextIntervalStart = (currentInterval + 1 < nIntervals) ?
                        rateShiftTimes.value()[currentInterval + 1] : Double.POSITIVE_INFINITY;

                // Check if next leaf arrives before the event
                double nextLeafTime = leavesToBeAdded.isEmpty() ?
                        Double.POSITIVE_INFINITY : leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();

                if (nextIntervalStart < event.time && nextIntervalStart <= nextLeafTime) {
                    // Move to next interval boundary
                    time = nextIntervalStart;
                    currentInterval++;
                    rateMatrix = buildRateMatrix(currentInterval);
                    totalRate = populateRateMatrix(activeNodes, rateMatrix, rates);
                    continue;
                }

                if (nextLeafTime < event.time) {
                    // Wait for next leaf
                    time = nextLeafTime;
                } else {
                    // Process the event
                    if (event.type == EventType.coalescent) {
                        TimeTreeNode node1 = selectRandomNode(activeNodes.get(event.pop));
                        TimeTreeNode node2 = selectRandomNode(activeNodes.get(event.pop));

                        TimeTreeNode parent = new TimeTreeNode((String) null, tree);
                        parent.setIndex(nodeNumber);
                        parent.setAge(event.time);
                        parent.setMetaData(populationLabel, event.pop);
                        parent.addChild(node1);
                        parent.addChild(node2);

                        activeNodes.get(event.pop).add(parent);
                    } else {
                        // Migration event
                        TimeTreeNode migrant = selectRandomNode(activeNodes.get(event.pop));

                        TimeTreeNode migrantsParent = new TimeTreeNode((String) null, tree);
                        migrantsParent.setIndex(nodeNumber);
                        migrantsParent.setAge(event.time);
                        migrantsParent.setMetaData(populationLabel, event.toPop);
                        migrantsParent.addChild(migrant);

                        activeNodes.get(event.toPop).add(migrantsParent);
                    }
                    time = event.time;
                    nodeNumber++;
                }
            }

            // Add any leaves that are now active
            while (leavesToBeAdded.size() > 0 &&
                    leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() <= time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
                activeNodes.get((Integer) youngest.getMetaData(populationLabel)).add(youngest);
            }

            // Update rates for current time
            currentInterval = getIntervalAtTime(time);
            rateMatrix = buildRateMatrix(currentInterval);
            totalRate = populateRateMatrix(activeNodes, rateMatrix, rates);
        }

        // Collect root node
        for (List<TimeTreeNode> nodeList : activeNodes) {
            if (!nodeList.isEmpty()) {
                return nodeList.get(0);
            }
        }
        throw new RuntimeException("No root node found!");
    }

    /**
     * Get the interval index for a given time.
     * Intervals are defined by rateShiftTimes: interval i covers [rateShiftTimes[i], rateShiftTimes[i+1])
     */
    private int getIntervalAtTime(double time) {
        Double[] times = rateShiftTimes.value();
        for (int i = times.length - 1; i >= 0; i--) {
            if (time >= times[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Build the rate matrix (Ne on diagonal, migration rates off-diagonal) for a given interval.
     */
    private Double[][] buildRateMatrix(int interval) {
        Double[][] matrix = new Double[nDemes][nDemes];
        Double[] thetaVals = theta.value();
        Double[] mVals = m.value();

        int thetaOffset = interval * nDemes;
        int mOffset = interval * nDemes * (nDemes - 1);

        // Fill diagonal with Ne values
        for (int i = 0; i < nDemes; i++) {
            matrix[i][i] = thetaVals[thetaOffset + i];
        }

        // Fill off-diagonal with migration rates
        int mIndex = 0;
        for (int i = 0; i < nDemes; i++) {
            for (int j = 0; j < nDemes; j++) {
                if (i != j) {
                    matrix[i][j] = mVals[mOffset + mIndex];
                    mIndex++;
                }
            }
        }

        return matrix;
    }

    private int getTotalNodeCount(List<List<TimeTreeNode>> nodes) {
        int count = 0;
        for (List<TimeTreeNode> nodeList : nodes) {
            count += nodeList.size();
        }
        return count;
    }

    private TimeTreeNode selectRandomNode(List<TimeTreeNode> nodes) {
        int index = random.nextInt(nodes.size());
        return nodes.remove(index);
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
        throw new RuntimeException("Failed to select event");
    }

    static double populateRateMatrix(List<List<TimeTreeNode>> nodes, Double[][] popSizesMigrationRates, double[][] rates) {
        double totalRate = 0.0;

        for (int i = 0; i < rates.length; i++) {
            double popSizei = popSizesMigrationRates[i][i];
            int sampleSizei = nodes.get(i).size();

            // Coalescent rate
            if (sampleSizei < 2) {
                rates[i][i] = 0.0;
            } else {
                rates[i][i] = (double) CombinatoricsUtils.binomialCoefficient(sampleSizei, 2) / popSizei;
            }

            // Migration rates
            for (int j = 0; j < rates[i].length; j++) {
                if (i != j) {
                    double popSizej = popSizesMigrationRates[j][j];
                    // Migration rate scaled by lineage count and population sizes
                    rates[i][j] = (double) nodes.get(i).size() * (popSizesMigrationRates[i][j] * popSizej) / popSizei;
                }
                totalRate += rates[i][j];
            }
        }

        return totalRate;
    }

    private void sanitiseDemeNames(TimeTree tree) {
        for (TimeTreeNode node : tree.getNodes()) {
            Object demeIndex = node.getMetaData(populationLabel);
            if (demeIndex instanceof Integer) {
                String demeName = uniqueDemes.get((Integer) demeIndex);
                // If name is numeric, prefix with "deme."
                try {
                    Integer.parseInt(demeName);
                    demeName = populationLabel + VectorUtils.INDEX_SEPARATOR + demeName;
                } catch (NumberFormatException ex) {
                    // Name is not numeric, use as-is
                }
                node.setMetaData(populationLabel, demeName);
            }
        }
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        // TODO: implement for inference
        return Double.NaN;
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> params = super.getParams();
        params.put(thetaParamName, theta);
        params.put(mParamName, m);
        params.put(rateShiftTimesParamName, rateShiftTimes);
        params.put(demesParamName, demes);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case thetaParamName -> theta = value;
            case mParamName -> m = value;
            case rateShiftTimesParamName -> rateShiftTimes = value;
            case demesParamName -> demes = value;
            default -> super.setParam(paramName, value);
        }
    }

    public Value<Double[]> getTheta() { return theta; }
    public Value<Double[]> getM() { return m; }
    public Value<Double[]> getRateShiftTimes() { return rateShiftTimes; }
    public Value<Object[]> getDemes() { return demes; }

    public List<String> getUniqueDemes() { return uniqueDemes; }
    public int getNDemes() { return nDemes; }
    public int getNIntervals() { return nIntervals; }

    class SCEvent {
        int pop;
        int toPop;
        double time;
        EventType type;

        public SCEvent(int pop1, int pop2, double time) {
            this.pop = pop1;
            this.toPop = pop2;
            this.time = time;
            this.type = (pop == toPop) ? EventType.coalescent : EventType.migration;
        }
    }
}
