package lphy.base.evolution.birthdeath;

import lphy.base.evolution.continuous.PhyloBrownian;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.function.GeneralLinearFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.system.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.muParamName;
import static lphy.base.evolution.birthdeath.BirthDeathConstants.originAgeParamName;

/**
 * A birth-death tree with birth rate driven by a GLM.
 */
public class GLMBirthDeathTree implements GenerativeDistribution<TimeTree> {

    public static final String x0ParamName = "x0";
    private Value<Number[]> beta;
    private Value<Number[]> x0;
    private Value<Number[]> diffRate;
    private Value<Number> deathRate;

    private Value<Number> originAge;

    RandomGenerator random;

    public GLMBirthDeathTree(@ParameterInfo(name = GeneralLinearFunction.betaParamName, narrativeName = "beta", description = "the coefficients of the general linear model driving the (log) birth rate.") Value<Number[]> beta,
                             @ParameterInfo(name = x0ParamName, narrativeName = "x0", description = "the initial values of the traits that drive the birth rate at the origin of the process.") Value<Number[]> x0,
                             @ParameterInfo(name = PhyloBrownian.diffRateParamName, description = "the variance of the underlying Brownian process for each trait.") Value<Number[]> diffRate,
                             @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                             @ParameterInfo(name = originAgeParamName, description = "the age of the origin.") Value<Number> originAge) {

        this.beta = beta;
        this.x0 = x0;
        this.diffRate = diffRate;
        this.deathRate = deathRate;
        this.originAge = originAge;
        this.random = RandomUtils.getRandom();
    }


    @GeneratorInfo(name = "GLMBirthDeathTree",
            category = GeneratorCategory.BD_TREE,
            description = "A full birth death tree driven by continuous trait evolution.<br>" +
                    "Conditioned on root age.")
    public RandomVariable<TimeTree> sample() {

        int steps = 1000;

        double t = ValueUtils.doubleValue(originAge);
        double dt = t / steps;

        TimeTreeNode originNode = new TimeTreeNode(t);

        double[] x = ValueUtils.doubleArrayValue(x0);

        originNode.setMetaData("x", x);

        double[] b = ValueUtils.doubleArrayValue(beta);

        double[] diff = ValueUtils.doubleArrayValue(diffRate);

        double mu = ValueUtils.doubleValue(deathRate);

        NormalDistribution[] diffusions = new NormalDistribution[diff.length];
        for (int i = 0; i < diffusions.length; i++) {
            diffusions[i] = new NormalDistribution(0.0, Math.sqrt(diff[i] * diff[i] * dt));
        }

        List<TimeTreeNode> activeNodes = new ArrayList<>();
        activeNodes.add(originNode);
        for (int i = 0; i < steps; i++) {

            List<TimeTreeNode> newNodes = new ArrayList<>();
            List<TimeTreeNode> nodesToRemove = new ArrayList<>();

            for (TimeTreeNode node : activeNodes) {
                double birthRate = birthRate(b, node);

                // update time of every node
                node.setAge(t + dt);

                // update traits of every active node
                node.setMetaData("x", brownian((double[])node.getMetaData("x"), diffusions));

                // probability that a birth or death happened in this interval
                double p = Math.exp(-(birthRate+mu)*dt);
                if (random.nextDouble() <= p) {

                    // birth or death happened, so mark node for removal from active node list
                    nodesToRemove.add(node);

                    // check if it was a birth or a death
                    if (random.nextDouble() < (birthRate/(birthRate+mu))) {
                        // birth happened.
                        // children start at same time and with same metadata as parent.
                        // parent removed from active list.
                        // slight improvement would be to set time from the conditional distribution of an event occurring within dt.

                        TimeTreeNode leftChild = new TimeTreeNode(t + dt);
                        leftChild.setMetaData("x", node.getMetaData("x"));
                        TimeTreeNode rightChild = new TimeTreeNode(t + dt);
                        rightChild.setMetaData("x", node.getMetaData("x"));
                        node.addChild(leftChild);
                        node.addChild(rightChild);
                        nodesToRemove.add(node);
                        newNodes.add(leftChild);
                        newNodes.add(rightChild);
                    } // else {
                        // death happened
                        // nothing more to do as node already marked for removal from active node list
                    // }
                }
            }
            activeNodes.removeAll(nodesToRemove);
            activeNodes.addAll(newNodes);
            t += dt;
        }

        TimeTree tree = new TimeTree();
        tree.setRoot(originNode);

        return new RandomVariable<>("\u03C8", tree, this);
    }

    private double birthRate(double[] beta, TimeTreeNode node) {
        return birthRate(beta, (double[])node.getMetaData("x"));
    }

    private double birthRate(double[] beta, double[] traits) {
        double x = 0;
        for (int i = 0; i < traits.length; i++) {
            x += beta[i] * traits[i];
        }
        return Math.log(x);
    }

    private double[] brownian(double[] x, NormalDistribution[] diffusions) {
        double[] xnew = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            xnew[i] = x[i] + diffusions[i].sample();
        }
        return xnew;
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(GeneralLinearFunction.betaParamName, beta);
            put(x0ParamName, x0);
            put(PhyloBrownian.diffRateParamName, diffRate);
            put(muParamName, deathRate);
            put(originAgeParamName, originAge);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case GeneralLinearFunction.betaParamName -> beta = value;
            case x0ParamName -> x0 = value;
            case PhyloBrownian.diffRateParamName -> diffRate = value;
            case muParamName -> deathRate = value;
            case originAgeParamName -> originAge = value;
            default -> throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    public String toString() {
        return getName();
    }
}
