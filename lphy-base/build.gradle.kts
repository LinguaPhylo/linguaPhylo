plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.2"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.2"
}

//version = "1.2.0"
//base.archivesName.set("core")

dependencies {
    // required in test
    implementation(project(":lphy"))
    // io.github.linguaphylo
    api("io.github.linguaphylo:jebl:3.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

val developers = "LPhy developer team"
// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy base",
            "Implementation-Vendor" to developers,
        )
    }
}

publishing {
    publications {
        // project.name contains "lphy" substring
        create<MavenPublication>(project.name) {
            artifactId = project.base.archivesName.get()
            pom {
                description.set("The standard library of LPhy, which contains the required generative distributions and basic functions.")
                developers {
                    developer {
                        name.set(developers)
                    }
                }
            }
        }
    }
}

// junit tests, https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html
tasks.test {
    useJUnitPlatform() {
        excludeTags("dev")
    }
    // set heap size for the test JVM(s)
    minHeapSize = "256m"
    maxHeapSize = "3G"
    // show standard out and standard error of the test JVM(s) on the console
    testLogging.showStandardStreams = true

    reports {
        junitXml.apply {
            isOutputPerTestCase = true // defaults to false
            mergeReruns.set(true) // defaults to false
        }
    }
}

// list locations of jars in dependencies
tasks.register("showCache") {
    doLast {
        configurations.compileClasspath.get().forEach { println(it) }
    }
}
