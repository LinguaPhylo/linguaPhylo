plugins {
    application
}

group = "lphy"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":lphy"))
//    testImplementation("junit:junit:4.13")
}

var maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
    mainClass.set(maincls)
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

// overwrite compileJava to use module-path
tasks.compileJava {
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    println("Java version used is ${JavaVersion.current()}.")

    doFirst {
        println("CLASSPATH IS ${classpath.asPath}")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }
}

// make studio app locating the correct parent path of examples sub-folder
tasks.withType<JavaExec>() {
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    jvmArgs = listOf("-Duser.dir=${projectDir.parent}")
}

tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Main-Class" to maincls,
            "Implementation-Title" to "LPhyStudio"
        )
    }
}

