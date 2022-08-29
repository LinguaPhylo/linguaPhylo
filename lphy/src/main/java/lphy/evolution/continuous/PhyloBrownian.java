package lphy.evolution.continuous;

import lphy.evolution.Taxa;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.*;
import lphy.graphicalModel.types.DoubleValue;
import lphy.util.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.apache.commons.math3.distribution.NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Citation(
        value = "Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.",
        title = "Maximum-likelihood estimation of evolutionary trees from continuous characters",
        authors = {"Felsenstein"},
        year = 1973,
        DOI = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/"
)
public class PhyloBrownian implements GenerativeDistribution<ContinuousCharacterData> {

    Value<TimeTree> tree;
    protected Value<Double> diffusionRate;
    Value<Double> y0;
    RandomGenerator random;

    public static final String treeParamName = "tree";
    public static final String diffRateParamName = "diffRate";
    public static final String y0ParamName = "y0";

    public PhyloBrownian(@ParameterInfo(name = treeParamName, description = "the time tree.") Value<TimeTree> tree,
                         @ParameterInfo(name = diffRateParamName, narrativeName = "evolutionary rate", description = "the diffusion rate.") Value<Double> diffusionRate,
                         @ParameterInfo(name = y0ParamName, narrativeName = "root value", description = "the value of continuous trait at the root.") Value<Double> y0) {
        this.tree = tree;
        this.diffusionRate = diffusionRate;
        this.y0 = y0;
        this.random = RandomUtils.getRandom();
    }

    // constructor for subclasses that don't wish to call the above one, for example because arguments are reordered.
    PhyloBrownian() {
    }

    @Override
    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(treeParamName, tree);
            put(diffRateParamName, diffusionRate);
            put(y0ParamName, y0);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        switch (paramName) {
            case treeParamName:
                tree = value;
                break;
            case diffRateParamName:
                diffusionRate = value;
                break;
            case y0ParamName:
                y0 = value;
                break;
            default:
                throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }
    }

    @GeneratorInfo(name = "PhyloBrownian", verbClause = "is assumed to have evolved under",
            narrativeName = "phylogenetic Brownian motion process",
            category = GeneratorCategory.PHYLO_LIKELIHOOD, examples = {"simplePhyloBrownian.lphy"},
            description = "The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id." +
                    "(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)")
    public RandomVariable<ContinuousCharacterData> sample() {

        SortedMap<String, Integer> idMap = new TreeMap<>();
        fillIdMap(tree.value().getRoot(), idMap);

        Map<String, Double> tipValues = new TreeMap<>();

        traverseTree(tree.value().getRoot(), y0, tipValues, diffusionRate.value(), idMap);

        Taxa taxa = Taxa.createTaxa(tipValues.keySet().toArray());
        Double[][] values = new Double[taxa.ntaxa()][1];
        String[] names = taxa.getTaxaNames();
        for (int i = 0; i < names.length; i++) {
            values[i][0] = tipValues.get(names[i]);
        }

        ContinuousCharacterData continuousCharacterData = new ContinuousCharacterData(taxa, values);

        return new RandomVariable<>(null, continuousCharacterData, this);
    }

    public String getTypeName() {
        return "Continuous Character Data";
    }

    private void fillIdMap(TimeTreeNode node, SortedMap<String, Integer> idMap) {
        if (node.isLeaf()) {
            Integer i = idMap.get(node.getId());
            if (i == null) {
                int nextValue = 0;
                for (Integer j : idMap.values()) {
                    if (j >= nextValue) nextValue = j + 1;
                }
                idMap.put(node.getId(), nextValue);
            }
        } else {
            for (TimeTreeNode child : node.getChildren()) {
                fillIdMap(child, idMap);
            }
        }
    }

    private void traverseTree(TimeTreeNode node, Value<Double> nodeState, Map<String, Double> tipValues, double diffusionRate, Map<String, Integer> idMap) {
        if (node.isLeaf()) {
            tipValues.put(node.getId(), nodeState.value());
        } else {
            for (TimeTreeNode child : node.getChildren()) {

                double variance = diffusionRate * (node.getAge() - child.getAge());

                double newState = sampleNewState(nodeState.value(), variance, child.getIndex());

                traverseTree(child, new DoubleValue("x", newState), tipValues, diffusionRate, idMap);
            }
        }
    }

    protected double sampleNewState(double initialState, double time, int nodeIndex) {
        // use code available since apache math 3.1, see #215
        NormalDistribution distribution = new
                NormalDistribution(random, initialState, Math.sqrt(time * diffusionRate.value()),
                DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        return handleBoundaries(distribution.sample());
    }

    protected double handleBoundaries(double rawValue) {
        return rawValue;
    }
}
