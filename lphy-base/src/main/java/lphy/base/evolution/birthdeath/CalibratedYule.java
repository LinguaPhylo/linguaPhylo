package lphy.base.evolution.birthdeath;

import lphy.base.distribution.DistributionConstants;
import lphy.base.distribution.Exp;
import lphy.base.distribution.UniformDiscrete;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.Taxon;
import lphy.base.evolution.tree.TaxaConditionedTreeGenerator;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.*;

@Citation(value = "Heled, J., & Drummond, A. J. (2012). Calibrated tree priors for relaxed phylogenetics and divergence time estimation. Systematic biology, 61(1), 138–149. https://doi.org/10.1093/sysbio/syr087",
        title = "Calibrated tree priors for relaxed phylogenetics and divergence time estimation",
        DOI = "10.1093/sysbio/syr087", authors = {"Heled","Drummond"}, year = 2012)

public class CalibratedYule extends TaxaConditionedTreeGenerator implements GenerativeDistribution<TimeTree> {
    Value<Number> rootAge;
    Value<Number> birthRate;
    Value<Number[]> cladeMRCAAge;
    Value cladeTaxaValue;
    Value otherTaxa;
    Taxa taxa;
    Taxa[] cladeTaxaArray;
    List<TimeTreeNode> activeNodes;
    List<TimeTreeNode> inactiveNodes;
    public static final String cladeMRCAAgeName = "cladeMRCAAge";
    public static final String cladeTaxaName = "cladeTaxa";
    public static final String otherTaxaName = "otherTaxa";

    // TODO: make cladeTaxa and cladeMRCAAge take the same type of data
    public CalibratedYule(@ParameterInfo(name = BirthDeathConstants.lambdaParamName, description = "per-lineage birth rate, possibly scaled to mutations or calendar units.") Value<Number> birthRate,
                          @ParameterInfo(name = DistributionConstants.nParamName, description = "the total number of taxa.", optional = true) Value<Integer> n,
                          @ParameterInfo(name = cladeTaxaName, description = "a string array of taxa id or a taxa object for clade taxa (e.g. dataframe, alignment or tree)") Value cladeTaxa,
                          @ParameterInfo(name = cladeMRCAAgeName, description = "an array of ages for clade most recent common ancestor, ages should be correspond with clade taxa array.") Value<Number[]> cladeMRCAAge,
                          @ParameterInfo(name = otherTaxaName, description = "a string array of taxa id or a taxa object for other taxa (e.g. dataframe, alignment or tree)", optional = true) Value otherTaxa,
                          @ParameterInfo(name = BirthDeathConstants.rootAgeParamName, description = "the root age to be conditioned on optional.", optional = true) Value<Number> rootAge){
        super(n, null, null);
        if (cladeTaxa == null) throw new IllegalArgumentException("The clade taxa shouldn't be null!");
        if (cladeMRCAAge == null) throw new IllegalArgumentException("The clade mrca age shouldn't be null!");
        if (cladeMRCAAge.value().length > 1)
            LoggerUtils.log.warning("LphyBeast for CalibratedYule with more than one calibration is currently not supported.");
        if (n == null && otherTaxa == null) {
            throw new IllegalArgumentException("At least one of " + DistributionConstants.nParamName + ", " + otherTaxaName + " must be specified.");
        }

        for (int i = 0; i < cladeMRCAAge.value().length; i++){
            if (rootAge != null && rootAge.value().doubleValue() <= cladeMRCAAge.value()[i].doubleValue()){
                throw new IllegalArgumentException("The clade MARCA age shouldn't be smaller than the tree root age!");
            }
        }

        this.cladeTaxaValue = cladeTaxa;
        this.cladeMRCAAge = cladeMRCAAge;
        this.otherTaxa = otherTaxa;
        this.rootAge = rootAge;
        this.birthRate = birthRate;

        if (otherTaxa == null) {
            activeNodes = new ArrayList<>(n() - getTaxaLength(cladeTaxa));
        } else {
            activeNodes = new ArrayList<>(getTaxaLength(otherTaxa));
        }
        inactiveNodes = new ArrayList<>();
    }

    /**
     * Get the number of given taxa
     * @param taxa
     * @return the number of taxa
     */
    private int getTaxaLength(Value taxa) {
        int nTaxa = 0;
        if (taxa.value() instanceof Taxa) {
            nTaxa = ((Taxa) taxa.value()).length();
        } else if (taxa.value().getClass().isArray()) {
            nTaxa = ((Object[]) taxa.value()).length;
        } else {
            throw new IllegalArgumentException("Taxa must be of type Object[] or Taxa!");
        }
        return nTaxa;
    }

