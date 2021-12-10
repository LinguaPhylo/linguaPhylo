package lphyext.manager;

import java.util.Objects;

/**
 * Data class maps to pom.xml {@code <dependency>}.
 * @author Walter Xie
 */
public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
//    private String scope;

    public Dependency(String groupId, String artifactId, String version) { //, String scope
        this.groupId = Objects.requireNonNull(groupId);
        this.artifactId = Objects.requireNonNull(artifactId);
        this.version = Objects.requireNonNull(version);
//        this.scope = scope;
    }

    public Dependency() { }

    public void setGroupId(String groupId) {
        this.groupId = Objects.requireNonNull(groupId);
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = Objects.requireNonNull(artifactId);
    }

    public void setVersion(String version) {
        this.version = Objects.requireNonNull(version);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId + ':' + version;
    }

    //    public String getScope() {
//        return scope;
//    }

}
