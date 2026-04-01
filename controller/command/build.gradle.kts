plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    application
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(project(":application-service:command"))
    implementation(project(":application-service:platform"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "top.emilejones.hhu.command.MainKt"
}