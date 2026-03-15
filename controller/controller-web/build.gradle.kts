plugins {
    application
    id("buildsrc.convention.base")
    id("buildsrc.convention.spring-base")
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    implementation(project(":application-service:platform"))
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

