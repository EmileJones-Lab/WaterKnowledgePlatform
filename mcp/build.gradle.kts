plugins {
    application
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.1"))
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

    implementation(project(":env"))
    implementation(project(":model"))
    implementation(project(":service"))
    implementation(project(":neo4j"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.MainApplication"
}
