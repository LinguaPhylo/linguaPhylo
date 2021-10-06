// this is the umbrella build to define cross-build lifecycle tasks.
// https://docs.gradle.org/current/userguide/structuring_software_products_details.html

tasks.register("checkFeatures") {
    group = "verification"
    description = "Run all feature tests"
//    dependsOn(gradle.includedBuild("admin-feature").task(":config:check"))
//    dependsOn(gradle.includedBuild("user-feature").task(":data:check"))
//    dependsOn(gradle.includedBuild("user-feature").task(":table:check"))
}

// buildSrc is a trap, use composite builds
// https://docs.gradle.org/current/userguide/structuring_software_products.html

