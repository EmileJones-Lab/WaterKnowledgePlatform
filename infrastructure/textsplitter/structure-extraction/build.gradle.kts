plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    libs.bundles.springBoms.get().forEach {
        implementation(platform(it))
    }
    implementation(libs.springBootStarter)
    testImplementation(libs.springBootStarterTest) {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.milvusSdk)
    implementation(libs.neo4jDriver)
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infrastructure:environment"))
    implementation(project(":infrastructure:model"))
    implementation(project(":infrastructure:textsplitter:markdown-corrector"))
}

tasks.test {
    useJUnitPlatform()
}
