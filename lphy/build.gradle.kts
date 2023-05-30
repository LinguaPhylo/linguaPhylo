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
    api("org.antlr:antlr4-runtime:4.11.1")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.12.0")

    // io.github.linguaphylo
    api("io.github.linguaphylo:jebl:3.1.0")

    // in maven
    implementation("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

val developers = "LPhy developer team"
// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy",
            "Implementation-Vendor" to developers,
        )
    }
}

//TODO Deprecated
// this function is modified from Maksim Kostromin's example
// https://gist.github.com/daggerok/4f5f63448f24d991c273165615baa39a
// create a fat non-modular jar containing all dependencies of lphy.
tasks.register("noModFatJar", Jar::class.java) {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Implementation-Title" to "LPhy",
            "Implementation-Vendor" to "LPhy team",
        )
    }
    // add jars
    from(configurations.runtimeClasspath.get()
        .onEach { println("add from dependencies: ${it.name}") }
        .map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("**/module-info.*")
    }
    // add java
    val sourcesMain = sourceSets.main.get()
//    sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
    from(sourcesMain.output) {
        exclude("**/module-info.*")
    }
    destinationDirectory.set(file("${buildDir}/fatjar"))
}

publishing {
    publications {
        // project.name contains "lphy" substring
        create<MavenPublication>(project.name) {
            artifactId = project.base.archivesName.get()
            pom {
                description.set("A probabilistic model specification language to concisely and precisely define phylogenetic models.")
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
