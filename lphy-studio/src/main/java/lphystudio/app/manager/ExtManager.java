package lphystudio.app.manager;

import lphy.core.spi.LPhyCoreLoader;
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

    final List<lphy.core.spi.Extension> extensions;
    Set<String> jarDirSet = new HashSet<>();

    public ExtManager()  {
        //TODO other loaders ?
        LPhyCoreLoader lphyCoreLoader = new LPhyCoreLoader();
        lphyCoreLoader.loadExtensions();

        extensions = lphyCoreLoader.getExtensionMap().values().stream().toList();
        System.out.println(extensions);
    }

    /**
     * Find loaded LPhy extensions from loaded modular jars.
     * @return  a list of {@link LPhyExtension}
     */
    public List<LPhyExtension> getLPhyExtensions() {
        Set<LPhyExtension> extSet = new TreeSet<>();

        // find all loaded lphy exts
        for (lphy.core.spi.Extension ext : extensions) {
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
                    LPhyExtension lphyExt = DependencyUtils.getExtensionFrom(module);
                    extSet.add(lphyExt);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
            } else { // use class path
//                URL jarURL = cls.getResource(pkgNm);
//                jarURL = cls.getResource(pkgNm);
//                LPhyExtension lphyExt = new LPhyExtension(jarURL);
//                extList.add(lphyExt);
                throw new UnsupportedOperationException("Do not support class path ! Please use module path.");
            }

        }

        List<LPhyExtension> extList = new ArrayList<>(extSet);
        // pin lphy on the top
        for (int i = 0; i < extList.size(); i++) {
            LPhyExtension ext = extList.get(i);
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
