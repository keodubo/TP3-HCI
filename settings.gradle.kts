pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Ensure the Kotlin Gradle plugin used by the build matches the project catalog.
    // This forces Kotlin plugin resolution to 1.9.25 so the Compose compiler (1.5.x)
    // which requires Kotlin 1.9.25 is compatible with the applied plugin.
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion("1.9.25")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Comprartir"
include(":app")
