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
    requires transitive jebl;

    requires markdowngenerator;

    // bmodel test
    exports lphy.bmodeltest;

    exports lphy.core;
    exports lphy.core.distributions;
    exports lphy.core.narrative;
    exports lphy.core.functions;
    exports lphy.core.functions.taxa;
    exports lphy.core.functions.tree;
    exports lphy.core.functions.alignment;
    // doc
    exports lphy.doc;

    // evolution
    exports lphy.evolution;
    exports lphy.evolution.alignment;
    exports lphy.evolution.birthdeath;
    exports lphy.evolution.branchrates;
    exports lphy.evolution.coalescent;
    exports lphy.evolution.continuous;
    exports lphy.evolution.datatype;
    exports lphy.evolution.io;
    exports lphy.evolution.likelihood;
    exports lphy.evolution.sitemodel;
    exports lphy.evolution.substitutionmodel;
    exports lphy.evolution.traits;
    exports lphy.evolution.tree;

    // graphical model
    exports lphy.graphicalModel;
    exports lphy.graphicalModel.code;
    exports lphy.graphicalModel.types;
    exports lphy.graphicalModel.logger;
    exports lphy.layeredgraph;

    exports lphy.nexus;

    // parser
    exports lphy.parser;
    exports lphy.parser.functions;

    // utils
    exports lphy.math;
    exports lphy.reflection;
    exports lphy.system;
    exports lphy.util;
    exports lphy; // for lphy.LPhyExtensionFactory

    // declare service provider interface (SPI)
    exports lphy.spi;
    uses lphy.spi.LPhyExtension;

    // declare what service interface the provider intends to use
    provides lphy.spi.LPhyExtension with lphy.spi.LPhyExtImpl;
}