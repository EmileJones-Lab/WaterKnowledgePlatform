plugins {
    kotlin("jvm")
    application
    id("buildsrc.convention.base")
    alias(libs.plugins.springBootGradlePlugin)
}

dependencies {
    libs.bundles.springBoms.get().forEach {
        implementation(platform(it))
    }
    implementation(libs.springBootStarter)
    testImplementation(libs.springBootStarterTest) {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }


    implementation(libs.springBootStarterWeb)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    implementation(project(":application-service:platform"))
    implementation(project(":common"))
    runtimeOnly(project(":infrastructure:textsplitter"))
    runtimeOnly(project(":infrastructure:document"))
    runtimeOnly(project(":infrastructure:knowledge"))
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
    val localDist = "${layout.buildDirectory.get()}/install/web"
    val workDir = "~/Software/rag"
    val scriptName = "web-server.sh"

    commandLine(
        "sh", "-c",
        """
            ssh $userName@$remoteHost 'rm -rf $remotePath/controller-web' && \
            scp -r $localDist $userName@$remoteHost:$remotePath/ && \
            ssh $userName@$remoteHost 'cd $workDir && ./$scriptName restart'
        """
    )
}

