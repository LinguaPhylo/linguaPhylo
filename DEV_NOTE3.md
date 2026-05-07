# LPhy Developer Guide 103 (Maven Project)

This guide explains technical details about the Maven project and how to build,
test, and run LPhy Studio or SLPhy directly from Maven.

## Maven Project

Here are two tutorials for importing a Maven project to IntelliJ:

- [Importing a Maven project](https://www.jetbrains.com/guide/java/tutorials/working-with-maven/importing-a-project/)
- [Add Maven support to an existing project](https://www.jetbrains.com/help/idea/convert-a-regular-project-into-a-maven-project.html)

First, understand the [Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

## Prerequisites

Both `lphy-studio` and `lphy-base` depend on upstream modules. You must either:

- Do a full install first (populates the local Maven repository), **or**
- Use `-am` (also-make) to build all dependencies from source in the same command.

> **Note:** `-am` cannot be used with `exec:exec` (the Run commands below).
> When `-am` is active, Maven includes `lphy-parent` in the reactor.
> Because `lphy-parent` has no `<executable>` configured for `exec-maven-plugin`,
> the build fails with: *"The parameter 'executable' is missing or invalid"*.
> Always run `mvn install -DskipTests` first, then use the exec commands without `-am`.

## Build

Build all modules and create the assembly at `lphy-studio/target/lphy-studio-<version>/`:

```bash
mvn clean install -DskipTests
```

## Test

```bash
mvn test
```

## Run LPhy Studio

`lphy-studio/pom.xml` is configured with `exec-maven-plugin` to launch
`lphystudio.app.LinguaPhyloStudio` on the JPMS module path.

The `studio.args` property (default: empty) is appended to the Java command.
Studio silently ignores an empty argument, so omitting `-Dstudio.args` opens
Studio with no file. Note that relative paths are resolved from the
`lphy-studio/` directory, so scripts under `examples/` need a `../` prefix.

```bash
# Go to the project root directory
cd ~/WorkSpace/linguaPhylo/

# Open Studio with no file
mvn -pl lphy-studio exec:exec

# Open Studio with a script loaded
mvn -pl lphy-studio exec:exec -Dstudio.args="../examples/coalescent/hkyCoalescent.lphy"

# Open Studio with a working dir set and a script
mvn -pl lphy-studio exec:exec -Dstudio.args="-d ../examples/coalescent hkyCoalescent.lphy"
```

## Run SLPhy

`lphy-base/pom.xml` is configured with `exec-maven-plugin` to launch
`lphy.core.simulator.SLPhy` on the JPMS module path. Running from `lphy-base`
ensures its distributions and functions are available to SLPhy via ServiceLoader.

The `slphy.args` property (default: `--help`) is appended to the Java command.
Use `-Dslphy.args=` to pass arguments to SLPhy. Note that relative paths are
resolved from the `lphy-base/` directory, so scripts under `examples/` need
a `../` prefix.

```bash
# Go to the project root directory
cd ~/WorkSpace/linguaPhylo/

# Show help (default)
mvn -pl lphy-base exec:exec -Dslphy.args="-h"

# Run a LPhy script
mvn -pl lphy-base exec:exec -Dslphy.args="../examples/coalescent/hkyCoalescent.lphy"

# Multiple SLPhy flags require the assembly binary (see below)
```

## Why `exec:exec` and not `exec:java`

Both launchers use the JPMS module system (`--module-path`, `--module`).
`exec:java` runs inside Maven's own JVM on the classpath, which is incompatible
with `module-info.java`. `exec:exec` spawns a new Java process and correctly
resolves the Maven dependency graph into a `--module-path`.

## Dependency Management

Read [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html).

### Dependency Analysis Tools

1. Display the dependency tree. https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html

```bash
mvn dependency:tree -Dverbose
```

2. Output a classpath string of dependencies from the local repository. https://maven.apache.org/plugins/maven-dependency-plugin/build-classpath-mojo.html

```bash
mvn dependency:build-classpath
```

## Useful Links

- [Maven in 5 Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [List of predefined Maven properties](https://github.com/cko/predefined_maven_properties/blob/master/README.md)
- [How are "mvn clean package" and "mvn clean install" different?](https://stackoverflow.com/questions/16602017/how-are-mvn-clean-package-and-mvn-clean-install-different)
