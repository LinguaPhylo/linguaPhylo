/**
 * @author Walter Xie
 */
module lphystudio {
    requires transitive lphy.base;
//    requires transitive lphy.io;

    requires jlatexmath;
//    requires org.json;
    requires org.jfree.jfreechart;
    requires markdowngenerator;

    exports lphystudio.app;
    exports lphystudio.app.manager;

    exports lphystudio.core.codebuilder;
    exports lphystudio.core.codecolorizer;
    exports lphystudio.core.layeredgraph;
    exports lphystudio.core.logger;
    exports lphystudio.core.narrative;
    exports lphystudio.core.swing;
    exports lphystudio.core.valueeditor;
    exports lphystudio.core.theme;

    // declare what service interface the provider intends to use
    provides lphy.core.spi.LPhyExtension with lphystudio.spi.LPhyStudioImpl;
}