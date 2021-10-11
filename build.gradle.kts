// buildSrc is a trap, use composite builds
// https://docs.gradle.org/current/userguide/structuring_software_products.html

// this is the umbrella build to define cross-build lifecycle tasks.
// https://docs.gradle.org/current/userguide/structuring_software_products_details.html

allprojects {
    repositories {
        mavenCentral()
    }
}

// shared attributes
subprojects {

    tasks.withType<JavaCompile> {
        options.isWarnings = true
    }

    tasks.withType<Jar>() {
        manifest {
            attributes(
                "Implementation-Vendor" to "LPhy team",
                "Implementation-URL" to "https://github.com/LinguaPhylo/linguaPhylo",
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt()
            )
        }
    }
}

