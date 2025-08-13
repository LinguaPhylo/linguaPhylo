import lphy.core.spi.LPhyCoreImpl;
import lphy.core.spi.ValueFormatterCoreImpl;

/**
 * @author Walter Xie
 */
module lphy.core {
    requires transitive java.datatransfer;
    requires transitive java.prefs;
    requires transitive java.logging;

    requires transitive org.antlr.antlr4.runtime;
    requires transitive org.apache.commons.lang3;
    requires transitive commons.math3;

    requires transitive info.picocli;
    requires transitive lphy.phylospec;

    // graphical model
    exports lphy.core.model;
    exports lphy.core.model.annotation;
//    exports lphy.core.model.component;
    exports lphy.core.model.datatype;

    // parser
    exports lphy.core.parser;
    exports lphy.core.parser.antlr;
    exports lphy.core.parser.argument;
    exports lphy.core.parser.function;
    exports lphy.core.parser.graphicalmodel;

    // vectorization
    exports lphy.core.vectorization;
//    exports lphy.core.vectorization.array;
    exports lphy.core.vectorization.operation;

    // others
    exports lphy.core.codebuilder;
    exports lphy.core.logger;
    //    exports lphy.core.narrative;
    exports lphy.core.exception;
    exports lphy.core.io;

    // declare service provider interface (SPI)
    exports lphy.core.spi;
    exports lphy.core.simulator;
    opens lphy.core.simulator;

    // LPhy extensions
    uses lphy.core.spi.Extension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.Extension with LPhyCoreImpl, ValueFormatterCoreImpl;
}