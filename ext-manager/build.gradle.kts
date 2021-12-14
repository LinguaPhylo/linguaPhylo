plugins {
    application
//    distribution
    `maven-publish`
    signing
    id("io.github.linguaphylo.platforms.lphy-java") version "0.1.1"
    id("io.github.linguaphylo.platforms.lphy-publish") version "0.1.1"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":lphy"))

    implementation("org.json:json:20210307")
}

var maincls : String = "lphyext.app.ExtManagerApp"
application {
    // need both mainModule and mainClass
    mainModule.set("extmanager")
    // if only mainClass, it will auto add maincls to the end of CMD
    mainClass.set(maincls)
}

tasks.withType<JavaExec>() {
    // set version into system property
    systemProperty("lphy.ext.manager.version", version)
    // projectDir = ~/WorkSpace/linguaPhylo/ext-manager/
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

tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Main-Class" to maincls,
            "Implementation-Title" to "LPhy Extension Manager",
            "Implementation-Vendor" to "Walter Xie",
        )
    }
}

publishing {
    publications {
        // project.name contains "manager" substring
        create<MavenPublication>(project.name) {
            artifactId = project.base.archivesName.get()
            pom {
                description.set("The GUI to manage and download the LPhy extensions.")
                developers {
                    developer {
                        name.set("Walter Xie")
                    }
                }
            }
        }

    }
}

tasks.getByName<Tar>("distTar").enabled = false
tasks.getByName<CreateStartScripts>("startScripts").enabled = false

