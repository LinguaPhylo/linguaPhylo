// buildSrc is a trap, use composite builds
// https://docs.gradle.org/current/userguide/structuring_software_products.html

// this is the umbrella build to define cross-build lifecycle tasks.
// https://docs.gradle.org/current/userguide/structuring_software_products_details.html

import java.text.SimpleDateFormat
import java.util.*

plugins {
    `java-library`
    `maven-publish`
}

// Configures this project and each of its sub-projects.
allprojects {
    repositories {
        mavenCentral()
        // add sonatype snapshots repository
        maven {
            url=uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        // Managing plugin versions via pluginManagement in settings.gradle.kts
//        mavenLocal() // only for testing
    }

    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
    }

    tasks.withType(Javadoc::class.java) {
        options.encoding = "UTF-8"
    }

}

// disable build folder at root project
tasks.jar {enabled = false}
tasks.build {enabled = false}

// Configures the sub-projects of this project.
subprojects {
    group = "io.github.linguaphylo"
    version = "1.5.0-SNAPSHOT" //-SNAPSHOT
    val webSteam = "github.com/LinguaPhylo/linguaPhylo"
    val web = "https://${webSteam}"
    val homepage = "https://linguaphylo.github.io/"

    var calendar: Calendar? = Calendar.getInstance()
    var formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

    // this task call GenerateMavenPom task to create pom.xml
    // and copy it to classes/java/main, so do not need to guess path when module.getResourceAsStream()
    val copyPom by tasks.registering(Copy::class) {
        dependsOn(tasks.withType(GenerateMavenPom::class))
        from(layout.buildDirectory.dir("publications/${project.name}"))
        include("**/*.xml")
        into(layout.buildDirectory.dir("classes/java/main"))
//        into(layout.buildDirectory.dir("resources/main/META-INF/maven/${project.group}/${project.name}"))
        rename("pom-default.xml", "pom.xml")
    }

    // shared attributes
    tasks.withType<Jar>() {
        // this includes pom.xml in the jar
        dependsOn(copyPom)

        manifest {
            attributes(
                "Implementation-Version" to archiveVersion,
                "Implementation-URL" to web,
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt(),
                "Built-Date" to formatter.format(calendar?.time)
            )
        }
        // copy LICENSE to META-INF
        metaInf {
            from(rootDir) {
                include("LICENSE")
            }
        }

    }

    // configure the shared contents in MavenPublication especially POM
    afterEvaluate{
        extensions.configure<PublishingExtension>{
            publications {
                withType<MavenPublication>().all() {
                    // only for name.contains("lphy")
                    if (name.contains("lphy") || name.contains("manager")) {
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
//                        description.set("...")
                            // compulsory
                            url.set(homepage)
                            packaging = "jar"
                            properties.set(
                                mapOf(
                                    "maven.compiler.source" to java.sourceCompatibility.majorVersion,
                                    "maven.compiler.target" to java.targetCompatibility.majorVersion
                                )
                            )
                            licenses {
                                license {
                                    name.set("GNU Lesser General Public License, version 3")
                                    url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                                }
                            }
//                        developers {
// ...
//                        }
                            // https://central.sonatype.org/publish/requirements/
                            scm {
                                connection.set("scm:git:git://${webSteam}.git")
                                developerConnection.set("scm:git:ssh://${webSteam}.git")
                                url.set(web)
                            }
                        }
                        println("Define MavenPublication ${name} and set shared contents in POM")
                    }
                }
            }
        }
    }

}

