package lphy.base.evolution.tree;

import lphy.base.distributions.DistributionConstants;
import lphy.base.evolution.EvolutionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.util.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Centralized shared code for dealing with taxa-conditioned tree generative distributions.
 */
public abstract class TaxaConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {

    public static final String taxaParamName = EvolutionConstants.taxaParamName;
    public static final String agesParamName = "ages";

    /**
     * A value holding the number of taxa.
     */
    protected Value<Integer> n;

    /**
     * A value holding the taxa, either as a class implementing Taxa interface, or as an Object[]
     */
    protected Value taxaValue;

    /**
     * A value holding the taxa ages, only permitted if taxaValue is not specified
     */
    protected Value<Double[]> ages;

    private Taxa taxa;
    private boolean taxaConstructed = false;

    /**
     * Make sure to use this random generator in all child classes
     */
    protected RandomGenerator random;

    /**
     * Init parameters and get random generator
     */
    public TaxaConditionedTreeGenerator(Value<Integer> n, Value taxaValue, Value<Double[]> ages) {
        this.n = n;
        this.taxaValue = taxaValue;
        this.ages = ages;

        this.random = RandomUtils.getRandom();
    }

    /**
     * Tests whether the values n and taxa are valid.
     *
     * @param atLeastOneRequired
     */
    protected void checkTaxaParameters(boolean atLeastOneRequired) {
        if (atLeastOneRequired && taxaValue == null && n == null && ages == null) {
            throw new IllegalArgumentException("At least one of " + DistributionConstants.nParamName + ", " + taxaParamName + ", " + agesParamName + " must be specified.");
        }

        if (taxaValue != null && n != null) {
            if (getTaxa().ntaxa() != n.value()) {
                throw new IllegalArgumentException(DistributionConstants.nParamName + " and " + taxaParamName + " values are incompatible.");
            }
        }

        if (ages != null && n != null) {
            if (ages.value().length != n.value()) {
                throw new IllegalArgumentException(DistributionConstants.nParamName + " and " + agesParamName + " values are incompatible.");
            }
        }

        if (ages != null && taxaValue != null) {
            throw new IllegalArgumentException("Only one of " + taxaParamName + " and " + agesParamName + " may be specified.");
        }
    }

    private void constructTaxa() {

        if (taxaValue == null) {
            if (ages != null) {
                // create taxa from ages
                taxa = Taxa.createTaxa(ages.value());
            } else {
                taxa = Taxa.createTaxa(n());
            }
        } else if (taxaValue.value() instanceof Taxa) {
            taxa = (Taxa)taxaValue.value();
        } else if (taxaValue.value().getClass().isArray()) {
            if (taxaValue.value() instanceof Taxon[]) {
                taxa = Taxa.createTaxa((Taxon[]) taxaValue.value());
            } else {
                taxa = Taxa.createTaxa((Object[]) taxaValue.value());
            }
        } else {
            throw new IllegalArgumentException(taxaParamName + " must be of type Object[] or Taxa, but it is type " + taxaValue.value().getClass());
        }
        taxaConstructed = true;
    }

    public Taxa getTaxa() {
        if (!taxaConstructed) {
            constructTaxa();
        }
        return taxa;
    }

    protected int n() {
        if (n != null) return n.value();
        return getTaxa().ntaxa();
    }

    /**
     * @param tree     the tree these nodes are being constructed for.
     * @param nodeList the list to add the created leaf nodes to.
     */
    protected void createLeafNodes(TimeTree tree, List<TimeTreeNode> nodeList) {

        if (!taxaConstructed) constructTaxa();
        String[] names = taxa.getTaxaNames();
        Double[] ages = taxa.getAges();

        for (int i = 0; i < names.length; i++) {
            TimeTreeNode node = new TimeTreeNode(names[i], tree);
            node.setAge(ages[i]);
            node.setLeafIndex(i);
            nodeList.add(node);
        }
    }

    protected List<TimeTreeNode> createLeafTaxa(TimeTree tree) {
        List<TimeTreeNode> leafNodes = new ArrayList<>();
        createLeafNodes(tree, leafNodes);
        return leafNodes;
    }

    protected TimeTreeNode drawRandomNode(List<TimeTreeNode> nodeList) {
        return nodeList.remove(random.nextInt(nodeList.size()));
    }

    protected TimeTreeNode drawRandomNodeWithReplacement(List<TimeTreeNode> nodeList) {
        return nodeList.get(random.nextInt(nodeList.size()));
    }

    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (n != null) map.put(DistributionConstants.nParamName, n);
        if (taxaValue != null) map.put(taxaParamName, taxaValue);
        if (ages != null) map.put(agesParamName, ages);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case DistributionConstants.nParamName:
                n = value;
                break;
            case taxaParamName:
                taxaValue = value;
                break;
            case agesParamName:
                ages = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
        constructTaxa();
    }

    public String toString() {
        return getName();
    }
}
