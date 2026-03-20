// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import org.gradle.kotlin.dsl.version

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
}

dependencies {
    // 版本管理
    implementation(platform("org.springframework.ai:spring-ai-bom:1.1.2"))
    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.5.5"))


    // spring-boot基础依赖
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }
}