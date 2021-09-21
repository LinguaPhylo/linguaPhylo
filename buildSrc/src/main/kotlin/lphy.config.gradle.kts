plugins {
    `java-library`
}
java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.isWarnings = true
}

tasks.compileJava {
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("CLASSPATH IS ${classpath.asPath}")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }
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