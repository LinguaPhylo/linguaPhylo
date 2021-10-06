plugins {
    id("java-library")
}

dependencies {
    // constraints for the environments: plugins
    implementation(platform("lphy.platform:plugins-platform"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<JavaCompile>() {
    options.isWarnings = true
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("CLASSPATH is ${classpath.asPath}")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }
}

tasks.withType<JavaExec>() {
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    jvmArgs = listOf("-Duser.dir=${projectDir.parent}")
}
