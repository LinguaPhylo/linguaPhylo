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
    implementation(project(":lphy-base"))

    implementation("org.scilab.forge:jlatexmath:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-greek:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
//    implementation("org.json:json:20210307")
    implementation("org.jfree:jfreechart:1.5.4")
    // in maven
    implementation("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    // for mac release
//    compileOnly("com.ultramixer.jarbundler:jarbundler-core:3.3.0")

}

val maincls : String = "lphystudio.app.LinguaPhyloStudio"
application {
    // equivalent to -m lphystudio
    // need both mainModule and mainClass
    mainModule.set("lphystudio")
    // if only mainClass, it will auto add maincls to the end of CMD
    mainClass.set(maincls)
    // applicationDefaultJvmArgs = listOf("-Dgreeting.language=en")
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
            from("$rootDir/bin") {
                into("bin")
                eachFile {
                    // fileMode 755 not working
                    file.setExecutable(true, true)
                }
            }
            from("$rootDir/examples") {
                include("**/*.lphy", "**/*.nex", "**/*.fasta")
                exclude("todo")
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
                include("language_specification.md")
            }
            // include src jar
            into("src") {
                from(tasks.sourcesJar)
                from(project(":lphy").tasks.sourcesJar)
                from(project(":lphy-base").tasks.sourcesJar)
                // icon
                from("src/main/resources") {
                    include("lphy48x48.png")
                    include("lphy512x512.icns")
                    include("lphy.ico")
                }
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


/**
 * For LPhy core, set working directory: ~/WorkSpace/linguaPhylo/lphy/doc,
 * and args[0] = version.
 * For extension, set working directory: ~/WorkSpace/$REPO/lphy/doc,
 * and args[0] = version, args[1] = extension name (no space),
 * args[2] = class name to implement LPhyExtension.
 * e.g. args = 0.0.5 "LPhy Extension Phylonco" phylonco.lphy.spi.Phylonco
 *
 * The docs will output to working dir, "user.dir"
 * This is equivalent to: java -p $LPHY/lib -m lphy/lphy.doc.GenerateDocs 1.1.0
 */
val lphyDoc = tasks.register("lphyDoc", JavaExec::class.java) {
    description = "Create LPhy doc"
    dependsOn("assemble")
//    println("user.dir = " + System.getProperty("user.dir"))

    // equivalent to: java -p ...
    jvmArgs = listOf("-p", sourceSets.main.get().runtimeClasspath.asPath,
        // set output to .../lphy/doc
        "-Duser.dir=${layout.projectDirectory.dir("doc")}")

    // -m lphystudio/lphystudio.app.docgenerator.GenerateDocs
    mainModule.set("lphystudio")
    mainClass.set("lphystudio.app.docgenerator.GenerateDocs")
    // such as 1.1.0
    setArgs(listOf("$version"))
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
