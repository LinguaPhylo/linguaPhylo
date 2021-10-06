plugins {
    id("java-platform")
}

group = "lphy.platform"

// allow the definition of dependencies to other platforms like the JUnit 5 BOM
javaPlatform.allowDependencies()

dependencies {
//    api(platform("org.junit:junit-bom:5.7.1"))
    api(platform("junit:junit:4.13.2"))
}

