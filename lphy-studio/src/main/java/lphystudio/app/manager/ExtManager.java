package lphystudio.app.manager;

import lphy.core.LPhyExtensionFactory;
import lphy.core.spi.LPhyExtension;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//import org.json.JSONObject;

/**
 * Pull extension information from pom.xml in the root path inside the jar file.
 * @author Walter Xie
 */
public class ExtManager {
    // TODO
    public final static String EXTENSIONS_XML = "https://raw.githubusercontent.com/LinguaPhylo/linguaPhylo.github.io/master/extensions-1.0.xml";
    // TODO
    public final String url = "https://search.maven.org/solrsearch/select?q=lphy";
    // pin lphy on the top of the list
    public final static String LPHY_ID = "lphy";

    final List<LPhyExtension> extensions;
    Set<String> jarDirSet = new HashSet<>();

    public ExtManager()  {
        LPhyExtensionFactory factory = LPhyExtensionFactory.getInstance();

        extensions = factory.getExtensions();
        System.out.println(extensions);
    }

    /**
     * Find loaded LPhy extensions from loaded modular jars.
     * @return  a list of {@link Extension}
     */
    public List<Extension> getLoadedLPhyExts() {
        List<Extension> extList = new ArrayList<>();

        // find all loaded lphy exts
        for (LPhyExtension ext : extensions) {
            Class<?> cls = ext.getClass();
//            String pkgNm = "/" + cls.getPackageName().replace(".", "/");
            // include jar name
            String jarPath = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
            // rm jar name
            Path jarDir = Paths.get(jarPath).getParent();
            jarDirSet.add(jarDir.toString());
//            System.out.println(cls + ",  "  + jarPath + ",  " + jarDir);

            Module module = cls.getModule();
            // use module path
            if (module != null) {
                try {
                    Extension lphyExt = DependencyUtils.getExtensionFrom(module);
                    extList.add(lphyExt);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
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
        return String.join("; ", jarDirSet);
    }


//    public static JSONObject getPublishedLPhyExt(String url) throws IOException, JSONException {
//        try (InputStream is = new URL(url).openStream()) {
//            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//            return new JSONObject(json);
//        }
//    }

}
