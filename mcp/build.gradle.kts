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
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation(project(":env"))
    implementation(project(":utils"))
    implementation(project(":web"))
}

application {
    mainClass = "top.emilejones.hhu.mcp.MainApplication"
}
