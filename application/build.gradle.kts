plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "top.emilejones"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")
}

tasks.test {
    useJUnitPlatform()
}