plugins {
    application
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("org.springframework.boot:spring-boot-starter-parent:3.5.5"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation(project(":service"))
    implementation(project(":milvus"))
    implementation(project(":neo4j"))
    implementation(project(":env"))
    implementation(project(":model"))
}

application {
    mainClass = "top.emilejones.hhu.web.WebApplication"
}
