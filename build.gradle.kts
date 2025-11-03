plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

tasks.register("assembleReleaseApk") {
    group = "build"
    description = "Assembles all release APKs for every flavor."
    dependsOn(":app:assemblePhoneRelease", ":app:assembleTabletRelease")
}
