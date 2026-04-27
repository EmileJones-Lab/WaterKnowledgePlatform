plugins {
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(project(":common"))
    implementation(project(":infrastructure:environment"))
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation("org.springframework.ai:spring-ai-openai")
}