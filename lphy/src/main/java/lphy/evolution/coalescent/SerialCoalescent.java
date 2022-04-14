package lphy.evolution.coalescent;

import lphy.core.distributions.Utils;
import lphy.evolution.Taxa;
import lphy.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lphy.core.distributions.DistributionConstants.nParamName;
import static lphy.evolution.coalescent.CoalescentConstants.thetaParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * A Kingman coalescent tree generative distribution for serially sampled data
 */
//@Citation(value = "Kingman JFC. The Coalescent. Stochastic Processes and their Applications 13, 235–248 (1982)",
//        title = "The Coalescent",
//        year = 1982,
//        authors = {"Kingman"},
//        DOI = "https://doi.org/10.1016/0304-4149(82)90011-4")
@Citation(value = "Rodrigo AG, Felsenstein J. (1999). Coalescent Approaches to HIV Population Genetics, " +
        "The Evolution of HIV, Chapter 8, edited by Crandall K., Johns Hopkins Univ. Press, Baltimore.",
        title = "Coalescent Approaches to HIV Population Genetics",
        authors = {"Rodrigo", "Felsenstein"}, year = 1999, ISBN = "0801861519")
public class SerialCoalescent extends TaxaConditionedTreeGenerator {

    private Value<Number> theta;

    public SerialCoalescent(@ParameterInfo(name = thetaParamName, narrativeName = "coalescent parameter", description = "effective population size, possibly scaled to mutations or calendar units.") Value<Number> theta,
                            @ParameterInfo(name = nParamName, description = "number of taxa.", optional = true) Value<Integer> n,
                            @ParameterInfo(name = taxaParamName, description = "Taxa object, (e.g. Taxa or TimeTree or Object[])", optional = true) Value<Taxa> taxa,
                            @ParameterInfo(name = agesParamName, description = "an array of leaf node ages.", optional = true) Value<Double[]> ages) {

        super(n, taxa, ages);

        this.theta = theta;
        this.ages = ages;
        this.random = Utils.getRandom();

        super.checkTaxaParameters(true);
        checkDimensions();
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

    @GeneratorInfo(name = "Coalescent",
            narrativeName = "Kingman's coalescent tree prior",
            description = "The Kingman coalescent with serially sampled data. (Rodrigo and Felsenstein, 1999)")
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

        leavesToBeAdded.sort((o1, o2) -> Double.compare(o2.getAge(), o1.getAge())); // REVERSE ORDER - youngest age at end of list

        double popSize = doubleValue(theta);
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

    @Override
    public double logDensity(TimeTree timeTree) {

        // TODO!

        return 0.0;
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(thetaParamName, theta);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(thetaParamName)) theta = value;
        else super.setParam(paramName, value);
    }

    public Value<Number> getTheta() {
        return theta;
    }
}
