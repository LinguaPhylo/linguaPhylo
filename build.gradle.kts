plugins {
    application
}
// buildSrc is a trap, use composite builds
// https://proandroiddev.com/stop-using-gradle-buildsrc-use-composite-builds-instead-3c38ac7a2ab3

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
