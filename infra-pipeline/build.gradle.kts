plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "top.emilejones"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
}

tasks.test {
    useJUnitPlatform()
}