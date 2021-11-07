plugins {
    application
    distribution
    `maven-publish`
}

group = "lphy"
version = "1.1.0-a.1"
dependencies {
    implementation(project(mapOf( "path" to ":lphy", "configuration" to "coreJars")))
//    testImplementation("junit:junit:4.13")
}

var maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
//    mainModule.set("lphystudio")
    mainClass.set(maincls)
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
//    withJavadocJar()
}

// overwrite compileJava to use module-path
tasks.compileJava {
//    dependsOn(":lphy:compileJava")
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("Java version used is ${JavaVersion.current()}.")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }

    doLast {
        println("${project.name} compiler args = ${options.compilerArgs}")
    }
}

// make studio app locating the correct parent path of examples sub-folder
tasks.withType<JavaExec>() {
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // rootDir = projectDir.parent = ~/WorkSpace/linguaPhylo
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    jvmArgs = listOf("-Duser.dir=${rootDir}")//, "-m lphystudio")
    // set version into system property
    systemProperty("lphy.studio.version", version)
    doLast {
        println("JavaExec : $jvmArgs")
    }
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

distributions {
    main {
        contents {
            from("$rootDir/examples") {
                include("**/*.lphy", "**/*.nex")
                exclude("todo", "**/*covid*")
                into("examples")
            }
            from("$rootDir/tutorials") {
                // add new tutorial here
                include("h3n2.lphy","h5n1.lphy","hcv_coal.lphy","hcv_coal_classic.lphy",
                    "RSV2.lphy","RSV2sim.lphy", "**/*.nex", "**/*.nexus")//, "**/*.fasta"
                exclude("**/*canis*")
                into("tutorials")
            }
            from("$rootDir") {
                include("README.md")
//                include("LICENSE")
            }
            // include src jar
            from(layout.buildDirectory.dir("libs")) {
                include("*-sources.jar")
                into("src")
            }
            from(project(":lphy").layout.buildDirectory.dir("libs")) {
                include("*-sources.jar")
                into("src")
            }
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("LPhyStudio") {
            from(components["java"])
        }
    }

    val releaseDir = "releases"
    repositories {
        maven {
            name = releaseDir
            url = uri(layout.buildDirectory.dir("${rootDir}/${releaseDir}"))
            println("Set the base URL of $releaseDir repository to : ${url.path}")
        }
    }
}
