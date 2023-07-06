import lphy.core.spi.LPhyCoreImpl;
import lphy.core.spi.LPhyCoreValueFormatterImpl;
import lphy.core.spi.LPhyValueFormatter;

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

    requires info.picocli;

    exports lphy.core.logger;

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
//    exports lphy.core.narrative;
    exports lphy.core.exception;
    exports lphy.core.io;

    // declare service provider interface (SPI)
    exports lphy.core.spi;
    exports lphy.core.simulator;
    opens lphy.core.simulator;

    // LPhy extensions
    uses lphy.core.spi.LPhyExtension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with LPhyCoreImpl;

    uses LPhyValueFormatter;
    provides LPhyValueFormatter with LPhyCoreValueFormatterImpl;
}