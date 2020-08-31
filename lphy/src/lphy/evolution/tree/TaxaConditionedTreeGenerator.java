package lphy.evolution.tree;

import lphy.evolution.Taxa;
import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * Centralized shared code for dealing with taxa-conditioned tree generative distributions.
 */
public abstract class TaxaConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {

    protected String nParamName;
    protected String taxaParamName;

    protected Value<Integer> n;
    protected Value taxa;

    protected RandomGenerator random;

    public TaxaConditionedTreeGenerator(Value<Integer> n, Value taxa) {
        this.n = n;
        this.taxa = taxa;
    }

    protected void checkTaxaParameters(boolean atLeastOneRequired) {
        if (atLeastOneRequired && taxa == null && n == null) {
            throw new IllegalArgumentException("At least one of " + nParamName + ", " + taxaParamName + " must be specified.");
        }

        if (taxa != null && n != null) {
            if (taxaLength(taxa) != n.value()) {
                throw new IllegalArgumentException(nParamName + " and " + taxaParamName + " are incompatible.");
            }
        }
    }

    protected String[] taxaNames() {
        String[] taxaNames = new String[n()];
        if (taxa == null) {
            for (int i = 0; i < taxaNames.length; i++) {
                taxaNames[i] = i + "";
            }
            return taxaNames;
        } else if (taxa.value().getClass().isArray()) {
            Object[] taxaObjects = (Object[])taxa.value();
            for (int i = 0; i < taxaNames.length; i++) {
                taxaNames[i] = taxaObjects[i].toString();
            }
            return taxaNames;
        } else if (taxa.value() instanceof Taxa) {
            return ((Taxa) taxa.value()).getTaxa();
        }
        throw new IllegalArgumentException(taxaParamName + " must be of type Object[] or Taxa, but it is type " + taxa.value().getClass());
    }

    /**
     * @return true if either an n or a taxa value available.
     */
    protected boolean hasTaxa() {
        return n != null || taxa != null;
    }

    protected int n() {
        if (n != null) return n.value();
        return taxaLength(taxa);
    }

    protected int taxaLength(Value taxa) {
        if (taxa == null) throw new IllegalArgumentException("No taxa available");
        if (taxa.value() instanceof Object[]) return ((Object[]) taxa.value()).length;
        if (taxa.value() instanceof Taxa) return ((Taxa) taxa.value()).ntaxa();
        throw new IllegalArgumentException(taxaParamName + " must be of type String[] or Taxa.");
    }

    /**
     * @param tree the tree these nodes are being constructed for.
     * @param nodeList the list to add the created leaf nodes to.
     */
    protected void createLeafNodes(TimeTree tree, List<TimeTreeNode> nodeList) {
        String[] taxaNames = null;
        if (taxa != null) taxaNames = taxaNames();
        int ntaxa = n();

        for (int i = 0; i < ntaxa; i++) {
            TimeTreeNode node = new TimeTreeNode(taxa == null ? i+"" : taxaNames[i], tree);
            node.setLeafIndex(i);
            nodeList.add(node);
        }
    }

    protected TimeTreeNode drawRandomNode(List<TimeTreeNode> nodeList) {
        return nodeList.remove(random.nextInt(nodeList.size()));
    }

    public SortedMap<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (n != null) map.put(nParamName, n);
        if (taxa != null) map.put(taxaParamName, taxa);
        return map;
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (paramName.equals(nParamName)) n = value;
        else if (paramName.equals(taxaParamName)) taxa = value;
        else throw new RuntimeException("Unrecognised parameter name: " + paramName);
    }

    public String toString() {
        return getName();
    }
}
