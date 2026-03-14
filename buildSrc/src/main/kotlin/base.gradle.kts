// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.5.5"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.1"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
}