plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("java")
    application
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.5.5"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("io.milvus:milvus-sdk-java:2.6.3")
    implementation("org.neo4j.driver:neo4j-java-driver:5.20.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation(project(":env"))
    implementation(project(":utils"))
}

application {
    mainClass = "top.emilejones.hhu.web.WebApplication"
}
