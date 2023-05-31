import lphy.core.spi.LPhyCoreImpl;
import lphy.core.spi.LPhyExtension;

/**
 * @author Walter Xie
 */
module lphy.core {
    requires transitive java.datatransfer;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive java.logging;

    requires transitive org.antlr.antlr4.runtime;
    requires transitive org.apache.commons.lang3;
    requires transitive commons.math3;
    requires transitive jebl;

    requires markdowngenerator;

    exports lphy.core; // for lphy.core.spi.LPhyExtensionFactory

    // graphical model
    exports lphy.core.graphicalmodel;
    exports lphy.core.codebuilder;
    exports lphy.core.graphicalmodel.components;
    exports lphy.core.graphicalmodel.types;
    exports lphy.core.graphicalmodel.vectorization;
    exports lphy.core.layeredgraph;

    // parser
    exports lphy.core.parser;
    exports lphy.core.parser.antlr;
    exports lphy.core.parser.functions;

    // others
    exports lphy.core.narrative;
    exports lphy.core.exception;
    exports lphy.core.logger;

    // utils
    exports lphy.core.cmd;
//    exports lphy.math;
    exports lphy.core.reflection;
    exports lphy.core.system;
    exports lphy.core.util;

    // declare service provider interface (SPI)
    exports lphy.core.spi;

    uses LPhyExtension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with LPhyCoreImpl;


    //TODO
    exports lphy.core.lightweight;

}