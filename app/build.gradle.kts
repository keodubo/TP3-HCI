import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = gradleLocalProperties(rootDir, providers)
val envProperties = Properties().apply {
    val envFile = rootProject.file(".env.android")
    if (envFile.exists()) {
        envFile.inputStream().use(::load)
    }
}

fun resolveEnvProperty(key: String): String? {
    return when {
        localProperties.containsKey(key) -> localProperties.getProperty(key)
        envProperties.containsKey(key) -> envProperties.getProperty(key)
        else -> null
    }
}

fun apiBaseUrl(flavor: String): String = resolveEnvProperty("comprartir.apiBaseUrl.$flavor")
    ?: resolveEnvProperty("comprartir.apiBaseUrl")
    ?: System.getenv("COMPRARTIR_API_BASE_URL")
    ?: "http://10.0.2.2:8080/api"

android {
    namespace = "com.comprartir.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.comprartir.mobile"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // TODO: Replace with app-specific rules once ready for release builds.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    flavorDimensions += "device"
    productFlavors {
        create("phone") {
            dimension = "device"
            // TODO: Customize resources and layout behavior for phone-specific nuances if needed.
            buildConfigField(
                "String",
                "COMPRARTIR_API_BASE_URL",
                "\"${apiBaseUrl("phone")}\""
            )
        }
        create("tablet") {
            dimension = "device"
            // TODO: Customize resources and layout behavior for tablet-specific nuances if needed.
            buildConfigField(
                "String",
                "COMPRARTIR_API_BASE_URL",
                "\"${apiBaseUrl("tablet")}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn",
            // Allow experimental Material3/window-size-class APIs used in the codebase
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            // Correct opt-in annotation package for window size class
            "-Xopt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
    // Use the Compose compiler extension version originally chosen for this project.
    // Align with Compose 1.5.x series (use latest 1.5.x compiler available)
    kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    // Provide Android Material components styles (Theme.Material3.* parents used by themes.xml)
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    kapt(libs.hilt.compiler)
    kapt(libs.androidx.room.compiler)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
}

kapt {
    correctErrorTypes = true
}

// Force Compose 1.5.x artifacts to avoid accidental upgrades from other libraries
// Improve Compose pinning: ensure any androidx.compose.* module resolves to the chosen 1.5.4
// This covers transitive modules like ui-util, ui-geometry, ui-tooling-data which otherwise
// pulled 1.6.0 artifacts and produced duplicate-class errors.
configurations.all {
    resolutionStrategy {
        // Pin commonly-used Compose groups to 1.5.4. Avoid pinning the broad
        // "androidx.compose" group (used by the BOM) which has different versioning.
        eachDependency {
            val g = requested.group
            if (g == "androidx.compose.ui" || g == "androidx.compose.material" || g == "androidx.compose.animation" || g == "androidx.compose.foundation" || g == "androidx.compose.runtime") {
                useVersion("1.5.4")
            } else if (g == "androidx.compose.material3") {
                useVersion("1.1.2")
            }
        }

        // Keep an explicit force list for some common entry points as an extra safeguard
        force(
            "androidx.compose.runtime:runtime:1.5.4",
            "androidx.compose.ui:ui:1.5.4",
            "androidx.compose.ui:ui-android:1.5.4",
            "androidx.compose.ui:ui-util:1.5.4",
            "androidx.compose.ui:ui-util-android:1.5.4",
            "androidx.compose.ui:ui-geometry:1.5.4",
            "androidx.compose.ui:ui-geometry-android:1.5.4",
            "androidx.compose.ui:ui-tooling-data:1.5.4",
            "androidx.compose.ui:ui-tooling-data-android:1.5.4",
            "androidx.compose.material:material-ripple:1.5.4",
            "androidx.compose.material:material-ripple-android:1.5.4",
            "androidx.compose.material3:material3:1.1.2",
            "androidx.compose.material3:material3-android:1.1.2",
            "androidx.compose.material3:material3-window-size-class:1.1.2",
            "androidx.compose.material3:material3-window-size-class-android:1.1.2"
        )
    }
}
