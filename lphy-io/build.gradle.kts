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
    implementation(project(":lphy"))
    implementation(project(":lphy-base"))

    // command line
    implementation("info.picocli:picocli:4.7.1")

//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

val developers = "LPhy developer team"
// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy IO",
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
                description.set("The IO library of LPhy, including simulator application SLPhy.")
                developers {
                    developer {
                        name.set(developers)
                    }
                }
            }
        }
    }
}

