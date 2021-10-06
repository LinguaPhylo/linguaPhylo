plugins {
    id("java-platform")
}

group = "lphy.platform"

dependencies {
    constraints {
        api("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.5.21") {
            because("?")
        }
    }
}
