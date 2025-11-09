plugins {
}

group = "top.emilejones"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.milvus:milvus-sdk-java:2.6.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":service"))
    implementation(project(":env"))
}

tasks.test {
    useJUnitPlatform()
}