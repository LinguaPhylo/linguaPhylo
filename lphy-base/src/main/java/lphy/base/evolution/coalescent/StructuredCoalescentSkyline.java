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
 * Structured coalescent with piecewise per-deme effective population sizes
 * (a Skyline on log-Ne) and either time-constant or time-varying migration rates.
 * Counterpart of Mascot's StructuredSkyline / StructuredMigrationSkyline.
 *
 * <h2>Deme Ordering Contract</h2>
 *
 * <p>Demes are <b>always sorted alphabetically</b> to ensure consistency with BEAST2/MASCOT.
 * This determines how {@code logNe}, {@code M}, and {@code logM} are indexed.</p>
 *
 * <h3>logNe layout</h3>
 * <p>{@code logNe[i][e]} is the natural log of effective population size of deme {@code i}
 * (alphabetical) during Ne epoch {@code e} (epoch 0 = most recent).
 * Shape: {@code [nDemes][nEpochs]}.</p>
 *
 * <h3>Migration layouts</h3>
 * <p>Constant mode ({@code M}): flat array of length {@code nDemes * (nDemes - 1)}.
 * Order: for source deme {@code i} (alphabetical), for dest deme {@code j != i}
 * (alphabetical):</p>
 * <pre>
 * M = [ m_0→1, m_0→2, ..., m_1→0, m_1→2, ..., m_2→0, m_2→1, ... ]
 * </pre>
 *
 * <p>Time-varying mode ({@code logM}): shape {@code [nDemes*(nDemes-1)][nMigEpochs]}.
 * Row index follows the same source-major order as {@code M}; column index is the
 * migration epoch (epoch 0 = most recent). Values are log migration rates.</p>
 *
 * <h3>Rate shift times</h3>
 * <p>{@code rateShifts[e]} is the start of Ne epoch {@code e}, going backward from the
 * present. Length equals the number of Ne epochs. Strictly increasing; first element
 * typically {@code 0.0}. Epoch {@code e} covers {@code [rateShifts[e], rateShifts[e+1])};
 * the last epoch extends to infinity. When {@code logM} is supplied,
 * {@code migrationRateShifts} plays the same role for migration epochs.</p>
 *
 * <h3>Parameter XOR</h3>
 * <p>Exactly one of {@code M} or {@code logM} must be supplied. If {@code logM} is
 * supplied then {@code migrationRateShifts} is required.</p>
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
    public static final String logMParamName = "logM";
    public static final String rateShiftsParamName = "rateShifts";
    public static final String migrationRateShiftsParamName = "migrationRateShifts";
    public static final String demesParamName = "demes";
    public static final String interpolationParamName = "interpolation";

    public static final String INTERP_CONSTANT = "constant";
    public static final String INTERP_LINEAR = "linear";

    private Value<Double[][]> logNe;
    private Value<Double[]> M;
    private Value<Double[][]> logM;
    private Value<Double[]> rateShifts;
    private Value<Double[]> migrationRateShifts;
    private Value<Object[]> demes;
    private Value<String> interpolation;

    private List<String> uniqueDemes;
    private int nDemes;
    private int nEpochs;
    private int nMigEpochs;
    private boolean isLinear;
    private boolean timeVaryingMigration;

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
                            "Rates are in units of expected migrants per generation backward in time. " +
                            "Exactly one of M or logM must be supplied.",
                    optional = true)
            Value<Double[]> M,

            @ParameterInfo(name = logMParamName,
                    description = "Log migration rates, time-varying. " +
                            "Shape [nDemes*(nDemes-1)][nMigEpochs]. Row index follows the same " +
                            "source-major order as M; column index is the migration epoch " +
                            "(epoch 0 = most recent). Requires migrationRateShifts. " +
                            "Exactly one of M or logM must be supplied.",
                    optional = true)
            Value<Double[][]> logM,

            @ParameterInfo(name = rateShiftsParamName,
                    description = "Start times of each Ne epoch, going backward from the present. " +
                            "Length equals number of epochs. Strictly increasing; first element typically 0.0.")
            Value<Double[]> rateShifts,

            @ParameterInfo(name = migrationRateShiftsParamName,
                    description = "Start times of each migration epoch, going backward from the present. " +
                            "Length equals logM[*].length. Strictly increasing; first element typically 0.0. " +
                            "Required when logM is supplied.",
                    optional = true)
            Value<Double[]> migrationRateShifts,

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
        this.logM = logM;
        this.rateShifts = rateShifts;
        this.migrationRateShifts = migrationRateShifts;
        this.demes = demes;
        this.interpolation = interpolation;

        if (taxa == null)
            throw new IllegalArgumentException("taxa must be specified!");
        if (demes == null)
            throw new IllegalArgumentException("demes must be specified!");
        if (rateShifts == null)
            throw new IllegalArgumentException("rateShifts must be specified!");

        boolean hasM = (M != null);
        boolean hasLogM = (logM != null);
        if (hasM == hasLogM) {
            throw new IllegalArgumentException(
                    "Exactly one of M (constant migration) or logM (time-varying migration) must be supplied.");
        }
        this.timeVaryingMigration = hasLogM;
        if (hasLogM && migrationRateShifts == null) {
            throw new IllegalArgumentException(
                    "migrationRateShifts must be supplied when logM is used.");
        }

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
        nMigEpochs = timeVaryingMigration ? migrationRateShifts.value().length : 1;
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

        int expectedMPairs = nDemes * (nDemes - 1);
        if (timeVaryingMigration) {
            Double[][] logMVal = logM.value();
            if (logMVal.length != expectedMPairs) {
                throw new IllegalArgumentException(String.format(
                        "logM outer dimension mismatch: expected %d (nDemes * (nDemes - 1)), got %d",
                        expectedMPairs, logMVal.length));
            }
            for (int i = 0; i < expectedMPairs; i++) {
                if (logMVal[i].length != nMigEpochs) {
                    throw new IllegalArgumentException(String.format(
                            "logM[%d] length mismatch: expected %d (nMigEpochs), got %d",
                            i, nMigEpochs, logMVal[i].length));
                }
            }
            Double[] mTimes = migrationRateShifts.value();
            for (int i = 1; i < mTimes.length; i++) {
                if (mTimes[i] <= mTimes[i - 1]) {
                    throw new IllegalArgumentException(
                            "migrationRateShifts must be in strictly increasing order. Got: " + Arrays.toString(mTimes));
                }
            }
        } else {
            if (M.value().length != expectedMPairs) {
                throw new IllegalArgumentException(String.format(
                        "M array size mismatch: expected %d (nDemes * (nDemes - 1)), got %d",
                        expectedMPairs, M.value().length));
            }
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
        int currentMigInterval = 0;
        Double[][] rateMatrix = buildRateMatrix(currentInterval, currentMigInterval);

        double[][] rates = new double[nDemes][nDemes];
        double totalRate = StructuredCoalescentRateShifts.populateRateMatrix(activeNodes, rateMatrix, rates);

        while ((getTotalNodeCount(activeNodes) + leavesToBeAdded.size()) > 1) {
            int k = getTotalNodeCount(activeNodes);

            if (k == 1 && leavesToBeAdded.size() > 0) {
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else if (k > 1) {
                SCEvent event = selectRandomEvent(rates, totalRate, time);

                double nextNeShift = (currentInterval + 1 < nEpochs) ?
                        rateShifts.value()[currentInterval + 1] : Double.POSITIVE_INFINITY;
                double nextMigShift = (timeVaryingMigration && currentMigInterval + 1 < nMigEpochs) ?
                        migrationRateShifts.value()[currentMigInterval + 1] : Double.POSITIVE_INFINITY;
                double nextShift = Math.min(nextNeShift, nextMigShift);

                double nextLeafTime = leavesToBeAdded.isEmpty() ?
                        Double.POSITIVE_INFINITY : leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();

                if (nextShift < event.time && nextShift <= nextLeafTime) {
                    time = nextShift;
                    currentInterval = getIntervalAtTime(time);
                    currentMigInterval = getMigIntervalAtTime(time);
                    rateMatrix = buildRateMatrix(currentInterval, currentMigInterval);
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
            currentMigInterval = getMigIntervalAtTime(time);
            rateMatrix = buildRateMatrix(currentInterval, currentMigInterval);
            totalRate = StructuredCoalescentRateShifts.populateRateMatrix(activeNodes, rateMatrix, rates);
        }

        for (List<TimeTreeNode> nodeList : activeNodes) {
            if (!nodeList.isEmpty()) {
                return nodeList.get(0);
            }
        }
        throw new RuntimeException("No root node found!");
    }

    private int getMigIntervalAtTime(double time) {
        if (!timeVaryingMigration) return 0;
        Double[] times = migrationRateShifts.value();
        for (int i = times.length - 1; i >= 0; i--) {
            if (time >= times[i]) {
                return i;
            }
        }
        return 0;
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
        int nPairs = nDemes * (nDemes - 1);
        double[] a = new double[nDemes];
        double[] b = new double[nDemes];
        double[] mA = new double[nPairs];
        double[] mB = new double[nPairs];
        Double[] mVals = timeVaryingMigration ? null : M.value();

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
            double nextMigShift = Double.POSITIVE_INFINITY;
            if (timeVaryingMigration) {
                int migSegment = getMigIntervalAtTime(time);
                if (migSegment + 1 < nMigEpochs) {
                    nextMigShift = migrationRateShifts.value()[migSegment + 1];
                }
            }
            double horizon = Math.min(Math.min(segmentEndTime, nextMigShift), nextLeafTime);

            computeLogNeAndSlope(segment, time, a, b);
            if (timeVaryingMigration) {
                computeLogMAndSlope(time, mA, mB);
            }

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
                    int pair = mIdx++;
                    if (n_i == 0) continue;
                    double A, B;
                    if (timeVaryingMigration) {
                        A = n_i * Math.exp(mA[pair] + a[j] - a[i]);
                        B = mB[pair] + b[j] - b[i];
                    } else {
                        double m_ij = mVals[pair];
                        A = n_i * m_ij * Math.exp(a[j] - a[i]);
                        B = b[j] - b[i];
                    }
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
     * Fill {@code mA[p] = logM[p](time)} and {@code mB[p] = d/dt logM[p]} for
     * each ordered deme pair {@code p}, in the linear migration segment
     * containing {@code time}. Tail segment: slope zero, value held at last knot.
     */
    private void computeLogMAndSlope(double time, double[] mA, double[] mB) {
        Double[][] logMVal = logM.value();
        Double[] times = migrationRateShifts.value();
        int nPairs = logMVal.length;
        int segment = getMigIntervalAtTime(time);
        boolean tail = (segment + 1 >= times.length);
        if (tail) {
            for (int p = 0; p < nPairs; p++) {
                mA[p] = logMVal[p][times.length - 1];
                mB[p] = 0.0;
            }
            return;
        }
        double segStart = times[segment];
        double segLen = times[segment + 1] - segStart;
        double frac = (time - segStart) / segLen;
        for (int p = 0; p < nPairs; p++) {
            double left = logMVal[p][segment];
            double right = logMVal[p][segment + 1];
            mA[p] = left + frac * (right - left);
            mB[p] = (right - left) / segLen;
        }
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
     * Build rate matrix for the current Ne epoch and migration epoch: Ne on the
     * diagonal (from exp(logNe[deme][neEpoch])), migration rates off-diagonal —
     * either from the constant flat M, or from exp(logM[pair][migEpoch]) when
     * migration is time-varying.
     */
    private Double[][] buildRateMatrix(int neEpoch, int migEpoch) {
        Double[][] matrix = new Double[nDemes][nDemes];
        Double[][] logNeVal = logNe.value();

        for (int i = 0; i < nDemes; i++) {
            matrix[i][i] = Math.exp(logNeVal[i][neEpoch]);
        }

        if (timeVaryingMigration) {
            Double[][] logMVal = logM.value();
            int mIndex = 0;
            for (int i = 0; i < nDemes; i++) {
                for (int j = 0; j < nDemes; j++) {
                    if (i != j) {
                        matrix[i][j] = Math.exp(logMVal[mIndex][migEpoch]);
                        mIndex++;
                    }
                }
            }
        } else {
            Double[] mVals = M.value();
            int mIndex = 0;
            for (int i = 0; i < nDemes; i++) {
                for (int j = 0; j < nDemes; j++) {
                    if (i != j) {
                        matrix[i][j] = mVals[mIndex];
                        mIndex++;
                    }
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
        if (M != null) params.put(MParamName, M);
        if (logM != null) params.put(logMParamName, logM);
        params.put(rateShiftsParamName, rateShifts);
        if (migrationRateShifts != null) params.put(migrationRateShiftsParamName, migrationRateShifts);
        params.put(demesParamName, demes);
        if (interpolation != null) params.put(interpolationParamName, interpolation);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case logNeParamName -> logNe = value;
            case MParamName -> M = value;
            case logMParamName -> logM = value;
            case rateShiftsParamName -> rateShifts = value;
            case migrationRateShiftsParamName -> migrationRateShifts = value;
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
    public Value<Double[][]> getLogM() { return logM; }
    public Value<Double[]> getRateShifts() { return rateShifts; }
    public Value<Double[]> getMigrationRateShifts() { return migrationRateShifts; }
    public Value<Object[]> getDemes() { return demes; }
    public Value<String> getInterpolation() { return interpolation; }

    public List<String> getUniqueDemes() { return uniqueDemes; }
    public int getNDemes() { return nDemes; }
    public int getNEpochs() { return nEpochs; }
    public int getNMigEpochs() { return nMigEpochs; }
    public boolean isLinearInterpolation() { return isLinear; }
    public boolean isTimeVaryingMigration() { return timeVaryingMigration; }

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
