package lphy.graphicalModel;

/**
 * The group of {@link GenerativeDistribution} or {@link Func}
 * has the same or similar return type.
 * @author Walter Xie
 */
public enum GeneratorCategory {
    DATA_TYPE, RATE_MATRIX,
    COAL_TREE, BIRTH_DEATH_TREE, PROB_DIST, STOCHASTIC_PROCESS, NONE,
    ALL // last element is only for GUI
}
