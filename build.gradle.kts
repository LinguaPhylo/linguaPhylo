// buildSrc is a trap, use composite builds
// https://docs.gradle.org/current/userguide/structuring_software_products.html

// this is the umbrella build to define cross-build lifecycle tasks.
// https://docs.gradle.org/current/userguide/structuring_software_products_details.html

import java.text.SimpleDateFormat
import java.util.Calendar

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {

    tasks.withType<JavaCompile> {
        options.isWarnings = true
    }

    var calendar: Calendar? = Calendar.getInstance()
    var formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

    // shared attributes
    tasks.withType<Jar>() {
        manifest {
            attributes(
                "Implementation-Vendor" to "LPhy team",
                "Implementation-Version" to archiveVersion,
                "Implementation-URL" to "https://github.com/LinguaPhylo/linguaPhylo",
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt(),
                "Built-Date" to formatter.format(calendar?.time)
            )
        }
    }
}

