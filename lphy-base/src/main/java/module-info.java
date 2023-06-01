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

    // bmodel test
    exports lphy.base.bmodeltest;

    exports lphy.base.distributions;
    exports lphy.base.functions;
    exports lphy.base.functions.alignment;
    exports lphy.base.functions.datatype;
    exports lphy.base.functions.taxa;
    exports lphy.base.functions.tree;

    // evolution
    exports lphy.base.evolution;
    exports lphy.base.evolution.alignment;
    exports lphy.base.evolution.birthdeath;
    exports lphy.base.evolution.branchrates;
    exports lphy.base.evolution.coalescent;
    exports lphy.base.evolution.continuous;
    exports lphy.base.evolution.datatype;
    exports lphy.base.evolution.io;
    exports lphy.base.evolution.likelihood;
    exports lphy.base.parser.nexus;
    exports lphy.base.evolution.sitemodel;
    exports lphy.base.evolution.substitutionmodel;
    exports lphy.base.evolution.traits;
    exports lphy.base.evolution.tree;

    exports lphy.base;
    exports lphy.base.system;

    // declare service provider interface (SPI)
    exports lphy.base.spi;
    uses LPhyExtension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with LPhyBaseImpl;

    uses SequenceTypeExtension;
    // declare what service interface the provider intends to use
    provides lphy.base.spi.SequenceTypeExtension with SequenceTypeBaseImpl;
}