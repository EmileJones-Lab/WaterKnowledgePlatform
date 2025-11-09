plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
}