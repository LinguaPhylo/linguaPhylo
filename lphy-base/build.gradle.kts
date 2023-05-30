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
                description.set("The base library of LPhy. LPhy is a probabilistic model specification language to concisely and precisely define phylogenetic models.")
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
