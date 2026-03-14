plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    buildsrc.convention.base
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation("com.mysql:mysql-connector-j:9.5.0")
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //加入mybatis依赖
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    implementation("io.minio:minio:8.6.0")

    // 加入Lombok依赖
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
}