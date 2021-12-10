package lphyext.manager;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 1 pom.xml to create 1 lphy extension.
 * @author Walter Xie
 */
public class POMXMLHandler extends DefaultHandler {

    public static final String GROUP_ID = "groupId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String VERSION = "version";
    public static final String WEBSITE = "url";
    public static final String DEPENDENCY = "dependency";
    public static final String DEPD_LIST = "dependencies";
    public static final String SCOPE = "scope";

    private Extension extension;
    private StringBuffer currVal = new StringBuffer();

    // same tags (e.g. groupId, artifactId) in dependencies
    // false indicates the extension's properties, true means dependencies
    private boolean isDependency;
    private List<Dependency> dependencies;
    private Dependency currDepd;


    public Extension getExtension() {
//        System.out.println(extension.getArtifactId());
        return extension;
    }

    @Override
    public void startDocument() {
        extension = new Extension();
        // extension artifactId in the beginning of XML
        isDependency = false;
        dependencies = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
         // reset the tag value
        currVal.setLength(0);

        if (DEPD_LIST.equalsIgnoreCase(qName))
            isDependency = true;
        if (DEPENDENCY.equalsIgnoreCase(qName))
            currDepd = new Dependency();

    }

    public void endElement(String uri, String localName, String qName) {
        String valStr = currVal.toString();
//        System.out.println(qName + " : " + valStr);

        if (GROUP_ID.equalsIgnoreCase(qName)) {
            if (isDependency)
                currDepd.setGroupId(valStr);
            else
                extension.setGroupId(valStr);
        }

        if (ARTIFACT_ID.equalsIgnoreCase(qName)) {
            if (isDependency)
                currDepd.setArtifactId(valStr);
            else
                extension.setArtifactId(valStr);
        }

        if (VERSION.equalsIgnoreCase(qName)) {
            if (isDependency)
                currDepd.setVersion(valStr);
            else
                extension.setVersion(valStr);
        }

        if ("name".equalsIgnoreCase(qName))
            extension.setName(currVal.toString());

        if ("description".equalsIgnoreCase(qName))
            extension.setDesc(currVal.toString());

        if (WEBSITE.equalsIgnoreCase(qName))
            extension.setWebsite(currVal.toString());

        if (DEPENDENCY.equalsIgnoreCase(qName))
            dependencies.add(currDepd);

        if (DEPD_LIST.equalsIgnoreCase(qName))
            extension.setDependencies(dependencies);
    }

    public void characters(char ch[], int start, int length) {
        currVal.append(ch, start, length);
    }

}
