plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    buildsrc.convention.base
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}