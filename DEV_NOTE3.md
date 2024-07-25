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

```bash
mvn clean package 
```

The release will be available under the build directory `target`.

```bash
mvn clean package -Dmaven.test.skip
```

## Useful Links

- [Maven in 5 Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
- [List of predefined Maven properties](https://github.com/cko/predefined_maven_properties/blob/master/README.md)


