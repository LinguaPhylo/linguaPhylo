
## Gradle

1. [Installation](https://gradle.org/install/).

2. Clean or build project using wrapper as suggested by
[Gradle doc](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper),
where `--info` provides more information about the process:

```bash
./gradlew clean
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

5. Publish to Maven central repository:

```bash
./gradlew publish --info 
    -Psigning.secretKeyRingFile=/path/to/.gnupg/mysecret.gpg 
    -Psigning.password=mypswd -Psigning.keyId=last8symbols 
    -Possrh.user=myuser -Possrh.pswd=mypswd
```

This supplies information of both your authentications for
[signing](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)
and [publishing](https://docs.gradle.org/current/userguide/publishing_maven.html).

The property `signing.password` is the passphrase of `gpg` used to protect your private key.
Run `gpg --list-keys` to find your keyId, which is a super long string 
mixed with letters and numbers, and assign the last 8 symbols to 
the property `signing.keyId` in the command line.

`ossrh.user` and `ossrh.pswd` are used to login your JIRA account in
[Sonatype](https://central.sonatype.org/publish/publish-guide/).
If your password contains special characters, 
you can use single quotes to wrap the string to avoid errors.
More details are available in Sonatype's
[publish guide](https://central.sonatype.org/publish/publish-guide/)
and [release rules](https://central.sonatype.org/publish/release/).

**Note:** once published, you will not be able to remove/update/modify the artifact in Sonatype.
So for testing purpose, assign the `version` in `build.gradle.kts` to contain 
the suffix "-SNAPSHOT", the artifact will be published to
https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/linguaphylo/,
which can be updated.

See also [Snapshot Repository vs Release Repository](https://stackoverflow.com/questions/275555/maven-snapshot-repository-vs-release-repository)
and [Best Practices for releasing with 3rd party SNAPSHOT dependencies](https://blog.sonatype.com/2009/01/best-practices-for-releasing-with-3rd-party-snapshot-dependencies/).


### Versions

- [Declaring Versions and Ranges](https://docs.gradle.org/current/userguide/single_versions.html)


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
