plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("java")
    application
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.1"))
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

    implementation("io.milvus:milvus-sdk-java:2.6.3")
    implementation("org.neo4j.driver:neo4j-java-driver:5.20.0")

    implementation(project(":env"))
    implementation(project(":model"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.MainApplication"
}
