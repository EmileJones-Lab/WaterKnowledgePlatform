plugins {
    application
    buildsrc.convention.base
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

    implementation(project(":common"))
    implementation(project(":infra-model"))
    implementation(project(":infra-textsplitter"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.MainApplication"
}
