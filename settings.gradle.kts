// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed. The component builds work independently.

rootProject.name = "LinguaPhylo"

//include("platforms")
//include("build-logic")
include("lphy")
include("lphy-studio")

// https://docs.gradle.org/current/userguide/build_cache.html
// https://docs.gradle.org/current/userguide/build_cache_use_cases.html
buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
        println("Creating local build cache : ${directory}")
    }
}
