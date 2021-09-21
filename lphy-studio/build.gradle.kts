plugins {
    application
    lphy.config
}

group = "linguaPhylo"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":lphy"))
//    testImplementation("junit:junit:4.13")
}

var maincls = "lphystudio.app.LinguaPhyloStudio"
application {
    mainClass.set(maincls)
}

tasks.jar {
    manifest {
        // shared attr in buildSrc/src/main/kotlin/lphy.config.gradle.kts
        attributes(
            "Main-Class" to maincls,
            "Implementation-Title" to "LPhyStudio",
            "Implementation-Version" to archiveVersion
        )
    }
}

