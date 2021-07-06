

/**
 * @author Walter Xie
 */
module lphy {
    requires transitive java.datatransfer;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive java.logging;

    requires transitive org.antlr.antlr4.runtime;
    requires transitive org.apache.commons.lang3;
    requires transitive commons.math3;
    requires transitive batik.all;
    requires transitive jlatexmath;
    requires transitive markdowngenerator;
    requires transitive junit; // TODO rm & use --patch-module ?

    requires transitive jebl;


    // lphy
    exports lphy.core;
    exports lphy.core.distributions;
    exports lphy.core.narrative;
    exports lphy.core.functions;

    exports lphy.evolution;
    exports lphy.evolution.alignment;
    exports lphy.evolution.coalescent;
    exports lphy.evolution.datatype;
    exports lphy.evolution.likelihood;
    exports lphy.evolution.substitutionmodel;
    exports lphy.evolution.tree;

    exports lphy.layeredgraph;
    exports lphy.graphicalModel;
    exports lphy.graphicalModel.code;
    exports lphy.graphicalModel.types;

    exports lphy.parser;

    exports lphy.util;

    // declare service provider interface (SPI)
    exports lphy.spi;
    uses lphy.spi.LPhyExtension;

    // declare what service interface the provider intends to use
    provides lphy.spi.LPhyExtension with lphy.spi.LPhyExtImpl;
}