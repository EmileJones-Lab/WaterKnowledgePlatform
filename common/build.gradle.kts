plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
    id("buildsrc.convention.kotlin-base")
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.mybatisStarter)
    implementation(libs.mysqlConnector)
    implementation(libs.milvusSdk)
    implementation(libs.minio)
    implementation(libs.neo4jDriver)
    implementation(libs.commonsCodec)
}
    