plugins {
    `java-library`
}

group = "LinguaPhylo"
version = "1.1-SNAPSHOT"

dependencies {
    api(platform("lphy.platform:product-platform"))

    testImplementation(platform("lphy.platform:test-platform"))
}

tasks.jar {
    manifest {
        // shared attr in build-logic/src/main/kotlin/lphy.core.gradle.kts
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
