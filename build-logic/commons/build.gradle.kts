plugins {
    id("java-library")
}

group = "lphy.core"

dependencies {
    api(platform("lphy.platform:product-platform"))

    testImplementation(platform("lphy.platform:test-platform"))
//    testImplementation("org.junit.jupiter:junit-jupiter-api")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// shared attributes
subprojects {
    tasks.withType<Jar>() {
        manifest {
            attributes (
                "Implementation-Vendor" to "LPhy team",
                "Implementation-URL" to "https://github.com/LinguaPhylo/linguaPhylo",
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt()
            )
        }
    }
}
