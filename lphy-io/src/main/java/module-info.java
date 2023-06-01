/**
 * @author Walter Xie
 */
module lphy.io {
    requires transitive lphy.base;

    requires info.picocli;

//    opens lphy.io.simulator;

    exports lphy.io.logger;
    exports lphy.io.simulator;

}