import java.nio.file.*

plugins {
    application
    distribution
    `maven-publish`
//    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.0-SNAPSHOT"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.0-SNAPSHOT"
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

publishing {
    publications {
        // must have "lphy" substring in the name
        create<MavenPublication>("lphy-studio") {
            artifactId = project.base.archivesName.get()
            from(components["java"])
            // Configures the version mapping strategy
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(project.name)
                description.set("The GUI for LPhy language.")
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
                        name.set("Alexei Drummond")
                    }
                    developer {
                        name.set("Walter Xie")
                    }
                }
                // https://central.sonatype.org/publish/requirements/
                scm {
                    connection.set("scm:git:git://github.com/LinguaPhylo/linguaPhylo.git")
                    developerConnection.set("scm:git:ssh://github.com/LinguaPhylo/linguaPhylo.git")
                    url.set("https://github.com/LinguaPhylo/linguaPhylo")
                }
            }
        }

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
                include("LICENSE")
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
