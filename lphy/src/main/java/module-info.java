import lphy.core.spi.LPhyCoreImpl;
import lphy.core.spi.LPhyExtension;

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

    // graphical model
    exports lphy.core.model;
    exports lphy.core.model.annotation;
    exports lphy.core.model.component;
    exports lphy.core.model.component.argument;
    exports lphy.core.model.datatype;

    // parser
    exports lphy.core.parser;
    exports lphy.core.parser.antlr;
    exports lphy.core.parser.function;

    // vectorization
    exports lphy.core.vectorization;
    exports lphy.core.vectorization.array;
    exports lphy.core.vectorization.operation;

    // others
    exports lphy.core.narrative;
    exports lphy.core.exception;

    // declare service provider interface (SPI)
    exports lphy.core.spi;

    uses LPhyExtension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with LPhyCoreImpl;

}