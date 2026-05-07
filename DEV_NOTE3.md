# LPhy Developer Guide 103 (Maven Project)

This guide explains technical details about the Maven project and how to build,
test, and run LPhy Studio or SLPhy directly from Maven.

## Maven Project

The repository is a Maven multi-module project. The three submodules form a strict
dependency chain, and all share a single parent POM:

```
lphy-parent  (aggregator + parent POM, packaging = pom)
├── lphy          ── core: ANTLR parser, model graph, SPI framework
│       depends on: antlr4-runtime, commons-math3, commons-lang3, picocli, org.json
│
├── lphy-base     ── standard library: distributions, functions, evolution models
│       depends on: lphy  +  jebl, mahout-math, colt
│
└── lphy-studio   ── Swing GUI: model visualisation, LPhy Studio app
        depends on: lphy-base  +  jfreechart, jlatexmath, markdowngenerator
```

The arrows flow upward: `lphy-studio` → `lphy-base` → `lphy`.
There are no circular dependencies; each module only sees the public API of the
module(s) below it, which is enforced at compile time by the JPMS `module-info.java`
declarations.

### Why `lphy-parent` exists

`lphy-parent` serves two distinct Maven roles simultaneously.

As the **aggregator**, it lists all three child modules under `<modules>`, so a
single `mvn` invocation at the project root builds (or tests) every module in
the correct topological order — no need to `cd` into each directory manually.

As the **parent POM**, it is the single source of truth for shared configuration
that would otherwise be copy-pasted across all three child POMs:

- **Java release version** — `<maven.compiler.release>25</maven.compiler.release>`
  is declared once; every child compiles against JDK 25 automatically.
- **Plugin versions** — `maven-compiler-plugin`, `maven-surefire-plugin`,
  `maven-source-plugin`, `maven-javadoc-plugin`, and `flatten-maven-plugin` are
  pinned in `<pluginManagement>` so child POMs can reference them without
  repeating version numbers.
- **CI-friendly versioning** — the `${revision}` property and the
  `flatten-maven-plugin` work together: the parent defines `<revision>1.8.0-SNAPSHOT</revision>`,
  child POMs inherit it via `${project.version}`, and the flatten plugin rewrites
  `${revision}` to a literal string in every published `.pom` file so consumers
  in Maven Central do not need to resolve a property that only exists inside the
  multi-module build.
- **Shared dependencies** — `junit-bom` is imported once in `<dependencyManagement>`
  and the `junit-jupiter` test dependency is declared once, so all three modules
  get consistent JUnit 5 versions with no duplication.
- **Release profile** — GPG signing (`maven-gpg-plugin`) and deployment to Maven
  Central (`central-publishing-maven-plugin`) are configured in the `release`
  profile of the parent. Activating `-Prelease` at the root automatically applies
  to every module, ensuring a consistent, reproducible release process.

Without `lphy-parent`, every child POM would have to repeat all of the above, and
keeping them in sync across three modules (and any future extensions) would become
error-prone.

---

Here are two tutorials for importing a Maven project to IntelliJ:

- [Importing a Maven project](https://www.jetbrains.com/guide/java/tutorials/working-with-maven/importing-a-project/)
- [Add Maven support to an existing project](https://www.jetbrains.com/help/idea/convert-a-regular-project-into-a-maven-project.html)

First, understand the [Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).


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

> **Note:** `-am` cannot be used with `exec:exec` above.
> When `-am` is active, Maven includes `lphy-parent` in the reactor.
> Because `lphy-parent` has no `<executable>` configured for `exec-maven-plugin`,
> the build fails with: *"The parameter 'executable' is missing or invalid"*.
> Always run `mvn install -DskipTests` first, then use the exec commands without `-am`.

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

### Why `exec:exec` and not `exec:java`

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

## Release

Releases are published to Maven Central automatically by the `mvnrelease.yml`
workflow. The trigger is a Git tag whose name matches `v*`. The tag name becomes
the published version (the leading `v` is stripped by the CI).

The required secrets (`CENTRAL_USERNAME`, `CENTRAL_TOKEN`, `GPG_PRIVATE_KEY`,
`GPG_PASSPHRASE`) are already saved in the GitHub repository settings and are
picked up automatically by the workflow.

### Step-by-step release procedure

**1. Verify the build is green**

Ensure the `master` branch passes all tests before tagging:

```bash
mvn clean verify
```

**2. Update `<revision>` in `pom.xml` if needed**

During development `pom.xml` carries a `SNAPSHOT` version, e.g. `1.8.0-SNAPSHOT`.
The CI overwrites this automatically using the tag name, so the `pom.xml` value
does not need to change *before* tagging. However, it is good practice to confirm
that the version you intend to release matches the current `<revision>`:

```xml
<!-- root pom.xml -->
<revision>1.8.0-SNAPSHOT</revision>   <!-- will become 1.8.0 when you push v1.8.0 -->
```

**3. Push the release tag**

```bash
# Replace 1.8.0 with the actual release version
git tag v1.8.0
git push origin v1.8.0
```

Pushing a tag that matches `v*` triggers `mvnrelease.yml`. The CI will:

1. Strip the `v` prefix → version string `1.8.0`.
2. Run `mvn versions:set -DnewVersion=1.8.0` to rewrite all POMs in the reactor.
3. Run `mvn -Prelease deploy`, which executes the full lifecycle
   (compile → test → package → verify → deploy) with:
   - GPG artifact signing (`maven-gpg-plugin`).
   - Upload and auto-publication to Maven Central (`central-publishing-maven-plugin`).

**4. Bump the snapshot version after release**

Once the tag is pushed, immediately advance `<revision>` on `master` to the next
development snapshot so that snapshot deployments to GitHub Packages do not
collide with the just-released version:

```bash
# Edit pom.xml: change <revision>1.8.0-SNAPSHOT</revision>
#                     to  <revision>1.9.0-SNAPSHOT</revision>
git add pom.xml
git commit -m "bump version to 1.9.0-SNAPSHOT"
git push origin master
```

### What the `release` profile does

The `-Prelease` flag activates the profile declared in the root `pom.xml`.
It adds two plugins to the build:

- **`maven-gpg-plugin`** — signs every artifact (`.jar`, `-sources.jar`,
  `-javadoc.jar`, `.pom`) with the GPG key stored in the `GPG_PRIVATE_KEY` secret.
- **`central-publishing-maven-plugin`** — uploads the signed artifacts to the
  Maven Central portal and sets `<autoPublish>true</autoPublish>`, so they are
  promoted to the public repository without a manual approval step.

### Snapshot deployments (non-release)

Every push to `master` that passes tests also triggers `deploy-snapshot` in
`verify.yml`. That job reads `<revision>` from `pom.xml`; if it ends in
`-SNAPSHOT` it deploys JARs to **GitHub Packages** (not Maven Central). This
gives downstream projects a way to depend on the latest unreleased code.

## Useful Links

- [Maven in 5 Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [List of predefined Maven properties](https://github.com/cko/predefined_maven_properties/blob/master/README.md)
- [How are "mvn clean package" and "mvn clean install" different?](https://stackoverflow.com/questions/16602017/how-are-mvn-clean-package-and-mvn-clean-install-different)
