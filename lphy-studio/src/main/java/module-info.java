/**
 * @author Walter Xie
 */
module lphystudio {
    requires transitive lphy;

    requires transitive extmanager;

    requires jlatexmath;

    // declare what service interface the provider intends to use
    provides lphy.spi.LPhyExtension with lphystudio.spi.LPhyStudioImpl;
}