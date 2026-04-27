plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.neo4jDriver)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(project(":infrastructure:model"))
}

tasks.test {
    useJUnitPlatform()
}