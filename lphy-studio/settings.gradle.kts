// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

includeBuild("../platforms")
includeBuild("../lphy")

// == Define the inner structure of this component ==
rootProject.name = "lphy-studio" // the component name
include("lphystudio")
