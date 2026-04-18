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
 * Structured coalescent with piecewise-constant per-deme effective population sizes
 * (a Skyline on log-Ne) and time-constant migration rates. Counterpart of Mascot's
 * StructuredSkyline with per-deme Skygrowth.
 *
 * <h2>Deme Ordering Contract</h2>
 *
 * <p>Demes are <b>always sorted alphabetically</b> to ensure consistency with BEAST2/MASCOT.
 * This determines how {@code logNe} and {@code M} are indexed.</p>
 *
 * <h3>logNe layout</h3>
 * <p>{@code logNe[i][e]} is the natural log of effective population size of deme {@code i}
 * (alphabetical) during epoch {@code e} (epoch 0 = most recent).
 * Shape: {@code [nDemes][nEpochs]}.</p>
 *
 * <h3>Migration layout</h3>
 * <p>Flat array of length {@code nDemes * (nDemes - 1)}. Order: for source deme {@code i}
 * (alphabetical), for dest deme {@code j != i} (alphabetical):</p>
 * <pre>
 * M = [ m_0→1, m_0→2, ..., m_1→0, m_1→2, ..., m_2→0, m_2→1, ... ]
 * </pre>
 * <p>Migration is constant over time in this generator (no per-interval migration).</p>
 *
 * <h3>Rate shift times</h3>
 * <p>{@code rateShifts[e]} is the start of epoch {@code e}, going backward from the present.
 * Length equals the number of epochs ({@code logNe[i].length}). Times must be strictly
 * increasing; first element is typically {@code 0.0}. Epoch {@code e} covers
 * {@code [rateShifts[e], rateShifts[e+1])}; the last epoch extends to infinity.</p>
 *
 * @see StructuredCoalescentRateShifts
 * @see StructuredCoalescent
 */
@Citation(
        value="Müller, N. F., Rasmussen, D. A., & Stadler, T. (2017). " +
                "The structured coalescent and its approximations. " +
                "Molecular biology and evolution, 34(11), 2970-2981.",
        title = "The structured coalescent and its approximations",
        year = 2017, authors = {"Müller","Rasmussen","Stadler"},
        DOI="https://doi.org/10.1093/molbev/msx186")
public class StructuredCoalescentSkyline extends TaxaConditionedTreeGenerator {

    public static final String logNeParamName = "logNe";
    public static final String MParamName = "M";
    public static final String rateShiftsParamName = "rateShifts";
    public static final String demesParamName = "demes";
    public static final String interpolationParamName = "interpolation";

    public static final String INTERP_CONSTANT = "constant";
    public static final String INTERP_LINEAR = "linear";

    private Value<Double[][]> logNe;
    private Value<Double[]> M;
    private Value<Double[]> rateShifts;
    private Value<Object[]> demes;
    private Value<String> interpolation;

    private List<String> uniqueDemes;
    private int nDemes;
    private int nEpochs;
    private boolean isLinear;

    public static final String populationLabel = "deme";

    enum EventType {coalescent, migration}

    public StructuredCoalescentSkyline(
            @ParameterInfo(name = logNeParamName,
                    description = "Log effective population sizes per deme per epoch. " +
                            "Shape [nDemes][nEpochs]. Row index follows ALPHABETICAL order of deme names; " +
                            "column index is epoch (0 = most recent).")
            Value<Double[][]> logNe,

            @ParameterInfo(name = MParamName,
                    description = "Migration rates (constant over time). " +
                            "Flat array of length nDemes * (nDemes - 1). " +
                            "Order: for source i (alphabetical), for dest j != i (alphabetical). " +
                            "Rates are in units of expected migrants per generation backward in time.")
            Value<Double[]> M,

            @ParameterInfo(name = rateShiftsParamName,
                    description = "Start times of each Ne epoch, going backward from the present. " +
                            "Length equals number of epochs. Strictly increasing; first element typically 0.0.")
            Value<Double[]> rateShifts,

            @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName,
                    description = "The taxa.")
            Value<Taxa> taxa,

            @ParameterInfo(name = demesParamName,
                    description = "Deme assignment for each taxon, parallel to the taxa array. " +
                            "Demes are sorted alphabetically to determine indices.")
            Value<Object[]> demes,

