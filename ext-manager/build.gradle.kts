plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.1"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":lphy"))

    implementation("org.json:json:20210307")
}

tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy Extension Manager",
            "Implementation-Vendor" to "Walter Xie",
        )
    }
}