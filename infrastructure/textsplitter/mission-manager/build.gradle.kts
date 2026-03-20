plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.lombok")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infrastructure:environment"))
    implementation(libs.mysqlConnector)
    implementation(libs.mybatisStarter)
    implementation(libs.minio)
}

tasks.test {
    useJUnitPlatform()
}