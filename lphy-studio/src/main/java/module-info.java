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

    // ViewerRegister loads all Viewers
    uses lphystudio.app.graphicalmodelpanel.viewer.Viewer;
    // declare what service interface the provider intends to use
<<<<<<< HEAD

=======
>>>>>>> 72a57d09f615f413b188480adbeffebd95300fd8
    // the core uses hard core to register all internal Viewers,
    // but extensions need to declare what service interface the provider intends to use for new Viewers.
    provides lphystudio.app.graphicalmodelpanel.viewer.Viewer with lphystudio.viewer.PopSizeFuncViewer;

    // Note: to adapt with the system not using Java module but using class path,
    // they need to be declared inside META-INF/services/lphystudio.app.graphicalmodelpanel.viewer.Viewer as well.
}