plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.1"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.1"
}

version = "1.1.0"
//base.archivesName.set("core")

dependencies {
    // required in test
    api("org.antlr:antlr4-runtime:4.9.3")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.12.0")
    // in maven
    implementation("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    // io.github.linguaphylo
    api("io.github.linguaphylo:jebl:3.1.0")

    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.13")
}

//// configure core dependencies, which can be reused in lphy-studio
//val coreJars by configurations.creating {
//    isCanBeConsumed = true
//    isCanBeResolved = false
//    extendsFrom(configurations["api"], configurations["implementation"])
//}
//// Attach the task jar to an outgoing configuration coreJars
//artifacts {
//    add("coreJars", tasks.jar)
//}

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

/**
 * For LPhy core, set working directory: ~/WorkSpace/linguaPhylo/lphy/doc,
 * and args[0] = version.
 * For extension, set working directory: ~/WorkSpace/$REPO/lphy/doc,
 * and args[0] = version, args[1] = extension name (no space),
 * args[2] = class name to implement LPhyExtension.
 * e.g. args = 0.0.5 "LPhy Extension Phylonco" phylonco.lphy.spi.Phylonco
 *
 * The docs will output to working dir, "user.dir"
 * This is equivalent to: java -p $LPHY/lib -m lphy/lphy.doc.GenerateDocs 1.1.0
 */
val lphyDoc = tasks.register("lphyDoc", JavaExec::class.java) {
    description = "Create LPhy doc"
    dependsOn("assemble")
//    println("user.dir = " + System.getProperty("user.dir"))

    // equivalent to: java -p ...
    jvmArgs = listOf("-p", sourceSets.main.get().runtimeClasspath.asPath,
        // set output to .../lphy/doc
        "-Duser.dir=${layout.projectDirectory.dir("doc")}")

    // -m lphy/lphy.doc.GenerateDocs
    mainModule.set("lphy")
    mainClass.set("lphy.doc.GenerateDocs")
    // such as 1.1.0
    setArgs(listOf("$version"))
}

// list locations of jars in dependencies
tasks.register("showCache") {
    doLast {
        configurations.compileClasspath.get().forEach { println(it) }
    }
}
