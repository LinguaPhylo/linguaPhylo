import lphy.base.spi.LPhyBaseImpl;
import lphy.base.spi.SequenceTypeBaseImpl;
import lphy.base.spi.SequenceTypeExtension;
import lphy.core.spi.LPhyExtension;

/**
 * @author Walter Xie
 */
module lphy.base {
    requires transitive lphy.core;
    requires transitive jebl;

    requires transitive java.desktop;

    requires info.picocli;

    // bmodel test
    exports lphy.base.bmodeltest;

    exports lphy.base.distribution;
    exports lphy.base.function;
    exports lphy.base.function.alignment;
    exports lphy.base.function.datatype;
    exports lphy.base.function.taxa;
    exports lphy.base.function.tree;

    // evolution
    exports lphy.base.evolution;
    exports lphy.base.evolution.alignment;
    exports lphy.base.evolution.birthdeath;
    exports lphy.base.evolution.branchrate;
    exports lphy.base.evolution.coalescent;
    exports lphy.base.evolution.continuous;
    exports lphy.base.evolution.datatype;
    exports lphy.base.evolution.likelihood;
    exports lphy.base.evolution.sitemodel;
    exports lphy.base.evolution.substitutionmodel;
    exports lphy.base.evolution.tree;

    exports lphy.base.logger;

    exports lphy.base;
//    exports lphy.base.system;
    exports lphy.base.math;
    exports lphy.base.simulator;

    exports lphy.base.parser;
    exports lphy.base.parser.nexus;

    // declare service provider interface (SPI)
    exports lphy.base.spi;

    // LPhy extensions
    uses LPhyExtension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with LPhyBaseImpl;

    uses SequenceTypeExtension;
    // declare what service interface the provider intends to use
    provides lphy.base.spi.SequenceTypeExtension with SequenceTypeBaseImpl;
}