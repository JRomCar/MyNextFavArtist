plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    // test-utils is an android.library (its InstantTaskExecutorRule dependency, core-testing,
    // ships as an AAR that only AGP can unpack), so a plain kotlin("jvm") module can't consume
    // it. domain's tests don't use TestBase/InstantTaskExecutorRule anyway - just these directly.
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
}
