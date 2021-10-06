plugins {
    id("java-platform")
}

group = "lphy.platform"

// allow the definition of dependencies to other platforms
javaPlatform.allowDependencies()

repositories {
    mavenCentral()
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
    api("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")
    // not in maven
//    api(files("libs/jebl-3.0.1.jar"))
    //implementation(fileTree("lib") { exclude("junit-*.jar") })

    constraints {
//        api("?:?:8.2.0")
    }
}
