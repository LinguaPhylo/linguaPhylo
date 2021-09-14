package lphystudio.app.extmanager;

import lphy.LPhyExtensionFactory;
import lphy.spi.LPhyExtension;

import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;

public class ExtManager {

    public final static String EXTENSIONS_XML = "https://raw.githubusercontent.com/LinguaPhylo/linguaPhylo.github.io/master/extensions-1.0.xml";

    Manifest manifest = new Manifest();

    final List<LPhyExtension> extensions;

    public ExtManager()  {
        LPhyExtensionFactory factory = LPhyExtensionFactory.getInstance();

        extensions = factory.getExtensions();
        System.out.println(extensions);
    }

    public void test() throws IOException {

        LPhyExtension ext = extensions.get(0);

//        URL url = ext.getClass().getResource("/META-INF/MANIFEST.MF");
//
//        manifest.read(url.openStream());
//
//        Attributes atts = manifest.getMainAttributes();
    }

}
