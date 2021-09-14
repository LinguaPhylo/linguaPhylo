

## Gradle

Build project using wrapper as
[Gradle doc](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper)
suggested, where `--info` provides more information about the build process:

```bash
./gradlew build --info
```

Upgrade the wrapper:

```bash
./gradlew wrapper --gradle-version 7.2
```

