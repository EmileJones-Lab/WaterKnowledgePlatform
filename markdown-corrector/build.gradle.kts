plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":infra-model"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}