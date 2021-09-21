plugins {
    lphy.config
}

group = "linguaPhylo"
version = "1.1-SNAPSHOT"

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
    // not in maven
    api(files("libs/jebl-3.0.1.jar"))
    //implementation(fileTree("lib") { exclude("junit-*.jar") })

    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.13")
}
repositories {
    mavenCentral()
}

tasks.jar {
    manifest {
        // shared attr in buildSrc/src/main/kotlin/lphy.config.gradle.kts
        attributes(
            "Implementation-Title" to "LPhy",
            "Implementation-Version" to archiveVersion,
            "Built-Date" to System.currentTimeMillis()
        )
    }
}

tasks.test {
    useJUnit()
    // useJUnitPlatform()
    maxHeapSize = "1G"
}
