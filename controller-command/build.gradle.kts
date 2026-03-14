plugins {
    // Apply the Application plugin to add support for building an executable JVM application.
    application
    buildsrc.convention.base
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(project(":application"))
    implementation(project(":infra-textsplitter"))
    implementation(project(":infra-model"))
    implementation(project(":common"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "top.emilejones.hhu.MainKt"
}