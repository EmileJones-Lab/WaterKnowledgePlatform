plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.lombok")
}

dependencies {
    implementation(platform(libs.spring.statemachine.bom))
    implementation(libs.spring.statemachine.starter)
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infrastructure:textsplitter"))
    implementation(project(":infrastructure:document"))
    implementation(project(":infrastructure:knowledge"))
    implementation(project(":application-service:configuration"))
    implementation("org.springframework:spring-tx")
}