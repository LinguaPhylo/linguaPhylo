// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
//        mavenCentral()
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("../platforms")

// == Define the inner structure of this component ==
rootProject.name = "lphy" // the component name
include("lphy")
