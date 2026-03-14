plugins {
    application
    buildsrc.convention.base
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")

    implementation(project(":application"))
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "top.emilejones.hhu.web.WebApplication"
}

tasks.register<Exec>("deploy-lab") {
    group = "deployment"
    description = "Deploy and restart the application on the remote server 10.196.83.122"
    dependsOn("installDist")
    
    val remoteHost = "10.196.83.122"
    val userName = "emilejones"
    val remotePath = "~/Software/rag/application"
    val localDist = "${layout.buildDirectory.get()}/install/controller-web"
    val workDir = "~/Software/rag"
    val scriptName = "web-server.sh"

    commandLine(
        "sh", "-c",
        """
            ssh $userName@$remoteHost 'rm -rf $remotePath/controller-web' && \
            scp -r $localDist $userName@$remoteHost:$remotePath/ && \
            ssh $userName@$remoteHost 'cd $workDir && ./$scriptName restart'
        """.trimIndent()
    )
}

