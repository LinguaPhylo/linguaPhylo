package lphy.graphicalModel;

/**
 * The group of {@link GenerativeDistribution} or {@link Func}
 * has the same or similar return type.
 * @author Walter Xie
 */
public enum GeneratorCategory {
    ALL("All","All categories"),
    SEQU_TYPE("Sequence type","Data type of sequences, e.g. nucleotides, amino acid, binary"),
    RATE_MATRIX("Rate matrix","Instantaneous rate matrix"),
    COAL_TREE("Coalescent tree","Coalescent tree prior"),
    BD_TREE("Birth-death tree","Birth-death tree prior"),
    PROB_DIST("Probability distribution","Prior probability distribution"),
    STOCHASTIC_PROCESS("Stochastic process","Such as continuous-time Markov chain (CTMC) and Brownian motion"),
    TAXA_ALIGNMENT("Taxa & Alignment","Taxa and alignment"),
    NONE("None","Unknown category"); // last element is only for GUI

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
