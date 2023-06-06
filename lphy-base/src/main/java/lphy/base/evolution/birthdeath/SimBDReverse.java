package lphy.base.evolution.birthdeath;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.math.MathUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.ValueUtils;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.List;
import java.util.Map;

import static lphy.base.evolution.birthdeath.BirthDeathConstants.*;

/**
 * A java implementation of https://rdrr.io/cran/TreeSim/src/R/sim2.bd.reverse.single.R
 */
public class SimBDReverse extends TaxaConditionedTreeGenerator {

    private Value<Number> birthRate;
    private Value<Number> deathRate;
    private Value<Number> rhoVal;
    
    public SimBDReverse(@ParameterInfo(name = lambdaParamName, description = "per-lineage birth rate.") Value<Number> birthRate,
                        @ParameterInfo(name = muParamName, description = "per-lineage death rate.") Value<Number> deathRate,
                        @ParameterInfo(name = TaxaConditionedTreeGenerator.taxaParamName, description = "The extant taxa that this process are conditioned on") Value<Taxa> taxa,
                        @ParameterInfo(name = rhoParamName, description = "The fraction of total extant species that the conditioned-on taxa represent. " +
                                "The resulting tree will have taxa.ntaxa()/rho total extant taxa.") Value<Number> rho) {

        super(null, taxa, null);

        this.birthRate = birthRate;
        this.deathRate = deathRate;
        this.rhoVal = rho;

        // Taxa to condition on must all be extant!
        if (!taxa.value().isUltrametric() || taxa.value().getTaxon(0).getAge() != 0.0) {
            throw new IllegalArgumentException("Taxa to condition on must all be extant!");
        }
    }


    @GeneratorInfo(name = "SimBDReverse",
            category = GeneratorCategory.BD_TREE, examples = {"simFossils.lphy"},
            description = "A complete birth-death tree with both extant and extinct species.<br>" +
            "Conditioned on (a fraction of) extant taxa.")
    public RandomVariable<TimeTree> sample() {

        TimeTree tree = new TimeTree();

        double lambda = ValueUtils.doubleValue(birthRate);
        double mu = ValueUtils.doubleValue(deathRate);
        double rho = ValueUtils.doubleValue(rhoVal);

        List<TimeTreeNode> activeNodes = createLeafTaxa(tree);

        int maxleaf = (int) Math.round(activeNodes.size()/rho);

        while (activeNodes.size() < maxleaf) {
            activeNodes.add(new TimeTreeNode(0.0));
        }

        TimeTreeNode rootNode = null;
        TimeTreeNode originNode = null;

        double time = 0.0;

        while (activeNodes.size() > 0) {

            double timestep = MathUtils.nextExponential(activeNodes.size()*(lambda+mu), random);

            time += timestep;

            double specevent = random.nextDouble();

            if (lambda/(lambda+mu) >= specevent) { // speciation

                if (activeNodes.size() > 1) { // do speciation backwards (i.e. coalescent event)

                    TimeTreeNode a = drawRandomNode(activeNodes);
                    TimeTreeNode b = drawRandomNode(activeNodes);

                    TimeTreeNode parent = new TimeTreeNode(time, new TimeTreeNode[]{a, b});
                    activeNodes.add(parent);

                } else { // last speciation event back in time, so this is the origin
                    rootNode = activeNodes.remove(0);
                    originNode = new TimeTreeNode(time, new TimeTreeNode[] {rootNode});

                    if (activeNodes.size() != 0) throw new AssertionError();
                }
            } else { // extinction back in time, means adding a new anonymous tip at this time.
                activeNodes.add(new TimeTreeNode(time));
            }
        }

        tree.setRoot(originNode, true);
        
        return new RandomVariable<>(null, tree, this);
    }

    @Override
    public double logDensity(TimeTree timeTree) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> params = super.getParams();
        params.put(lambdaParamName, birthRate);
        params.put(muParamName, deathRate);
        params.put(rhoParamName, rhoVal);
        return params;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(lambdaParamName)) birthRate = value;
        else if (paramName.equals(muParamName)) deathRate = value;
        else if (paramName.equals(rhoParamName)) rhoVal = value;
        else super.setParam(paramName, value);
    }

    public String toString() {
        return getName();
    }
}
