plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.lombok")
}

dependencies {
    libs.bundles.springBoms.get().forEach {
        implementation(platform(it))
    }
    implementation(libs.springBootStarter)
    testImplementation(libs.springBootStarterTest) {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }

    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":application-service:configuration"))
    implementation("org.springframework:spring-tx")
}