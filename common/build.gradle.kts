plugins {
    alias(libs.plugins.kotlinPluginSerialization)
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
        implementation("org.springframework.boot:spring-boot-starter")
    
        implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
        implementation("com.mysql:mysql-connector-j:9.5.0")
    
        implementation("io.milvus:milvus-sdk-java:2.6.3")
        implementation("io.minio:minio:8.6.0")
        implementation("org.neo4j.driver:neo4j-java-driver:5.28.9")
    }
    