    /**
     * Construct taxa and initialise the tree first. Fill in active and inactive nodes list to do coalesce. Build the tree at last.
     * @return calibrated yule tree
     */
    @GeneratorInfo(name = "CalibratedYule",
            category = GeneratorCategory.BD_TREE, examples = {"calibratedYule.lphy"},
            description = "The CalibratedYule method accepts one or more clade taxa and generates a tip-labelled time tree. If a root age is provided, the method conditions the tree generation on this root age.")
    @Override
    public RandomVariable<TimeTree> sample() {
        // construct the clade taxa first
        constructCladeTaxa();

        //adding other taxa to active node list, must come after constructCladeTaxa() call
        constructOtherTaxa();

        Number[] cladeMRCAAge = getCladeMRCAAge().value();

        System.out.println(getCladeTaxaArray());
        System.out.println(getCladeTaxaArray().length);
        System.out.println(cladeMRCAAge.length);
        // do another check after constructing clade taxa
        if (getCladeTaxaArray().length != cladeMRCAAge.length) throw new IllegalArgumentException("The number of clade mrca age should be the same as clade taxa number!");

        // initialise a new tree
        TimeTree tree = new TimeTree();

        // get active nodes names
        List<String> activeNodeNames = new ArrayList<>();
        for (TimeTreeNode node : activeNodes){
            activeNodeNames.add(node.getId());
        }

        // get inactive nodes names and change repeat names
        for (int i = 0; i < getCladeTaxaArray().length; i++) {
            // generate the clade tree
            TimeTree cladeTree = getCladeTree(cladeMRCAAge[i], cladeTaxaArray[i]);
            // add the root node to inactiveNodes
            inactiveNodes.add(cladeTree.getRoot());

            // check repeat names
            List<String> leafNames = List.of(cladeTree.getRoot().getLeafNames());
            // prepare for checking active nodes names
            boolean hasRepeatName = leafNames.stream().anyMatch(activeNodeNames::contains);

            // prepare for checking inactive node leaf nodes names
            Set<String> allCladeNames = new HashSet<>();
            boolean hasRepeatCladeNames = false;

            for (Taxa cladeTaxa : getCladeTaxaArray()) {
                for (Taxon taxon : cladeTaxa.getTaxonArray()) {
                    if (!allCladeNames.add(taxon.getName())) {
                        hasRepeatCladeNames = true;
                        break;
                    }
                }
                if (hasRepeatCladeNames) {
                    break;
                }
            }

            // if there are repeat names, change all leaf node names for the clade
            if (hasRepeatName || hasRepeatCladeNames){
                List<TimeTreeNode> leafNodes = cladeTree.getLeafNodes();
                for (int j = 0; j<leafNames.size(); j++){
                    if (getCladeTaxaArray().length == 1){
                        String newName = "clade_" + leafNames.get(j);
                        leafNodes.get(j).setId(newName);
                    } else {
                        String newName = "clade" + i + "_" + leafNames.get(j);
                        leafNodes.get(j).setId(newName);
                    }
                }
            }
        }

        // get a certain lambda for Yule coalescent
        double lambda = (double) getBirthRate().value();

        // coalescent
        double t = 0.0;
        coalesce(lambda, t);

        // set root to construct the tree
        if (tree != null) {
            tree.setRoot(activeNodes.get(0), true);
        }

        // specify the root age if given
        if (rootAge != null){
            Number rootAgeValue = getRootAge().value();
            if (rootAgeValue instanceof Double) {
                tree.getRoot().setAge((double) rootAgeValue);
            } else {
                // handle other number types if necessary
                tree.getRoot().setAge(rootAgeValue.doubleValue());
            }
        }

        return new RandomVariable<>("calibratedYuleTree", tree, this);
    }

