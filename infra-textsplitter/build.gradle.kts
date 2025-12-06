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
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.neo4j.driver:neo4j-java-driver:5.28.9")
    implementation("io.milvus:milvus-sdk-java:2.6.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(project(":infra-model"))
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":markdown-corrector"))
}

tasks.test {
    useJUnitPlatform()
}
