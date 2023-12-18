package lphystudio.app.manager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Inherit {@link Dependency} and store some extra
 * metadata only for the extension.
 * @author Walter Xie
 */
public class LPhyExtension extends Dependency {

    private String name; // may be diff to artifactId
    private String desc;
    private String website;

    private List<Dependency> dependencies = new ArrayList<>();

    public LPhyExtension() {
        super();
    }

    public LPhyExtension(String groupId, String artifactId, String version) {
        super(groupId, artifactId, version);

    }

    public String getDependenciesStr() {
        return dependencies.toString();
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * This not works for module path.
     //     * @param jarURL
     * @throws FileNotFoundException
     @Deprecated
     public LPhyExtension(URL jarURL) throws FileNotFoundException {
     String jarLoc = Objects.requireNonNull(jarURL).toString();

     if (!jarLoc.startsWith("jar:file:") || !jarLoc.contains(".jar"))
     throw new FileNotFoundException("Cannot locate lphy extension jar : " + jarLoc + " !");
     // jar:file:/.../libs/lphy-1.1.0-SNAPSHOT.jar!/lphy/spi
     String s = jarLoc.substring(0, jarLoc.indexOf("!"));
     this.name = s.substring(s.lastIndexOf("/")+ 1);
     this.version = "";
     this.jarDir = s.replace("jar:file:", "")
     .replace(name,"");

     System.out.println("locate " + name + " " + version + " in " + jarDir);
     }     */

}