    /**
     * Do coalesce process.
     * After coalesce all nodes in activeNodes, if time is still smaller than the youngest node in inactiveNodes,
     * then force time to be same as the youngest node age.
     * @param lambda
     * @param t
     */
    private void coalesce(double lambda, double t) {
        while (activeNodes.size() > 1){
            // sample t with exp distribution
            double mean = activeNodes.size() * lambda;
            Value<Number> meanValue = new Value<>("mean", mean);
            Exp exp = new Exp(meanValue);

            t += exp.sample().value();

            if (inactiveNodes.size() != 0) {
                if (t >= getYoungestNode(inactiveNodes).getAge()) { // count clade root to coalesce
                    t = getYoungestNode(inactiveNodes).getAge();
                    // add cladeRoot to the candidate list if it's not exist in it
                    if (!activeNodes.contains(getYoungestNode(inactiveNodes))) {
                        activeNodes.add(getYoungestNode(inactiveNodes));
                        inactiveNodes.remove(getYoungestNode(inactiveNodes));
                    }

                    coalesceNodes(activeNodes, t);
                } else { // do not count clade root to coalesce
                    coalesceNodes(activeNodes, t);
                    // if there is only one node in activeNodes and is not from inactiveNodes
                    // then add the youngest node from inactiveNodes to activeNodes
                    if (activeNodes.size() == 1 && !activeNodes.contains(getYoungestNode(inactiveNodes))) {
                        t = getYoungestNode(inactiveNodes).getAge();
                        activeNodes.add(getYoungestNode(inactiveNodes));
                        inactiveNodes.remove(getYoungestNode(inactiveNodes));
                    }
                }
            } else coalesceNodes(activeNodes, t);
        }
    }

    /**
     * Get a Yule tree for each clade taxa.
     * @param cladeMRCAAge
     * @param taxa
     * @return the Yule tree
     */
    private TimeTree getCladeTree(Number cladeMRCAAge, Taxa taxa) {
        Value<Integer> cladeLengthValue = new Value<> (null, taxa.length());
        Value<Number> cladeMRCAAgeValue = new Value<>(null, cladeMRCAAge);
        Value<Taxa> taxaValue = new Value<>(null, taxa);
        Yule yuleInstance = new Yule(getBirthRate(), cladeLengthValue, taxaValue, cladeMRCAAgeValue);

        return yuleInstance.sample().value();
    }

    /**
     * Get the youngest node in the node list.
     * @param inactiveNodes a list of nodes
     * @return the node with the smallest age
     */
    private TimeTreeNode getYoungestNode(List<TimeTreeNode> inactiveNodes) {
        TimeTreeNode tempNode = inactiveNodes.get(0);
        double age = tempNode.getAge();
        for (TimeTreeNode node : inactiveNodes){
            if (age > node.getAge()){
                tempNode = node;
                age = node.getAge();
            }
        }
        return tempNode;
    }

    /**
     * Construct clade taxa and build cladeTaxaArray
     */
    public void constructCladeTaxa() {
        Object cladeTaxaValueObject = getCladeTaxa().value();

        if (cladeTaxaValueObject instanceof Taxa) {
            cladeTaxaArray = new Taxa[] {(Taxa) cladeTaxaValueObject};
        } else if (cladeTaxaValueObject.getClass().isArray()) {
            Object[] array = (Object[]) cladeTaxaValueObject;
            Object sample = array[0];
            if (sample instanceof Taxa) {
                cladeTaxaArray = new Taxa[array.length];
                for (int i = 0; i < array.length; i++) {
                    cladeTaxaArray[i] = (Taxa) array[i];
                }
            } else if (sample instanceof Taxon) {
                cladeTaxaArray = new Taxa[]{Taxa.createTaxa((Taxon[]) cladeTaxaValueObject)};
            } else if (sample instanceof Taxon[]) {
                Taxon[][] taxonArray = (Taxon[][]) cladeTaxaValueObject;
                cladeTaxaArray = new Taxa[taxonArray.length];

                for (int i = 0; i < taxonArray.length; i++) {
                    Taxon[] innerArray = taxonArray[i];
                    cladeTaxaArray[i] = Taxa.createTaxa(innerArray);
                }
            } else if (sample instanceof Object[]) {
                Object[][] objectArray = (Object[][]) cladeTaxaValueObject;
                cladeTaxaArray = new Taxa[objectArray.length];
                for (int i = 0; i < objectArray.length; i++) {
                    cladeTaxaArray[i] = Taxa.createTaxa(objectArray[i]);
                }
            } else {
                cladeTaxaArray = new Taxa[]{Taxa.createTaxa((Object[]) cladeTaxaValueObject)};
            }
        } else {
            throw new IllegalArgumentException(taxaParamName + " must be of type Object[], Taxa, or Taxa[], but it is type " + cladeTaxaValueObject.getClass());
        }
    }

