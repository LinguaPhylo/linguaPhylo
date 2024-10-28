# LPhy Developer Guide 103 (Maven project)

This tutorial explains some technical details about Maven project in IntelliJ.

## Maven project

Here are two tutorials for importing a Maven project to IntelliJ :

- [Importing a Maven project](https://www.jetbrains.com/guide/java/tutorials/working-with-maven/importing-a-project/)
- [Add Maven support to an existing project](https://www.jetbrains.com/help/idea/convert-a-regular-project-into-a-maven-project.html)

## Build

First, you need to understand a [Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

## Test

When you want to reuse some classes in the unit tests in another project, such as utils, 
you create a jar containing test-classes.

https://maven.apache.org/plugins/maven-jar-plugin/examples/create-test-jar.html

## Release 

1. Git pull, and then build and test. If there is no issues, go to the next step.

2. Update the version in "pom.xml". If the project is also a BEAST2 package, e.g. LPhyBeast, 
you need to update the "version.xml" as well.

3. Run the `install` command to create the release.

```bash
mvn clean install 
```

The release will be available under the build directory `target`.

```bash
mvn clean install -Dmaven.test.skip
```

4. Find the jar or zip file in the corresponding `target` folder, and upload it to Github release.

## Dependency management

Read [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html). 

### Dependency analysis tools

1. Displays the dependency tree for this project. https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html

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

