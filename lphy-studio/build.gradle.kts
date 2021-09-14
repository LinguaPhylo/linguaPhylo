plugins {
    application
}

group = "linguaPhylo"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lphy"))
//
//    implementation("org.antlr:antlr4-runtime:4.8")
//    implementation("org.apache.commons:commons-math3:3.6.1")
//    implementation("org.apache.commons:commons-lang3:3.10")
//    // in maven
//    compileOnly("org.scilab.forge:jlatexmath:1.0.7")
//    compileOnly("org.scilab.forge:jlatexmath-font-greek:1.0.7")
//    compileOnly("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
//    // not in maven
//    implementation(files("lib/markdowngenerator-1.3.2.jar")) //TODO not used?
//    implementation(files("lib/jebl-3.0.1.jar"))

//    testImplementation("junit:junit:4.8")
}

//tasks.compileJava {
//    doFirst {
//        println("CLASSPATH IS ${classpath.asPath}")
//        options.compilerArgs = listOf("--module-path", classpath.asPath)
//        classpath = files()
//    }
//}


tasks.jar {
    manifest {
        attributes(
                "Implementation-Title" to "LPhyStudio",
                "Implementation-Version" to archiveVersion,
                "Main-Class" to "lphystudio.app.LinguaPhyloStudio",
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt()
        )
    }
}

