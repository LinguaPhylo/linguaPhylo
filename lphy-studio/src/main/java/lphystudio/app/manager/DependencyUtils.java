package lphystudio.app.manager;

import lphy.util.LoggerUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Util class to handle dependency metadata,
 * such as versions.
 * @author Walter Xie
 */
public final class DependencyUtils {

    // pom.xml should in the root dir of jar
    public final static String POM_XML_LOCATION = "pom.xml";
    public final static String MANIFEST_LOCATION = "META-INF/MANIFEST.MF";

    /**
     * Use MANIFEST.MF to load the version from the released jar,
     * and then try pom.xml if fail to load the version,
     * but use system property to load the version in development.
     *
     * @param cls          the class to detect jar.
     * @param propertyKey  the system property to retrieve version during development.
     *                     To make this working in development, the Gradle build needs
     *                     to have {@code systemProperty($propertyKey, $version)}
     *                     assigned in the task {@code tasks.withType&lt;JavaExec&gt;()},
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
                // search for MANIFEST.MF first
                InputStream is = module.getResourceAsStream(MANIFEST_LOCATION);
                if (is != null) {
                    Manifest manifest = new Manifest(is);
                    Attributes attr = manifest.getMainAttributes();
                    String name = attr.getValue("Implementation-Title");
                    version = attr.getValue("Implementation-Version");
//                    System.out.println("v1 = " + version);
                }
            } catch (IOException e) {
                LoggerUtils.log.severe("Cannot find manifest ! Package = " + cls.getPackageName() +
                        ", class = " + cls.getName());
                e.printStackTrace();
            }
            // if not, then try pom.xml
            if (version == null) {
                try {
                    Extension lphyExt = getExtensionFrom(module);
                    if (lphyExt != null) {
                        version = lphyExt.getVersion();
                    }
//                   System.out.println("v2 = " + version);
                } catch(ParserConfigurationException | SAXException | IOException e){
                    e.printStackTrace();
                }
            }
        }

        // for IDE to get version from system property, e.g. "lphy.studio.version"
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

    /**
     * @param module  the Java {@link Module} contains the classes of the {@link Extension}.
     * @return  an {@link Extension} created from pom.xml, or from MANIFEST.MF, or from source
     */
    public static Extension getExtensionFrom(Module module)
            throws IOException, ParserConfigurationException, SAXException {
        // 1. first look for pom.xml in jar
        InputStream in = module.getResourceAsStream(POM_XML_LOCATION);
        System.out.print("Load " + module);
        if (in != null)
            System.out.println(", extract meta-data from pom.xml");

        // 2. check META-INF/MANIFEST.MF
        if (in == null) {
            InputStream is = module.getResourceAsStream(MANIFEST_LOCATION);
            if (is != null) {
                Manifest manifest = new Manifest(is);
                Attributes attr = manifest.getMainAttributes();
                String name = attr.getValue("Implementation-Title");
                String version = attr.getValue("Implementation-Version");
                System.out.println(", extract meta-data from MANIFEST.MF");

                return new Extension("", name, version);
            }
        }

        // 3. then for IntelliJ using out/production/classes/module/... as reference
        if (in == null) {
            Optional<ModuleReference> opt = ModuleLayer.boot().configuration().
                    findModule(module.getName()).map(ResolvedModule::reference);
            // flatMap does not wrap it within an additional Optional.
            Optional<URI> uriOptional = opt.flatMap(ModuleReference::location);
            String dirStr = uriOptional.map(URI::getPath).orElse("");

            // dir to contain classes in IntelliJ
            if (!dirStr.trim().isEmpty() && dirStr.contains("out/production")) {
                System.out.print(" from source " + dirStr);
                // if .../sub-project/out/production/classes/module-info.class
                String parentFolder = dirStr.substring(0, dirStr.indexOf("out/production"));
                // then Gradle build path is sub-project/build/classes/java/main/...
                Path pomFromGradleBuild = Path.of(parentFolder,
                        "build/classes/java/main", "pom.xml");
                if (pomFromGradleBuild.toFile().exists()) {
                    in = new FileInputStream(pomFromGradleBuild.toFile());
                    System.out.print(", extract meta-data from " + pomFromGradleBuild);
                }
                System.out.println();
            }
        }

        // 4. unknown
        if (in == null) {
            System.err.println("Warning : cannot find extension information from module " + module.getName() + " !");
            return new Extension("", module.getName(), "unknown");
        }
//               String pom = new String(in.readAllBytes(), StandardCharsets.UTF_8);
//               System.out.println(pom);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        POMXMLHandler handler = new POMXMLHandler();
        saxParser.parse(in, handler);

        return handler.getExtension();
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
