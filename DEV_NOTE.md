
## Gradle

1. [Installation](https://gradle.org/install/).

2. Build project using wrapper as
[Gradle doc](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper)
suggested, where `--info` provides more information about the build process:

```bash
./gradlew build --info
```

3. Run LPhy studio application through Gradle:
<a href="./Gradle-run.png"><img src="Gradle-run.png" align="right" height="300" ></a>

```bash
./gradlew run
```

Or through IntelliJ Gradle tool window, expand lphy-studio => Task => application,
and click `run`. The screenshot is shown at the right.

4. Distribute jar files:

Expand lphy-studio => Task => distribution, and click `distZip`.
Then the zip file, named as "lphy-studio-${versoin}.zip", will be created
inside the sub-folder "build/distributions" under the `lphy-studio` module.
More details are available in the user guide of
[distribution plugin](https://docs.gradle.org/current/userguide/distribution_plugin.html).

5. Publish
TODO

### Upgrade the wrapper 

If it is not the latest version (e.g. version 7.2 at the time of writing):

```bash
./gradlew -v
./gradlew wrapper --gradle-version 7.2
```

### We choose [Gradle + Kotlin](https://gradle.org/kotlin/). 

Please also see [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html) 
and [the benefit switching from Groovy to Kotlin](https://stackoverflow.com/questions/45335874/gradle-what-is-the-benefit-if-i-switch-from-groovy-to-kotlin).

Please also see
[Declaring Dependencies between Subprojects](https://docs.gradle.org/current/userguide/declaring_dependencies_between_subprojects.html). 


## IntelliJ

- [Working with Gradle](https://www.jetbrains.com/idea/guide/tutorials/working-with-gradle/)
- [Gradle projects](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html)
