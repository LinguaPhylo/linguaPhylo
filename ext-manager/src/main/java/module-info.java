/**
 * @author Walter Xie
 */
module extmanager {
    requires java.desktop;
    requires java.xml;

    requires org.json;

    requires lphy;
    // A service interface LPhyExtension can be consumed
    uses lphy.spi.LPhyExtension;

    exports lphyext.manager;
    exports lphyext.app;
}