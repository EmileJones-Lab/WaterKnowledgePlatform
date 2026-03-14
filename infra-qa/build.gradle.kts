plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    alias(libs.plugins.kotlinPluginSerialization)
    buildsrc.convention.base
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    implementation("org.neo4j.driver:neo4j-java-driver:5.20.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":infra-model"))
}

tasks.test {
    useJUnitPlatform()
}