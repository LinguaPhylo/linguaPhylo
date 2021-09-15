

## Gradle

1. [Installation](https://gradle.org/install/).

2. Build project using wrapper as
[Gradle doc](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper)
suggested, where `--info` provides more information about the build process:

```bash
./gradlew build --info
```

3. Upgrade the wrapper if it is not the latest version:

```bash
./gradlew -v
./gradlew wrapper --gradle-version 7.2
```

4. [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
and [the benefit switching from Groovy to Kotlin](https://stackoverflow.com/questions/45335874/gradle-what-is-the-benefit-if-i-switch-from-groovy-to-kotlin).


## IntelliJ

- [Getting Started with Gradle](https://www.jetbrains.com/help/idea/getting-started-with-gradle.html) 
- [Gradle projects](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html)
