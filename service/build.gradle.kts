plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("java")
}

group = "top.emilejones"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation(project(":utils"))
    implementation(project(":env"))
}

tasks.test {
    useJUnitPlatform()
}