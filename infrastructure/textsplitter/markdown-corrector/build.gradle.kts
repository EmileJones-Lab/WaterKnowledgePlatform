plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":infrastructure:model"))
    implementation(libs.gson)
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}