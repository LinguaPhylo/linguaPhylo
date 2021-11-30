plugins {
    `java-library`
    `maven-publish`
//    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.0-SNAPSHOT"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.0-SNAPSHOT"
}

version = "1.1.0-SNAPSHOT"
//base.archivesName.set("core")

dependencies {
    // required in test
    api("org.antlr:antlr4-runtime:4.9.3")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.12.0")
    // in maven
    api("org.scilab.forge:jlatexmath:1.0.7")
    api("org.scilab.forge:jlatexmath-font-greek:1.0.7")
    api("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
    api("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    // io.github.linguaphylo
    api("io.github.linguaphylo:jebl:3.1.0")

    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.13")
}

// configure core dependencies, which can be reused in lphy-studio
val coreJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations["api"])
}
// Attach the task jar to an outgoing configuration coreJars
artifacts {
    add("coreJars", tasks.jar)
}

// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy",
            "Implementation-Vendor" to "LPhy team",
        )
    }
}

publishing {
    publications {
        // must have "lphy" substring in the name
        create<MavenPublication>("lphy-core") {
            artifactId = project.base.archivesName.get()
            pom {
                description.set("A probabilistic model specification language to concisely and precisely define phylogenetic models.")
                developers {
                    developer {
                        name.set("LPhy developer team")
                    }
                }
            }
        }
    }
}

// junit tests, https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html
tasks.test {
    useJUnit()
    // useJUnitPlatform()
    // set heap size for the test JVM(s)
    minHeapSize = "128m"
    maxHeapSize = "1G"
    // show standard out and standard error of the test JVM(s) on the console
    testLogging.showStandardStreams = true
    //testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
}

// list locations of jars in dependencies
tasks.register("showCache") {
    doLast {
        configurations.compileClasspath.get().forEach { println(it) }
    }
}
