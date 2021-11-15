import java.nio.file.*

plugins {
    application
    distribution
    `maven-publish`
    signing
}

version = "1.1.0-SNAPSHOT"

dependencies {
    implementation(project(mapOf( "path" to ":lphy", "configuration" to "coreJars")))
//    testImplementation("junit:junit:4.13")
}

var maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
//    mainModule.set("lphystudio")
    mainClass.set(maincls)
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
//    withJavadocJar()
}

// overwrite compileJava to use module-path
tasks.compileJava {
//    dependsOn(":lphy:compileJava")
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

// make studio app locating the correct parent path of examples sub-folder
tasks.withType<JavaExec>() {
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // rootDir = projectDir.parent = ~/WorkSpace/linguaPhylo
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    jvmArgs = listOf("-Duser.dir=${rootDir}")//, "-m lphystudio")
    // set version into system property
    systemProperty("lphy.studio.version", version)
    doLast {
        println("JavaExec : $jvmArgs")
    }
}

tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Main-Class" to maincls,
            "Implementation-Title" to "LPhyStudio"
        )
    }
}

// copy related files and Zip
distributions {
    main {
        contents {
            from("$rootDir/examples") {
                include("**/*.lphy", "**/*.nex")
                exclude("todo", "**/*covid*")
                into("examples")
            }
            from("$rootDir/tutorials") {
                // add new tutorial here
                include("h3n2.lphy","h5n1.lphy","hcv_coal.lphy","hcv_coal_classic.lphy",
                    "RSV2.lphy","RSV2sim.lphy", "**/*.nex", "**/*.nexus")//, "**/*.fasta"
                exclude("**/*canis*")
                into("tutorials")
            }
            from("$rootDir") {
                include("README.md")
//                include("LICENSE")
            }
            // include src jar
            from(layout.buildDirectory.dir("libs")) {
                include("*-sources.jar")
                into("src")
            }
            from(project(":lphy").layout.buildDirectory.dir("libs")) {
                include("*-sources.jar")
                into("src")
            }
        }
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
val pubId = "LPhyStudio"
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
                description.set("The Java Evolutionary Biology Library using the Java Platform Module System.")
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
                        name.set("Alexei Drummond")
                    }
                    developer {
                        name.set("Walter Xie")
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
}
