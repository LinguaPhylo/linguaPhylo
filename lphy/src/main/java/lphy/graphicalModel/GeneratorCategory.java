package lphy.graphicalModel;

/**
 * The group of {@link GenerativeDistribution} or {@link Func}
 * play the same or similar roles in the Bayesian phylogenetic frame.
 * @author Walter Xie
 */
public enum GeneratorCategory {
    ALL("All","All models or functions"),
    SEQU_TYPE("Sequence type","Data types of sequences, e.g. nucleotides, amino acid, binary"),
    TAXA_ALIGNMENT("Taxa & Alignment","Taxa and alignments"),
    RATE_MATRIX("Rate matrix","Instantaneous rate matrix"),
    SITE_MODEL("Site model",
            "Wrapper objects of Q matrix, site rates before invariable, the proportion of invariable sites"),
    COAL_TREE("Coalescent tree","Coalescent tree priors"),
    BD_TREE("Birth-death tree","Birth-death tree priors"),
    TREE("Tree functions","Functions related to the tree"),
    PROB_DIST("Probability distribution","Prior probability distributions"),
    STOCHASTIC_PROCESS("Stochastic process",
            "Stochastic Processes, e.g. continuous-time Markov chain (CTMC) and Brownian motion"),
    MODEL_AVE_SEL("\"True\" model","Model averaging or model selection"),
    NONE("None","Utils or unclassified models"); // last element is only for GUI

    private String name;
    private String description;

    GeneratorCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
