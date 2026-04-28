plugins {
    kotlin("jvm")
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

    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

    implementation(project(":common"))
    implementation(project(":infrastructure:model"))
    implementation(project(":infrastructure:textsplitter"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.McpApplication"
}
