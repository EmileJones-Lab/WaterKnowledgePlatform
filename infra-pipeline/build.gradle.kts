plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
}

tasks.test {
    useJUnitPlatform()
}