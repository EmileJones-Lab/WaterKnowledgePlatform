plugins {
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "top.emilejones"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.slf4j:slf4j-api")
    runtimeOnly("com.mysql:mysql-connector-j")
    
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.platform", module = "junit-platform-launcher")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")

    //加入mybatis依赖
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")


    // 加入Lombok依赖
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
}
