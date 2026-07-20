plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.jrom.mynextfavartist.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "MUSICBRAINZ_BASE_URL", "\"https://musicbrainz.org/ws/2/\"")
        // MusicBrainz requires a meaningful User-Agent with a contact string.
        val mbContact = (project.findProperty("mbContact") as? String) ?: "contact@example.com"
        buildConfigField("String", "MUSICBRAINZ_CONTACT", "\"$mbContact\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    // NetworkMonitorImpl's ConnectivityManager.NetworkCallback overrides call super.onLost()/
    // super.onCapabilitiesChanged(), which throw "Stub!" from the android.jar unit-test stub
    // unless unstubbed methods are allowed to return a default (no-op for Unit) instead.
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    testImplementation(project(":test-utils"))
    api(project(":domain"))
    implementation(project(":core-di"))

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ArtistDaoTest runs against a real in-memory Room database, so it needs an actual device/
    // emulator - Room's Flow invalidation tracker (does saveArtist() cause observeAllArtists()
    // to re-emit?) isn't expressible against a mocked ArtistDao.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.room.testing)
}
