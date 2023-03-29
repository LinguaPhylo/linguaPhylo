plugins {
    application
    distribution
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.2"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.2"
    id("org.panteleyev.jpackageplugin") version "1.5.1"
}

//version = "1.2.0"
//base.archivesName.set("studio")

dependencies {
//    implementation(project(mapOf( "path" to ":lphy", "configuration" to "coreJars")))
    implementation(project(":lphy"))

    implementation("org.scilab.forge:jlatexmath:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-greek:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
//    implementation("org.json:json:20210307")
    implementation("org.jfree:jfreechart:1.5.4")

    implementation("info.picocli:picocli:4.7.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

val maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
    // equivalent to -m lphystudio
    // need both mainModule and mainClass
    mainModule.set("lphystudio")
    // if only mainClass, it will auto add maincls to the end of CMD
    mainClass.set(maincls)
}

// make studio app locating the correct parent path of examples sub-folder
tasks.withType<JavaExec>() {
    // set version into system property
    systemProperty("lphy.studio.version", version)
    // projectDir = ~/WorkSpace/linguaPhylo/lphy-studio/
    // rootDir = projectDir.parent = ~/WorkSpace/linguaPhylo
    // user.dir = ~/WorkSpace/linguaPhylo/, so examples can be loaded properly
    doFirst {
        // equivalent to: java -p ...
        // user.dir=rootDir (~/WorkSpace/linguaPhylo/), so examples can be loaded properly
        jvmArgs = listOf("-p", classpath.asPath, "-Duser.dir=${rootDir}")
        classpath = files()
    }
    doLast {
        println("JavaExec : $jvmArgs")
    }
}

val developers = "LPhy developer team"
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Main-Class" to maincls,
            "Implementation-Title" to "LPhyStudio",
            "Implementation-Vendor" to developers,
        )
    }
}

task("copyDependencies", Copy::class) {
    from(configurations.compileClasspath.get().files).into("$buildDir/jmods")
    from(tasks.jar).into("$buildDir/jmods")
}

tasks.jpackage {
    dependsOn("copyDependencies")
//    mustRunAfter("copyDependencies")

    appName = "LPhyStudio"//"${project.name}-${project.version}"
    appVersion = project.version.toString().removeSuffix("-SNAPSHOT")
    appDescription = "The GUI for LPhy language."
    vendor = "io.github.linguaphylo"
    copyright = "Copyright (c) 2023 LPhy team"
    runtimeImage = System.getProperty("java.home")
    module = "lphystudio/$maincls"
    modulePaths = fileTree("$buildDir/jmods").map{ it.canonicalPath } //configurations.compileClasspath.get().files.map{ it.canonicalPath }
    destination = "$buildDir/distributions/"
    javaOptions = listOf("-Dfile.encoding=UTF-8")

    mac {
        icon = "$rootDir/icons/lphy.icns"
    }

    windows {
        icon = "$rootDir/icons/lphy.ico"
        winMenu = true
        winDirChooser = true
    }

    doFirst {
        println("jpackage module path :")
        println(configurations.compileClasspath.get().files)
//        println(fileTree("$buildDir/distributions/${project.name}-${project.version}/lib").map{ it.canonicalPath })
    }
}

tasks.getByName<Tar>("distTar").enabled = false
// exclude start scripts, Gradle is terrible to handle module path
tasks.getByName<CreateStartScripts>("startScripts").enabled = false

// copy related files and Zip
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
                include("LICENSE")
            }
            // include src jar
            into("src") {
                from(tasks.sourcesJar)
                from(project(":lphy").tasks.sourcesJar)
            }
            //TODO ext manager
        }
    }
}

publishing {
    publications {
        // project.name contains "lphy" substring
        create<MavenPublication>(project.name) {
            artifactId = project.base.archivesName.get()
            pom {
                description.set("The GUI for LPhy language.")
                developers {
                    developer {
                        name.set(developers)
                    }
                }
            }
        }

    }
}

// junit tests, https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html
tasks.test {
    useJUnitPlatform() {
        excludeTags("dev")
    }
    // set heap size for the test JVM(s)
    minHeapSize = "128m"
    maxHeapSize = "1G"
    // show standard out and standard error of the test JVM(s) on the console
    testLogging.showStandardStreams = true

    reports {
        junitXml.apply {
            isOutputPerTestCase = true // defaults to false
            mergeReruns.set(true) // defaults to false
        }
    }
}
