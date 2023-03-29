/**
 * @author Walter Xie
 */
module lphystudio {
    requires transitive lphy;

    requires jlatexmath;
//    requires org.json;
    requires org.jfree.jfreechart;
    requires info.picocli;
    opens lphystudio.app.simulator;
//    exports lphystudio.app.simulator;

    exports lphystudio.app;
    exports lphystudio.app.narrative;
    exports lphystudio.app.manager;

    exports lphystudio.core.codecolorizer;
    exports lphystudio.core.layeredgraph;
    exports lphystudio.core.log;
    exports lphystudio.core.swing;
    exports lphystudio.core.valueeditors;

    // declare what service interface the provider intends to use
    provides lphy.spi.LPhyExtension with lphystudio.spi.LPhyStudioImpl;
}