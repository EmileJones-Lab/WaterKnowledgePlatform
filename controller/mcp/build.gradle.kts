plugins {
    application
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
}

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

    implementation(project(":common"))
    implementation(project(":infrastructure:model"))
    implementation(project(":infrastructure:textsplitter"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.McpApplication"
}
