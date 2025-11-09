plugins {
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("buildsrc.convention.kotlin-jvm")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.13")
        implementation("ch.qos.logback:logback-classic:1.5.6")
    }
}