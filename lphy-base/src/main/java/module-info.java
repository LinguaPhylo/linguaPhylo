import lphy.base.spi.LPhyBaseImpl;
import lphy.base.spi.SequenceTypeBaseImpl;
import lphy.base.spi.ValueFormatterBaseImpl;

/**
 * @author Walter Xie
 */
module lphy.base {
    requires transitive lphy.core;
    requires transitive jebl;

    requires transitive java.desktop;

    // bmodel test
    exports lphy.base.bmodeltest;

    exports lphy.base.distribution;
    exports lphy.base.function;
    exports lphy.base.function.alignment;
    exports lphy.base.function.datatype;
    exports lphy.base.function.io;
    exports lphy.base.function.taxa;
    exports lphy.base.function.tree;

    // evolution
    exports lphy.base.evolution;
    exports lphy.base.evolution.alignment;
    exports lphy.base.evolution.birthdeath;
    exports lphy.base.evolution.branchrate;
    exports lphy.base.evolution.coalescent;
    exports lphy.base.evolution.coalescent.populationmodel;
    exports lphy.base.evolution.continuous;
    exports lphy.base.evolution.datatype;
    exports lphy.base.evolution.likelihood;
    exports lphy.base.evolution.sitemodel;
    exports lphy.base.evolution.substitutionmodel;
    exports lphy.base.evolution.tree;

//    exports lphy.base.logger;
//    opens lphy.base.simulator;
//    exports lphy.base.simulator;

    exports lphy.base;
//    exports lphy.base.system;
    exports lphy.base.math;

    exports lphy.base.parser;
    exports lphy.base.parser.nexus;
    exports lphy.base.parser.newick;

    // declare service provider interface (SPI)
    exports lphy.base.spi;
    exports lphy.base.logger;

    // LPhy extensions
    uses lphy.core.spi.Extension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.Extension with LPhyBaseImpl, ValueFormatterBaseImpl, SequenceTypeBaseImpl;
}