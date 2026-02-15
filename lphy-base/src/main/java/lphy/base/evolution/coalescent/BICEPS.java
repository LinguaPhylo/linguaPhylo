package lphy.base.evolution.coalescent;

import lphy.base.distribution.DistributionConstants;
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
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.special.Gamma;

import java.util.*;

import static lphy.base.evolution.coalescent.SkylineCoalescent.groupSizesParamName;

/**
 * BICEPS (Bayesian Integrated Coalescent Epoch PlotS) tree prior.
 * Divides the coalescent tree into K epochs with independent InverseGamma-distributed
 * population sizes, providing an analytically integrated (marginal) log-density.
 */
@Citation(value = "Bouckaert, R. (2022). An Efficient Coalescent Epoch Model for Bayesian Phylogenetic Inference. " +
        "Systematic Biology, 71(6), 1549-1560.",
        title = "An Efficient Coalescent Epoch Model for Bayesian Phylogenetic Inference",
        year = 2022, authors = {"Bouckaert"}, DOI = "10.1093/sysbio/syac015")
public class BICEPS extends TaxaConditionedTreeGenerator {

    public static final String populationShapeParamName = "populationShape";
    public static final String populationMeanParamName = "populationMean";
    public static final String ploidyParamName = "ploidy";

    private Value<Double> populationShape;
    private Value<Double> populationMean;
    private Value<Integer[]> groupSizes;
    private Value<Double> ploidy;

