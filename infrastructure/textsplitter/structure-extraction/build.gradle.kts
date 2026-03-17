plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.milvusSdk)
    implementation(libs.neo4jDriver)
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infrastructure:model"))
    implementation(project(":infrastructure:textsplitter:markdown-corrector"))
}

tasks.test {
    useJUnitPlatform()
}
