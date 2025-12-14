plugins {
    alias(libs.plugins.kotlinPluginSerialization)
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