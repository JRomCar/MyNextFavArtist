plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.jrom.mynextfavartist"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.jrom.mynextfavartist"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// MusicBrainz blocks generic/placeholder User-Agents, and data/build.gradle.kts silently falls
// back to a placeholder contact when -PmbContact isn't supplied (useful for local debug builds).
// A real release must not ship that placeholder, so fail the release-packaging tasks specifically
// - not every Gradle invocation - if the property is missing.
run {
    // Captured as a true local (not a script-class property) so the doFirst closure below
    // holds the Provider by value instead of an outer reference to the script object, which
    // the configuration cache can't serialize.
    val mbContactProvider = providers.gradleProperty("mbContact")
    tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
        doFirst {
            check(mbContactProvider.isPresent) {
                "mbContact Gradle property must be set for release builds, e.g. " +
                    "-PmbContact=you@example.com (MusicBrainz blocks generic/placeholder User-Agents)."
            }
        }
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":ui"))
    implementation(project(":core-di"))
    testImplementation(project(":test-utils"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.dagger.android)
    ksp(libs.hilt.dagger.compiler)

    // Coil3 - App.kt sets the SingletonImageLoader.Factory
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}