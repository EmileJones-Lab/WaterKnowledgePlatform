plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infra-textsplitter"))
    implementation(project(":infra-pipeline"))
    implementation(project(":infra-document"))
    implementation(project(":infra-knowledge"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")
}

tasks.test {
    useJUnitPlatform()
}