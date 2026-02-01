plugins {
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("buildsrc.convention.kotlin-jvm")
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.25"
}

allprojects {
    group = "top.emilejones"
    version = "unspecified" // Or specify a central version here
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.13")
        implementation("ch.qos.logback:logback-classic:1.5.6")
        implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.5.5"))
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }
}