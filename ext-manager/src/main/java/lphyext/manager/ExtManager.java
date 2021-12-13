package lphyext.manager;

import lphy.LPhyExtensionFactory;
import lphy.spi.LPhyExtension;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ExtManager {

    public final static String EXTENSIONS_XML = "https://raw.githubusercontent.com/LinguaPhylo/linguaPhylo.github.io/master/extensions-1.0.xml";

    public final String url = "https://search.maven.org/solrsearch/select?q=lphy";


    public final static String LPHY_ID = "lphy";
    // "META-INF/maven/io.github.linguaphylo/lphy/pom.xml"
    public final String POM_XML_LOCATION = "pom.xml";

    final List<LPhyExtension> extensions;
    Set<String> jarDirSet = new HashSet<>();

    public ExtManager()  {
        LPhyExtensionFactory factory = LPhyExtensionFactory.getInstance();

        extensions = factory.getExtensions();
        System.out.println(extensions);
    }

    public List<Extension> getLoadedLPhyExts() throws IOException {
        List<Extension> extList = new ArrayList<>();

        // find all loaded lphy exts
        for (LPhyExtension ext : extensions) {
            Class<?> cls = ext.getClass();
            String pkgNm = "/" + cls.getPackageName().replace(".", "/");
            // include jar name
            String jarPath = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            // rm jar name
            Path jarDir = Paths.get(jarPath).getParent();
            jarDirSet.add(jarDir.toString());

            System.out.println(cls + ",  "  + pkgNm + ",  "  + cls.getResource(pkgNm) + ", in " + jarDir);

            Module module = cls.getModule();
            // use module path
            if (module != null) {

                InputStream is = module.getResourceAsStream(POM_XML_LOCATION);
//                String pom = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//                System.out.println(pom);

                Extension lphyExt = new Extension();
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    POMXMLHandler handler = new POMXMLHandler();
                    saxParser.parse(is, handler);

                    lphyExt = handler.getExtension();
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }

//                ModuleDescriptor md = module.getDescriptor();
//                Extension lphyExt = new Extension(md.name(), md.rawVersion().orElse("null"), "");
                extList.add(lphyExt);
            } else { // use class path
//                URL jarURL = cls.getResource(pkgNm);
//                jarURL = cls.getResource(pkgNm);
//                Extension lphyExt = new Extension(jarURL);
//                extList.add(lphyExt);
                throw new UnsupportedOperationException("Do not support class path ! Please use module path.");
            }

        }

        // sort by ext artifactId
        extList.sort(Comparator.comparing(Extension::getArtifactId));
        // pin lphy on the top
        for (int i = 0; i < extList.size(); i++) {
            Extension ext = extList.get(i);
            if (LPHY_ID.equalsIgnoreCase(ext.getArtifactId())) {
                extList.add(0, extList.remove(i));
            }
        }

//        URL url = ext.getClass().getResource("/META-INF/MANIFEST.MF");
//        manifest.read(url.openStream());
//        Attributes atts = manifest.getMainAttributes();

        //Search ext in Maven API

//        JSONObject mavenResp = ExtManager.getPublishedLPhyExt(url);

        // [ … ] parsed to JSONArray, { … } parsed to JSONObject
//        JSONArray exts = mavenResp.getJSONObject("response").getJSONArray("docs");

        return extList;
    }

    /**
     * @return  single string of all directories containing jars.
     */
    public String getJarDirStr() {
        return String.join(";", jarDirSet);
    }

    public static JSONObject getPublishedLPhyExt(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(json);
        }
    }

}
