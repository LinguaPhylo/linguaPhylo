plugins {
    application
}

group = "linguaPhylo"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":lphy"))
//    testImplementation("junit:junit:4.13")
}

var maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
    mainClass.set(maincls)
}

tasks.withType<JavaExec>() {
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    jvmArgs = listOf("-Duser.dir=${projectDir.parent}")
}


//====== duplicates start ======//
java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
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
//====== duplicates end ======//

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

