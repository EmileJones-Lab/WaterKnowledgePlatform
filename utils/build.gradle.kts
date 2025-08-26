plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}