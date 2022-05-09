/**
 * @author Walter Xie
 */
module lphystudio {
    requires transitive lphy;
    requires transitive extmanager;

    requires jlatexmath;

    exports lphystudio.app;
    exports lphystudio.app.narrative;

    exports lphystudio.core.codecolorizer;
    exports lphystudio.core.layeredgraph;
    exports lphystudio.core.log;
    exports lphystudio.core.swing;
    exports lphystudio.core.valueeditors;

    // declare what service interface the provider intends to use
    provides lphy.spi.LPhyExtension with lphystudio.spi.LPhyStudioImpl;
}