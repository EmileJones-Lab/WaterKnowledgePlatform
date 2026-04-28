plugins {
    kotlin("jvm")
    // Apply the Application plugin to add support for building an executable JVM application.
    application
    id("buildsrc.convention.base")
}

dependencies {
    libs.bundles.springBoms.get().forEach {
        implementation(platform(it))
    }
    implementation(libs.springBootStarter)
    testImplementation(libs.springBootStarterTest) {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }

    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(project(":application-service:command"))
    runtimeOnly(project(":infrastructure:textsplitter"))
    runtimeOnly(project(":infrastructure:document"))
    runtimeOnly(project(":infrastructure:knowledge"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "top.emilejones.hhu.command.MainKt"
}