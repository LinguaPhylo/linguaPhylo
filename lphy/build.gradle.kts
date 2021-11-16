import java.nio.file.*

plugins {
    `java-library`
    `maven-publish`
    signing
}

version = "1.1.0-SNAPSHOT"
//base.archivesName.set("core")

dependencies {
    // required in test
    api("org.antlr:antlr4-runtime:4.8")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.10")
    // in maven
    api("org.scilab.forge:jlatexmath:1.0.7")
    api("org.scilab.forge:jlatexmath-font-greek:1.0.7")
    api("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
    api("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    // io.github.linguaphylo
    api("io.github.linguaphylo:jebl:3.1.0-SNAPSHOT")

    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.13")
}

// configure core dependencies, which can be reused in lphy-studio
val coreJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations["api"])
}
artifacts {
    add("coreJars", tasks.jar)
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
    withJavadocJar() // TODO Problems generating Javadoc
}

// overwrite compileJava to use module-path
tasks.compileJava {
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("Java version used is ${JavaVersion.current()}.")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }

    doLast {
        println("${project.name} compiler args = ${options.compilerArgs}")
    }
}

// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy",
        )
    }
}

// define the release folder according to version
val releaseRepoUrl = layout.buildDirectory.dir("releases")
val snapshotRepoUrl = layout.buildDirectory.dir("snapshots")
val localUrl = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl)
// delete previous release under the local build folder
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        val path: java.nio.file.Path = Paths.get(localUrl.path)
        if (Files.exists(path)) {
            println("Delete the existing previous release : ${path.toAbsolutePath()}")
            project.delete(path)
        }
    }
}
// publish to maven central
val pubId = "LPhy"
publishing {
    publications {
        create<MavenPublication>(pubId) {
            artifactId = base.archivesName.get()
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(pubId)
                description.set("A probabilistic model specification language to concisely and precisely define phylogenetic models.")
                url.set("https://linguaphylo.github.io/")
                packaging = "jar"
                properties.set(mapOf(
                    "maven.compiler.source" to java.sourceCompatibility.majorVersion,
                    "maven.compiler.target" to java.targetCompatibility.majorVersion
                ))
                licenses {
                    license {
                        name.set("GNU Lesser General Public License, version 3")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("LPhy developer team")
                    }
                }
                // https://central.sonatype.org/publish/requirements/
                scm {
                    connection.set("scm:git:git://github.com/LinguaPhylo/jebl3.git")
                    developerConnection.set("scm:git:ssh://github.com/LinguaPhylo/jebl3.git")
                    url.set("https://github.com/LinguaPhylo/jebl3/tree/master")
                }
            }
        }
    }

    repositories {
        maven {
            if (project.hasProperty("ossrh.user")) {
                // -Possrh.user=myuser -Possrh.pswd=mypswd
                val ossrhUser = project.property("ossrh.user")
                val ossrhPswd = project.property("ossrh.pswd")
                println("ossrhUser = $ossrhUser, ossrhPswd = ******") // $ossrhPswd

                // publish to maven
                val releaseOSSRH = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotOSSRH = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotOSSRH else releaseOSSRH)
                credentials {
                    username = "$ossrhUser"
                    password = "$ossrhPswd"
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }

            } else {
                // publish to local
                url = localUrl
            }

            println("Publish $base:$version to : ${url.path}")
        }
    }
}

// -Psigning.secretKeyRingFile=/path/to/mysecr.gpg -Psigning.password=mypswd -Psigning.keyId=last8chars
signing {
    sign(publishing.publications[pubId])
}

tasks.javadoc {
    // if (JavaVersion.current().isJava9Compatible)
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    doFirst {
        options.modulePath = classpath.files.toList()
        options.classpath = listOf()
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
