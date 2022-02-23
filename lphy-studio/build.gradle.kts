plugins {
    application
    distribution
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.2"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.2"
}

//version = "1.2.0"
//base.archivesName.set("studio")

dependencies {
//    implementation(project(mapOf( "path" to ":lphy", "configuration" to "coreJars")))
    implementation(project(":lphy"))
    implementation(project(":ext-manager"))

    implementation("org.scilab.forge:jlatexmath:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-greek:1.0.7")
//    implementation("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")

//    testImplementation("junit:junit:4.13")
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
    systemProperty("user.dir", rootDir)
    doFirst {
        // equivalent to: java -p ...
        jvmArgs = listOf("-p", classpath.asPath)
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
