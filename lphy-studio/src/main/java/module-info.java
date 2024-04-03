/**
 * @author Walter Xie
 */
module lphystudio {
    requires transitive lphy.base;

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

    //TODO how to extend panels ?
    exports lphystudio.app.graphicalmodelpanel;
    // for Viewer SPI
    exports lphystudio.app.graphicalmodelpanel.viewer;
    exports lphystudio.spi;

    // Both are empty now, but must be declared in order to show studio ext in the LPhyExtension Manager.
    // LPhy extensions
    uses lphy.core.spi.Extension;
    // declare what service interface the provider intends to use
    provides lphy.core.spi.Extension with lphystudio.spi.StudioViewerImpl, lphystudio.spi.LPhyStudioImpl;
}