package lphy.core.narrative;

/**
 * Sections in narrative
 */
public enum Section {

    // Do not change the order,
    // otherwise it will break NarrativeCreator
    Code("Code"),
    Data("Data"),
    Model("Model"),
    Posterior("Posterior"),
    GraphicalModel("Graphical Model"),
    References("References");

    public String name;

    Section(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
