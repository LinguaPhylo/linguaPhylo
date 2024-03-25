package lphy.base.evolution.tree;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.evolution.EvolutionConstants;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;


public class RandomSample extends ParametricDistribution<TimeTree> {
    Value<TimeTree> tree;
    Value<String[]> taxaName;
    Value<Double[]> sampleFraction;
    // use the random generator in this class
    protected RandomGenerator random;

    public static final String taxaParamName = EvolutionConstants.taxaParamName;
    public static final String sampleFractionPara = "sampleFraction";
    public static final String treeParamName = "tree";

    public RandomSample(
            @ParameterInfo(name = treeParamName, narrativeName = "full tree", description = "the full tree to extract taxa from.") Value<TimeTree> tree,
            @ParameterInfo(name = taxaParamName, narrativeName = "taxa names", description = "the two taxa names that the function would sample") Value<String[]> taxaName,
            @ParameterInfo(name = sampleFractionPara, narrativeName = "fraction of sampling", description = "the two fractions that the function sample in the taxa") Value<Double[]> sampleFraction){
        if (tree == null) throw new IllegalArgumentException("The original tree cannot be null");
        setParam(treeParamName, tree);
        setParam(taxaParamName, taxaName);
        this.sampleFraction = sampleFraction;
        this.tree = tree;
        this.taxaName = taxaName;
        this.random = RandomUtils.getRandom();
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "RandomSample", description = "Generate the randomly sampled tree with given two sample fractions" +
            "and two clade taxa names within the given tree.")
    @Override
    public RandomVariable<TimeTree> sample() {
        Value<TimeTree> tree = getParams().get(treeParamName);
        Value<String[]> taxaName = getParams().get(taxaParamName);
        Value<Double[]> sampleFraction = getParams().get(sampleFractionPara);
        TimeTree treeValue = tree.value();

        // obtain tumour and normal taxa names
        String[] tumourName = new String[]{taxaName.value()[0]};
        String[] normalName = new String[]{taxaName.value()[1]};

        // obtain tumour and normal sample fractions
        double tumourFraction = sampleFraction.value()[0];
        double normalFraction = sampleFraction.value()[1];

        // randomly pick the taxa names
        String[] sampledTumour = getSampleResult(tumourFraction, tumourName);
        String[] sampledNormal = getSampleResult(normalFraction, normalName);

        // construct taxa for both tumour and normal cells
        Taxa sampledTumourTaxa = constructTaxa(treeValue, sampledTumour);
        Taxa sampledNormalTaxa = constructTaxa(treeValue,sampledNormal);

        // combine them to one taxon
        Taxa newTaxa = combineTwoTaxa(sampledTumourTaxa, sampledNormalTaxa);

        // create the tree
        TimeTree newTree = new TimeTree(newTaxa);

        return new RandomVariable<>(null, newTree, this);
    }

    private static Taxa combineTwoTaxa(Taxa taxa1, Taxa taxa2) {
        // calculate combined number
        int totalLength = taxa1.ntaxa() + taxa2.ntaxa();

        // create a new taxon array to store the new taxon
        Taxa[] mergedTaxa = new Taxa[totalLength];

        // do copying
        for (int i = 0; i < taxa1.ntaxa(); i++) {
            mergedTaxa[i] = (Taxa) taxa1.getTaxon(i);
        }

        for (int i = 0; i < taxa2.ntaxa(); i++) {
            mergedTaxa[i + taxa1.ntaxa()] = (Taxa) taxa2.getTaxon(i);
        }
        return Taxa.createTaxa(mergedTaxa);
    }

    private static Taxa constructTaxa(TimeTree treeValue, String[] sampledNames) {
        // construct taxa
        Taxa allTaxa = Taxa.createTaxa(new Taxa[]{treeValue.getTaxa()});
        Taxon[] sampledCellsList = new Taxon[sampledNames.length];
        for (int i = 0; i < sampledNames.length; i++){
            for (int j = 0; j < allTaxa.length(); j++){
                if (sampledNames[i] == allTaxa.getTaxaNames()[j]){
                    sampledCellsList[i] = new Taxon(sampledNames[i], allTaxa.getAges()[j]);
                }
            }
        }
        // create taxa for sampled tumour cells
        Taxa sampledTaxa = Taxa.createTaxa(sampledNames);
        return sampledTaxa;
    }

    // public for unit test
    public String[] getSampleResult(double fraction, String[] name) {
        // calculate the num of taxa names to get
        int sampleNumber = (int)Math.round(fraction * name.length);
        // create a list to write result in
        List<String> sampleResult = new ArrayList<>();
        while (sampleResult.size() < sampleNumber){
            int index;
            do{
                index = random.nextInt(name.length); // get a random index
            } while (sampleResult.contains(name[index])); // check the index is a new one

            sampleResult.add(name[index]); // add the name to the result list
        }

        return sampleResult.toArray(new String[0]);
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (sampleFraction != null) map.put(String.valueOf(sampleFraction), sampleFraction);
        if (tree != null) map.put(treeParamName, tree);
        if (taxaName != null) map.put(taxaParamName, taxaName);
        return map;
    }
}