    public BICEPS(@ParameterInfo(name = populationShapeParamName, narrativeName = "population size prior shape",
                    description = "Shape (alpha) of InverseGamma prior on population sizes. Must be > 1.")
                  Value<Double> populationShape,
                  @ParameterInfo(name = populationMeanParamName, narrativeName = "population size prior mean",
                    description = "Mean of InverseGamma prior on population sizes. Scale beta = populationMean * (alpha - 1).")
                  Value<Double> populationMean,
                  @ParameterInfo(name = groupSizesParamName, narrativeName = "group sizes",
                    description = "Coalescent events per epoch (present to past). The sum determines n-1.")
                  Value<Integer[]> groupSizes,
                  @ParameterInfo(name = ploidyParamName, narrativeName = "ploidy",
                    description = "Gene copy number per individual (default 2.0 for diploid).", optional = true)
                  Value<Double> ploidy,
                  @ParameterInfo(name = DistributionConstants.nParamName, description = "number of taxa.", optional = true)
                  Value<Integer> n,
                  @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "Taxa object, (e.g. Taxa or Object[])", optional = true)
                  Value<Taxa> taxa,
                  @ParameterInfo(name = TaxaConditionedTreeGenerator.agesParamName, description = "an array of leaf node ages.", optional = true)
                  Value<Double[]> ages) {

        super(n, taxa, ages);

        this.populationShape = populationShape;
        this.populationMean = populationMean;
        this.groupSizes = groupSizes;
        this.ploidy = ploidy;

        if (populationShape.value() <= 1.0) {
            throw new IllegalArgumentException("populationShape (alpha) must be > 1 for InverseGamma to have a finite mean.");
        }

        int c = (ages == null ? 0 : 1) + (taxa == null ? 0 : 1) + (n == null ? 0 : 1);
        if (c > 1) {
            throw new IllegalArgumentException("Only one of " + DistributionConstants.nParamName + ", " +
                    TaxaConditionedTreeGenerator.agesParamName + " and " + TaxaConditionedTreeGenerator.taxaParamName +
                    " may be specified in " + getName());
        }

        super.checkTaxaParameters(false);
        checkDimensions();
    }

    private void checkDimensions() {
        if (n != null && n.value() != n()) {
            throw new IllegalArgumentException("The n parameter is inconsistent with groupSizes (sum + 1 = " + n() + ").");
        }
        if (ages != null && ages.value().length != n()) {
            throw new IllegalArgumentException("The ages array length is inconsistent with groupSizes.");
        }
    }

    @Override
    protected int n() {
        int sum = 0;
        for (int groupSize : groupSizes.value()) {
            sum += groupSize;
        }
        return sum + 1;
    }

    private double getPloidy() {
        return ploidy != null ? ploidy.value() : 2.0;
    }

    private double sampleInverseGamma(double alpha, double beta) {
        GammaDistribution g = new GammaDistribution(random, alpha, 1.0 / beta,
                GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        return 1.0 / g.sample();
    }

    @GeneratorInfo(name = "BICEPS", verbClause = "has", narrativeName = "BICEPS coalescent prior",
            category = GeneratorCategory.COAL_TREE,
            examples = {"biceps.lphy"},
            description = "The BICEPS (Bayesian Integrated Coalescent Epoch PlotS) tree prior. " +
                    "Divides the tree into epochs with independent InverseGamma-distributed population sizes.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree(getTaxa());

        List<TimeTreeNode> leafNodes = createLeafTaxa(tree);
        List<TimeTreeNode> activeNodes = new ArrayList<>();
        List<TimeTreeNode> leavesToBeAdded = new ArrayList<>();

        double time = 0.0;

        for (TimeTreeNode leaf : leafNodes) {
            if (leaf.getAge() <= time) {
                activeNodes.add(leaf);
            } else {
                leavesToBeAdded.add(leaf);
            }
        }

        leavesToBeAdded.sort(
                (o1, o2) -> Double.compare(o2.getAge(), o1.getAge())); // REVERSE ORDER - youngest age at end of list

        double alpha = populationShape.value();
        double beta = populationMean.value() * (alpha - 1.0);
        double ploidyVal = getPloidy();

        Integer[] gs = groupSizes.value();
        int groupIndex = 0;
        int countWithinGroup = 0;

        // Draw initial population size for first epoch
        double popSize = sampleInverseGamma(alpha, beta);

        while ((activeNodes.size() + leavesToBeAdded.size()) > 1) {
            int k = activeNodes.size();

            if (k == 1) {
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else {
                // draw next coalescent time
                double rate = (k * (k - 1.0)) / (2.0 * ploidyVal * popSize);
                double x = -Math.log(random.nextDouble()) / rate;
                time += x;

                if (leavesToBeAdded.size() > 0 && time > leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge()) {
                    time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
                } else {
                    // do coalescence
                    TimeTreeNode a = activeNodes.remove(random.nextInt(activeNodes.size()));
                    TimeTreeNode b = activeNodes.remove(random.nextInt(activeNodes.size()));

                    TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
                    activeNodes.add(parent);

                    if (countWithinGroup == (gs[groupIndex] - 1)) {
                        groupIndex += 1;
                        countWithinGroup = 0;
                        // Draw new population size for next epoch
                        if (groupIndex < gs.length) {
                            popSize = sampleInverseGamma(alpha, beta);
                        }
                    } else {
                        countWithinGroup += 1;
                    }
                }
            }

            while (leavesToBeAdded.size() > 0 && leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() == time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
                activeNodes.add(youngest);
            }
        }

        tree.setRoot(activeNodes.get(0));

        if (groupSizes != null && (countWithinGroup != 0 || groupIndex != gs.length)) {
            throw new AssertionError("Programmer error in indexing the groupSizes array during simulation. " +
                    countWithinGroup + " " + groupIndex + " " + Arrays.toString(gs));
        }

        return new RandomVariable<>("\u03C8", tree, this);
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        double alpha = populationShape.value();
        double beta = populationMean.value() * (alpha - 1.0);
        double ploidyVal = getPloidy();
        Integer[] gs = groupSizes.value();

        // Collect all node ages and classify as leaf or coalescent
        List<double[]> events = new ArrayList<>(); // [age, type] where type: 0=leaf, 1=coalescent
        for (TimeTreeNode node : timeTree.getNodes()) {
            if (node.isLeaf()) {
                events.add(new double[]{node.getAge(), 0});
            } else {
                events.add(new double[]{node.getAge(), 1});
            }
        }

        // Sort by age, with leaves before coalescences at the same time
        events.sort((a, b) -> {
            int cmp = Double.compare(a[0], b[0]);
            if (cmp != 0) return cmp;
            return Double.compare(a[1], b[1]); // leaves (0) before coalescences (1)
        });

        // Walk forward in time, tracking lineage count and accumulating partial gamma
        int k = 0; // current lineage count
        double prevTime = 0.0;
        double partialGamma = 0.0;
        int coalCount = 0; // coalescent events within current epoch
        int groupIndex = 0;
        double logP = 0.0;

        for (double[] event : events) {
            double eventTime = event[0];
            boolean isCoalescent = event[1] > 0.5;

            // Add interval contribution
            if (eventTime > prevTime && k >= 2) {
                partialGamma += (eventTime - prevTime) * k * (k - 1) / 2.0;
            }
            prevTime = eventTime;

            if (isCoalescent) {
                k -= 1;
                coalCount += 1;

                // Check if we've reached an epoch boundary
                if (coalCount == gs[groupIndex]) {
                    logP += epochLogDensity(alpha, beta, ploidyVal, coalCount, partialGamma);
                    partialGamma = 0.0;
                    coalCount = 0;
                    groupIndex += 1;
                }
            } else {
                // Leaf event: lineage count increases
                k += 1;
            }
        }

        return logP;
    }

    /**
     * Compute the per-epoch analytically integrated log-density (marginal over population size).
     *
     * logP = -(alpha + eventCount) * log(beta + partialGamma / ploidy)
     *      + alpha * log(beta)
     *      - eventCount * log(ploidy)
     *      + logGamma(alpha + eventCount) - logGamma(alpha)
     */
    private double epochLogDensity(double alpha, double beta, double ploidy,
                                   int eventCount, double partialGamma) {
        return -(alpha + eventCount) * Math.log(beta + partialGamma / ploidy)
                + alpha * Math.log(beta)
                - eventCount * Math.log(ploidy)
                + Gamma.logGamma(alpha + eventCount) - Gamma.logGamma(alpha);
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(populationShapeParamName, populationShape);
        map.put(populationMeanParamName, populationMean);
        map.put(groupSizesParamName, groupSizes);
        if (ploidy != null) map.put(ploidyParamName, ploidy);
        if (n != null) map.put(DistributionConstants.nParamName, n);
        if (ages != null) map.put(TaxaConditionedTreeGenerator.agesParamName, ages);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case populationShapeParamName:
                populationShape = value;
                break;
            case populationMeanParamName:
                populationMean = value;
                break;
            case groupSizesParamName:
                groupSizes = value;
                break;
            case ploidyParamName:
                ploidy = value;
                break;
            case TaxaConditionedTreeGenerator.agesParamName:
                ages = value;
                break;
            default:
                super.setParam(paramName, value);
                break;
        }
    }

    public Value<Double> getPopulationShape() {
        return populationShape;
    }

    public Value<Double> getPopulationMean() {
        return populationMean;
    }

    public Value<Integer[]> getGroupSizes() {
        return groupSizes;
    }

    public Value<Double> getPloidy2() {
        return ploidy;
    }
}
