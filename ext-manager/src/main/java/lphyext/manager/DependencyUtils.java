package lphyext.manager;

import lphy.util.LoggerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Util class to handle dependency metadata,
 * such as versions.
 * @author Walter Xie
 */
public final class DependencyUtils {

    /**
     * Use MANIFEST.MF to load the version from the released jar,
     * but use system property to load the version in development.
     *
     * @param cls          the class to detect jar.
     * @param propertyKey  the system property to retrieve version during development.
     *                     To make this working in development, the Gradle build needs
     *                     to have {@code systemProperty($propertyKey, $version)}
     *                     assigned in the task {@code tasks.withType<JavaExec>()},
     *                     where {@code $propertyKey} is a string like "lphy.studio.version"
     *                     and {@code $version} is given in the Gradle build.
     * @return   a string of version. If no version is loaded, then return "DEVELOPMENT".
     */
    public static String getVersion(Class<?> cls, String propertyKey) { // TODO return Dependency?
        String version = null;
        // released jar for Java module system
        Module module = cls.getModule();
        if (module != null) {
            try {
                InputStream is = module.getResourceAsStream("META-INF/MANIFEST.MF");
                Manifest manifest = new Manifest(is);
                Attributes attr = manifest.getMainAttributes();
                String name = attr.getValue("Implementation-Title");
                version = attr.getValue("Implementation-Version");
            } catch (IOException e) {
                LoggerUtils.log.severe("Cannot find manifest ! Package = " + cls.getPackageName() +
                        ", class = " + cls.getName());
                e.printStackTrace();
            }
        }

        // for IDE to get version from system property "lphy.studio.version"
        if (version == null)
            version = System.getProperty(propertyKey);
        // for class path
        if (version == null)
            version = cls.getPackage().getImplementationVersion();
        // should not reach here
        if (version == null)
            version = "DEVELOPMENT";
        return version;
    }

    /* // loop through all jars
    Enumeration<URL> resources = LinguaPhyloStudio.class.getClassLoader()
            .getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
        Manifest manifest = new Manifest(resources.nextElement().openStream());
        Attributes attr = manifest.getMainAttributes();
        String name = attr.getValue("Implementation-Title");
        if ("LPhyStudio".equalsIgnoreCase(name)) {
            version = attr.getValue("Implementation-DependencyUtils");
            break;
        }
    }*/

}