            @ParameterInfo(name = interpolationParamName,
                    description = "How log-Ne is interpolated between epochs. " +
                            "\"constant\" (default): piecewise-constant log-Ne, where logNe[deme][i] is the value " +
                            "throughout epoch i. \"linear\": piecewise-linear log-Ne, where logNe[deme][i] is the " +
                            "value at the knot time rateShifts[i]; between rateShifts[i] and rateShifts[i+1] " +
                            "log-Ne interpolates linearly between logNe[deme][i] and logNe[deme][i+1]; after the " +
                            "last knot log-Ne stays at logNe[deme][nEpochs-1]. The linear mode matches Mascot's " +
                            "Skygrowth; the constant mode matches Mascot's StructuredSkygrid.",
                    optional = true)
            Value<String> interpolation) {

        super(null, taxa, null);

        this.logNe = logNe;
        this.M = M;
        this.rateShifts = rateShifts;
        this.demes = demes;
        this.interpolation = interpolation;

        if (taxa == null)
            throw new IllegalArgumentException("taxa must be specified!");
        if (demes == null)
            throw new IllegalArgumentException("demes must be specified!");
        if (rateShifts == null)
            throw new IllegalArgumentException("rateShifts must be specified!");

        String mode = (interpolation == null) ? INTERP_CONSTANT : interpolation.value();
        if (!INTERP_CONSTANT.equals(mode) && !INTERP_LINEAR.equals(mode)) {
            throw new IllegalArgumentException(
                    "Unknown interpolation \"" + mode + "\"; expected \"" + INTERP_CONSTANT + "\" or \"" + INTERP_LINEAR + "\"");
        }
        this.isLinear = INTERP_LINEAR.equals(mode);

        initDemes();
        validateArraySizes();
    }

    private void initDemes() {
        Object[] demesVal = demes.value();
        Set<String> demesSet = new TreeSet<>();
        for (Object d : demesVal) {
            demesSet.add(String.valueOf(d));
        }
        uniqueDemes = new ArrayList<>(demesSet);
        nDemes = uniqueDemes.size();
        nEpochs = rateShifts.value().length;
    }

    private void validateArraySizes() {
        Double[][] logNeVal = logNe.value();
        if (logNeVal.length != nDemes) {
            throw new IllegalArgumentException(String.format(
                    "logNe outer dimension mismatch: expected %d (nDemes), got %d",
                    nDemes, logNeVal.length));
        }
        for (int i = 0; i < nDemes; i++) {
            if (logNeVal[i].length != nEpochs) {
                throw new IllegalArgumentException(String.format(
                        "logNe[%d] length mismatch: expected %d (nEpochs), got %d",
                        i, nEpochs, logNeVal[i].length));
            }
        }

        int expectedMSize = nDemes * (nDemes - 1);
        if (M.value().length != expectedMSize) {
            throw new IllegalArgumentException(String.format(
                    "M array size mismatch: expected %d (nDemes * (nDemes - 1)), got %d",
                    expectedMSize, M.value().length));
        }

        Double[] times = rateShifts.value();
        for (int i = 1; i < times.length; i++) {
            if (times[i] <= times[i - 1]) {
                throw new IllegalArgumentException(
                        "rateShifts must be in strictly increasing order. Got: " + Arrays.toString(times));
            }
        }
    }

    @GeneratorInfo(name = "StructuredCoalescentSkyline",
            category = GeneratorCategory.COAL_TREE,
            examples = {"structuredCoalescentSkyline.lphy"},
            description = "Structured coalescent with a per-deme Skyline on log effective population sizes " +
                    "and time-constant migration rates. Demes are always sorted alphabetically; " +
                    "provide logNe and M arrays with entries ordered by alphabetically sorted deme names.")
    public RandomVariable<TimeTree> sample() {

        Taxa taxa = getTaxa();
        TimeTree tree = new TimeTree(taxa);

        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();
        List<List<TimeTreeNode>> activeNodes = new ArrayList<>();

        for (int i = 0; i < nDemes; i++) {
            activeNodes.add(new ArrayList<>());
        }

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

        leavesToBeAdded.sort((o1, o2) -> Double.compare(o2.getAge(), o1.getAge()));

        TimeTreeNode root = isLinear
                ? simulateLinearInterpolation(tree, activeNodes, leavesToBeAdded)
                : simulateStructuredCoalescentForest(tree, activeNodes, leavesToBeAdded);
        tree.setRoot(root);

        sanitiseDemeNames(tree);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private TimeTreeNode simulateStructuredCoalescentForest(
            TimeTree tree,
            List<List<TimeTreeNode>> activeNodes,
            List<TimeTreeNode> leavesToBeAdded) {

        double time = 0.0;
        int nodeNumber = getTotalNodeCount(activeNodes);

        int currentInterval = 0;
        Double[][] rateMatrix = buildRateMatrix(currentInterval);

        double[][] rates = new double[nDemes][nDemes];
        double totalRate = StructuredCoalescentRateShifts.populateRateMatrix(activeNodes, rateMatrix, rates);

        while ((getTotalNodeCount(activeNodes) + leavesToBeAdded.size()) > 1) {
            int k = getTotalNodeCount(activeNodes);

            if (k == 1 && leavesToBeAdded.size() > 0) {
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else if (k > 1) {
                SCEvent event = selectRandomEvent(rates, totalRate, time);

                double nextIntervalStart = (currentInterval + 1 < nEpochs) ?
                        rateShifts.value()[currentInterval + 1] : Double.POSITIVE_INFINITY;

                double nextLeafTime = leavesToBeAdded.isEmpty() ?
                        Double.POSITIVE_INFINITY : leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();

                if (nextIntervalStart < event.time && nextIntervalStart <= nextLeafTime) {
                    time = nextIntervalStart;
                    currentInterval++;
                    rateMatrix = buildRateMatrix(currentInterval);
                    totalRate = StructuredCoalescentRateShifts.populateRateMatrix(activeNodes, rateMatrix, rates);
                    continue;
                }

                if (nextLeafTime < event.time) {
                    time = nextLeafTime;
                } else {
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

            while (leavesToBeAdded.size() > 0 &&
                    leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() <= time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
                activeNodes.get((Integer) youngest.getMetaData(populationLabel)).add(youngest);
            }

            currentInterval = getIntervalAtTime(time);
            rateMatrix = buildRateMatrix(currentInterval);
            totalRate = StructuredCoalescentRateShifts.populateRateMatrix(activeNodes, rateMatrix, rates);
        }

        for (List<TimeTreeNode> nodeList : activeNodes) {
            if (!nodeList.isEmpty()) {
                return nodeList.get(0);
            }
        }
        throw new RuntimeException("No root node found!");
    }

    private int getIntervalAtTime(double time) {
        Double[] times = rateShifts.value();
        for (int i = times.length - 1; i >= 0; i--) {
            if (time >= times[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Linear-mode forward simulator using exact inverse-CDF sampling.
     *
     * Within a linear segment the log-Ne of each deme evolves as a line,
     * so each event's hazard (coalescent in one deme, or migration between
     * two demes) has the form h(τ) = A · exp(B · τ) in local time τ from
     * the current wall-clock time. Its integrated hazard has a closed form,
     * and we invert {@code H(τ) = u} (with {@code u ~ Exp(1)}) to draw an
     * exact per-event waiting time. The next event is {@code argmin_e τ_e};
     * no thinning or rejection is needed. If the first event falls beyond
     * the next segment boundary (or the next leaf arrival), we instead
     * advance to that horizon and re-enter the loop.
     *
     * Parameters per event type at time t within segment s:
     *   - Coalescent in deme i (n_i lineages): A = C(n_i, 2) · exp(-a_i),
     *     B = -b_i, where a_i = logNe[i](t) and b_i is the log-Ne slope
     *     in this segment. In the tail segment b_i = 0 (constant Ne).
     *   - Migration i → j (i != j): A = n_i · m_ij · exp(a_j - a_i),
     *     B = b_j - b_i. This is Mascot's backward-time rate n_i · m_ij ·
     *     Ne_j(t) / Ne_i(t), and since the Ne ratio is itself a quotient of
     *     two exp-of-linears, the hazard stays in the A·exp(B·τ) form even
     *     though Ne is time-varying (per-event inversion + argmin avoids
     *     the "sum of heterogeneous exponentials" problem that would break
     *     total-rate inversion).
     */
    private TimeTreeNode simulateLinearInterpolation(
            TimeTree tree,
            List<List<TimeTreeNode>> activeNodes,
            List<TimeTreeNode> leavesToBeAdded) {

        double time = 0.0;
        int nodeNumber = getTotalNodeCount(activeNodes);
        double[] a = new double[nDemes];
        double[] b = new double[nDemes];
        Double[] mVals = M.value();

        while ((getTotalNodeCount(activeNodes) + leavesToBeAdded.size()) > 1) {
            int k = getTotalNodeCount(activeNodes);

            double nextLeafTime = leavesToBeAdded.isEmpty() ?
                    Double.POSITIVE_INFINITY : leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();

            if (k <= 1) {
                time = nextLeafTime;
                addArrivedLeaves(activeNodes, leavesToBeAdded, time);
                continue;
            }

            int segment = getIntervalAtTime(time);
            double segmentEndTime = (segment + 1 < nEpochs) ?
                    rateShifts.value()[segment + 1] : Double.POSITIVE_INFINITY;
            double horizon = Math.min(segmentEndTime, nextLeafTime);

            computeLogNeAndSlope(segment, time, a, b);

            double bestTau = Double.POSITIVE_INFINITY;
            int bestPop = -1, bestToPop = -1;

            // Coalescent events per deme.
            for (int i = 0; i < nDemes; i++) {
                int n_i = activeNodes.get(i).size();
                if (n_i < 2) continue;
                double A = (n_i * (n_i - 1.0) / 2.0) * Math.exp(-a[i]);
                double B = -b[i];
                double tau = sampleWaitingTime(A, B);
                if (tau < bestTau) { bestTau = tau; bestPop = i; bestToPop = i; }
            }

            // Migration events between each ordered deme pair (source-major).
            int mIdx = 0;
            for (int i = 0; i < nDemes; i++) {
                for (int j = 0; j < nDemes; j++) {
                    if (i == j) continue;
                    int n_i = activeNodes.get(i).size();
                    double m_ij = mVals[mIdx++];
                    if (n_i == 0) continue;
                    double A = n_i * m_ij * Math.exp(a[j] - a[i]);
                    double B = b[j] - b[i];
                    double tau = sampleWaitingTime(A, B);
                    if (tau < bestTau) { bestTau = tau; bestPop = i; bestToPop = j; }
                }
            }

            double eventTime = time + bestTau;

            if (eventTime >= horizon) {
                time = horizon;
                addArrivedLeaves(activeNodes, leavesToBeAdded, time);
                continue;
            }

            if (bestPop == bestToPop) {
                // Coalescent
                TimeTreeNode node1 = selectRandomNode(activeNodes.get(bestPop));
                TimeTreeNode node2 = selectRandomNode(activeNodes.get(bestPop));
                TimeTreeNode parent = new TimeTreeNode((String) null, tree);
                parent.setIndex(nodeNumber);
                parent.setAge(eventTime);
                parent.setMetaData(populationLabel, bestPop);
                parent.addChild(node1);
                parent.addChild(node2);
                activeNodes.get(bestPop).add(parent);
            } else {
                // Migration
                TimeTreeNode migrant = selectRandomNode(activeNodes.get(bestPop));
                TimeTreeNode migrantsParent = new TimeTreeNode((String) null, tree);
                migrantsParent.setIndex(nodeNumber);
                migrantsParent.setAge(eventTime);
                migrantsParent.setMetaData(populationLabel, bestToPop);
                migrantsParent.addChild(migrant);
                activeNodes.get(bestToPop).add(migrantsParent);
            }
            time = eventTime;
            nodeNumber++;
            addArrivedLeaves(activeNodes, leavesToBeAdded, time);
        }

        for (List<TimeTreeNode> nodeList : activeNodes) {
            if (!nodeList.isEmpty()) {
                return nodeList.get(0);
            }
        }
        throw new RuntimeException("No root node found!");
    }

    /**
     * Fill {@code a[d] = logNe[d](time)} and {@code b[d] = d/dt logNe[d]} for
     * the linear segment containing {@code time}. In the tail segment (after
     * the last knot) the slope is zero and log-Ne is held at the last knot.
     */
    private void computeLogNeAndSlope(int segment, double time, double[] a, double[] b) {
        Double[][] logNeVal = logNe.value();
        Double[] times = rateShifts.value();
        boolean tail = (segment + 1 >= times.length);
        if (tail) {
            for (int d = 0; d < nDemes; d++) {
                a[d] = logNeVal[d][times.length - 1];
                b[d] = 0.0;
            }
            return;
        }
        double segStart = times[segment];
        double segLen = times[segment + 1] - segStart;
        double frac = (time - segStart) / segLen;
        for (int d = 0; d < nDemes; d++) {
            double left = logNeVal[d][segment];
            double right = logNeVal[d][segment + 1];
            a[d] = left + frac * (right - left);
            b[d] = (right - left) / segLen;
        }
    }

    /**
     * Exact inverse-CDF draw of the waiting time for a hazard
     * {@code h(τ) = A · exp(B · τ)}. With {@code u ~ Exp(1)}:
     * <ul>
     *   <li>{@code B = 0}: τ = u / A (standard exponential).</li>
     *   <li>{@code B ≠ 0}: τ = log(1 + u·B/A) / B, defined when 1 + u·B/A > 0.
     *       If the argument is non-positive the integrated hazard saturates
     *       before reaching u (can only happen when B < 0, i.e. hazard
     *       decreases to 0 fast enough that H(∞) = -A/B is finite); return
     *       {@code +∞}.</li>
     * </ul>
     */
    private double sampleWaitingTime(double A, double B) {
        if (A <= 0.0) return Double.POSITIVE_INFINITY;
        double u = -Math.log(random.nextDouble());
        if (B == 0.0) return u / A;
        double arg = 1.0 + u * B / A;
        if (arg <= 0.0) return Double.POSITIVE_INFINITY;
        return Math.log(arg) / B;
    }

    private void addArrivedLeaves(List<List<TimeTreeNode>> activeNodes,
                                  List<TimeTreeNode> leavesToBeAdded, double time) {
        while (!leavesToBeAdded.isEmpty() &&
                leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() <= time) {
            TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
            activeNodes.get((Integer) youngest.getMetaData(populationLabel)).add(youngest);
        }
    }

/**
     * Build rate matrix for a given epoch: Ne on the diagonal (from exp(logNe)),
     * migration rates off-diagonal (constant over time, from flat M).
     */
    private Double[][] buildRateMatrix(int epoch) {
        Double[][] matrix = new Double[nDemes][nDemes];
        Double[][] logNeVal = logNe.value();
        Double[] mVals = M.value();

        for (int i = 0; i < nDemes; i++) {
            matrix[i][i] = Math.exp(logNeVal[i][epoch]);
        }

        int mIndex = 0;
        for (int i = 0; i < nDemes; i++) {
            for (int j = 0; j < nDemes; j++) {
                if (i != j) {
                    matrix[i][j] = mVals[mIndex];
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

    private void sanitiseDemeNames(TimeTree tree) {
        for (TimeTreeNode node : tree.getNodes()) {
            Object demeIndex = node.getMetaData(populationLabel);
            if (demeIndex instanceof Integer) {
                String demeName = uniqueDemes.get((Integer) demeIndex);
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
        return Double.NaN;
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> params = super.getParams();
        params.put(logNeParamName, logNe);
        params.put(MParamName, M);
        params.put(rateShiftsParamName, rateShifts);
        params.put(demesParamName, demes);
        if (interpolation != null) params.put(interpolationParamName, interpolation);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case logNeParamName -> logNe = value;
            case MParamName -> M = value;
            case rateShiftsParamName -> rateShifts = value;
            case demesParamName -> demes = value;
            case interpolationParamName -> {
                interpolation = value;
                isLinear = value != null && INTERP_LINEAR.equals(((Value<String>) value).value());
            }
            default -> super.setParam(paramName, value);
        }
    }

    public Value<Double[][]> getLogNe() { return logNe; }
    public Value<Double[]> getM() { return M; }
    public Value<Double[]> getRateShifts() { return rateShifts; }
    public Value<Object[]> getDemes() { return demes; }
    public Value<String> getInterpolation() { return interpolation; }

    public List<String> getUniqueDemes() { return uniqueDemes; }
    public int getNDemes() { return nDemes; }
    public int getNEpochs() { return nEpochs; }
    public boolean isLinearInterpolation() { return isLinear; }

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
