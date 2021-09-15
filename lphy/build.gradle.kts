plugins {
    `java-library`
}
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(16)) }
}

group = "linguaPhylo"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    //flatDir { dirs("lib") }
}

dependencies {
    // required in test
    api("org.antlr:antlr4-runtime:4.8")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.10")
    // in maven
    api("org.scilab.forge:jlatexmath:1.0.7")
    api("org.scilab.forge:jlatexmath-font-greek:1.0.7")
    api("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
    api("net.steppschuh.markdowngenerator:markdowngenerator:1.3.2")
    // not in maven
    api(files("libs/jebl-3.0.1.jar"))
    //implementation(fileTree("lib") { exclude("junit-*.jar") })

    testImplementation("junit:junit:4.8")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.8")
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

tasks.jar {
    manifest {
        attributes(
                "Implementation-Title" to "LPhy",
                "Implementation-Version" to archiveVersion,
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt()
        )
    }
}

//tasks.getByName<Test>("test") {
//    useJUnitPlatform()
//}

tasks.test {
    useJUnit()
    maxHeapSize = "1G"

    filter {
        excludeTestsMatching("ParserTest") // TODO SPI?
    }
}