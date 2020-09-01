package lphy.evolution.coalescent;

import lphy.core.distributions.Utils;
import lphy.evolution.TaxaAges;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Kingman coalescent tree generative distribution for serially sampled data
 */
public class SerialCoalescent extends TaxaConditionedTreeGenerator {

    private final String thetaParamName;
    private final String agesParamName;
    private Value<Number> theta;
    private Value<Double[]> ages;

    public SerialCoalescent(@ParameterInfo(name = "theta", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Number> theta,
                             @ParameterInfo(name = "n", description = "number of taxa.", optional = true) Value<Integer> n,
                             @ParameterInfo(name = "taxaAges", description = "TaxaAges object, (e.g. TaxaAges or TimeTree)", optional = true) Value<TaxaAges> taxaAges,
                             @ParameterInfo(name = "ages", description = "an array of leaf node ages.", optional = true) Value<Double[]> ages) {

        super(n, taxaAges);

        this.theta = theta;
        this.ages = ages;
        this.random = Utils.getRandom();

        thetaParamName = getParamName(0);
        nParamName = getParamName(1);
        taxaParamName = getParamName(2);
        agesParamName = getParamName(3);

        int c = (ages == null ? 0 : 1) + (taxaAges == null ? 0 : 1) + (n == null ? 0 : 1);

        if (c > 1) {
            throw new IllegalArgumentException("One one of " + nParamName + ", " + agesParamName + " and " + taxaParamName + " may be specified in " + getName());
        }
        super.checkTaxaParameters(false);
        checkDimensions();
    }

    protected int n() {
        if (n != null) return n.value();
        if (ages != null) return ages.value().length;
        return taxaLength(taxa);
    }

    private void checkDimensions() {
        boolean success = true;
        if (n != null && n.value() != n()) {
            success = false;
        }
        if (ages != null && ages.value().length != n()) {
            success = false;
        }
        if (!success) {
            throw new IllegalArgumentException("The number of theta values must be exactly one less than the number of taxa!");
        }
    }

    @GeneratorInfo(name = "Coalescent", description = "The Kingman coalescent with serially sampled data. (Rodrigo and Felsenstein, 1999)")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

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

        double popSize = doubleValue(theta);
        int groupIndex = 0;
        int countWithinGroup = 0;
        while ((activeNodes.size() + leavesToBeAdded.size()) > 1) {
            int k = activeNodes.size();

            if (k == 1) {
                time = leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge();
            } else {
                // draw next time;
                double rate = (k * (k - 1.0)) / (popSize * 2.0);
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
                }
            }

            while (leavesToBeAdded.size() > 0 && leavesToBeAdded.get(leavesToBeAdded.size() - 1).getAge() == time) {
                TimeTreeNode youngest = leavesToBeAdded.remove(leavesToBeAdded.size() - 1);
                activeNodes.add(youngest);
            }
        }

        tree.setRoot(activeNodes.get(0));

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private List<TimeTreeNode> createLeafTaxa(TimeTree tree) {
        List<TimeTreeNode> leafNodes = new ArrayList<>();

        if (ages != null) {

            Double[] leafAges = ages.value();

            for (int i = 0; i < leafAges.length; i++) {
                TimeTreeNode node = new TimeTreeNode(i + "", tree);
                node.setAge(leafAges[i]);
                leafNodes.add(node);
            }
            return leafNodes;

        } else if (taxa != null) {

            TaxaAges taxaAges = (TaxaAges)taxa.value();
            String[] taxaNames = taxaAges.getTaxa();
            Double[] ages = taxaAges.getAges();

            for (int i = 0; i < taxaNames.length; i++) {
                TimeTreeNode node = new TimeTreeNode(taxaNames[i], tree);
                node.setAge(ages[i]);
                leafNodes.add(node);
            }
            return leafNodes;

        } else {
            for (int i = 0; i < n(); i++) {
                TimeTreeNode node = new TimeTreeNode(i + "", tree);
                node.setAge(0.0);
                leafNodes.add(node);
            }
            return leafNodes;
        }
    }

    @Override
    public double logDensity(TimeTree timeTree) {

        // TODO!

        return 0.0;
    }

    @Override
    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = super.getParams();
        map.put(thetaParamName, theta);
        if (n != null) map.put(nParamName, n);
        if (ages != null) map.put(agesParamName, ages);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else if (paramName.equals(agesParamName)) ages = value;
        else super.setParam(paramName, value);
    }

    public Value<Number> getTheta() {
        return theta;
    }

}