    /**
     * Construct other taxa if given and build constructOtherTaxa
     */
    private void constructOtherTaxa() {
        if (getOtherTaxa() == null) {

            int totalCladeTaxaLength = 0;
            for (Taxa taxa : cladeTaxaArray) {
                totalCladeTaxaLength += taxa.length();
            }
            taxa = Taxa.createTaxa(n() - totalCladeTaxaLength);
            mapActiveNodes();
        } else {
            if (getOtherTaxa().value() instanceof Taxa) {
                taxa = (Taxa) getOtherTaxa().value();
                mapActiveNodes();
            } else if (getOtherTaxa().value().getClass().isArray()) {
                if (getOtherTaxa().value() instanceof Taxon[]) {
                    taxa = Taxa.createTaxa((Taxon[]) getOtherTaxa().value());
                    mapActiveNodes();
                } else {
                    taxa = Taxa.createTaxa((Object[]) getOtherTaxa().value());
                    mapActiveNodes();
                }
            } else {
                throw new IllegalArgumentException(taxaParamName + " must be of type Object[] or Taxa, but it is type " + getOtherTaxa().value().getClass());
            }
        }
    }

    private void mapActiveNodes() {
        TimeTreeNode[] nodes = new TimeTreeNode[taxa.ntaxa()];
        for (int i = 0; i<nodes.length; i++) {
            nodes[i] = new TimeTreeNode(taxa.getTaxon(i), null);
            activeNodes.add(nodes[i]);
        }
    }

    /**
     * Coalesce two given nodes at time t
     * @param activeNodes
     * @param t
     */
    private static void coalesceNodes(List<TimeTreeNode> activeNodes, double t) {
        // random two nodes to coalesceT
        List<TimeTreeNode> nodes = randomTwoNodes(activeNodes);

        TimeTreeNode node1 = nodes.get(0);
        TimeTreeNode node2 = nodes.get(1);

        // create the parent node
        TimeTreeNode parentNode = new TimeTreeNode(t);
        parentNode.addChild(node1);
        parentNode.addChild(node2);
        node1.setParent(parentNode);
        node2.setParent(parentNode);

        // remove coalesced nodes from the candidate list and add parent
        activeNodes.remove(node1);
        activeNodes.remove(node2);
        activeNodes.add(parentNode);
    }

    // public for unit test
    /**
     * Randomly draw two different nodes in activeNodes
     * @param activeNodes
     * @return list of two nodes
     */
    public static List<TimeTreeNode> randomTwoNodes(List<TimeTreeNode> activeNodes) {
        // get node1
        TimeTreeNode node1 = randomNode(activeNodes);

        // get a new list without node1
        List<TimeTreeNode> copyList = new ArrayList<>(activeNodes);
        copyList.remove(node1);

        // get node2
        TimeTreeNode node2 = randomNode(copyList);

        // create the random result list
        List<TimeTreeNode> randomNodes = new ArrayList<>(2);
        randomNodes.add(node1);
        randomNodes.add(node2);

        return randomNodes;
    }

    /**
     * Randomly draw a node in activeNodes
     * @param nodeList
     * @return list of one node
     */
    private static TimeTreeNode randomNode(List<TimeTreeNode> nodeList) {
        // create uniform discrete instance
        Value<Integer> lower = new Value<>("low", 0);
        Value<Integer> upper = new Value<>("high", nodeList.size()-1);
        UniformDiscrete uniformDiscrete = new UniformDiscrete(lower, upper);
        // random an index
        RandomVariable<Integer> index = uniformDiscrete.sample();
        return nodeList.get(index.value());
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> map = super.getParams();
        map.put(BirthDeathConstants.lambdaParamName, birthRate);
        map.put(cladeMRCAAgeName, cladeMRCAAge);
        map.put(cladeTaxaName, cladeTaxaValue);
        if (rootAge != null) map.put(BirthDeathConstants.rootAgeParamName, rootAge);
        if (otherTaxa != null) map.put(otherTaxaName,otherTaxa);
        return map;
    }

    public void setParam(String paramName, Value value){
        if (paramName.equals(BirthDeathConstants.lambdaParamName)) birthRate = value;
        else if (paramName.equals(BirthDeathConstants.rootAgeParamName)) rootAge = value;
        else if (paramName.equals(cladeTaxaName)) cladeTaxaValue = value;
        else if (paramName.equals(cladeMRCAAgeName)) cladeMRCAAge = value;
        else if (paramName.equals(otherTaxaName)) otherTaxa = value;
        else super.setParam(paramName, value);
    }

    public Value<Number> getBirthRate(){
        return getParams().get(BirthDeathConstants.lambdaParamName);
    }
    public Value getCladeTaxa(){
        return getParams().get(cladeTaxaName);
    }
    public Taxa[] getCladeTaxaArray(){
        return cladeTaxaArray;
    }
    public Value<Number[]> getCladeMRCAAge(){
        return getParams().get(cladeMRCAAgeName);
    }
    public Value getOtherTaxa(){
        return getParams().get(otherTaxaName);
    }
    public Value<Number> getRootAge(){
        return getParams().get(BirthDeathConstants.rootAgeParamName);
    }
}