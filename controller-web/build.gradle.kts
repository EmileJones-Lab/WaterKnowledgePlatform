plugins {
    application
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    implementation(project(":application"))
    implementation(project(":common"))
}

application {
    mainClass = "top.emilejones.hhu.web.WebApplication"
}
