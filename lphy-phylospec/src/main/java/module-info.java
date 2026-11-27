/**
 * @author Walter Xie
 */
module lphy.phylospec {
    requires transitive lphy.base;
    requires org.phylospec.core;

    exports lphy.phylospec.convert;
    // picocli sets the runner's option fields by reflection
    opens lphy.phylospec.convert to info.picocli;
    exports lphy.phylospec.export;
}
