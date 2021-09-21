
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

5. Upgrade the wrapper if it is not the latest version (e.g. version 7.2 at the time of writing):

```bash
./gradlew -v
./gradlew wrapper --gradle-version 7.2
```

4. We choose [Gradle + Kotlin](https://gradle.org/kotlin/). 

Please also see [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html) 
and [the benefit switching from Groovy to Kotlin](https://stackoverflow.com/questions/45335874/gradle-what-is-the-benefit-if-i-switch-from-groovy-to-kotlin).

5. Shared build logic is organised in 
[the directory `buildSrc`](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources). 

Please also see
[Declaring Dependencies between Subprojects](https://docs.gradle.org/current/userguide/declaring_dependencies_between_subprojects.html). 


## IntelliJ

- [Working with Gradle](https://www.jetbrains.com/idea/guide/tutorials/working-with-gradle/)
- [Gradle projects](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html)
