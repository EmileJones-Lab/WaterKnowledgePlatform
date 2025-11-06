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
    implementation("org.neo4j.driver:neo4j-java-driver:5.28.9")
    implementation(project(":service"))
    implementation(project(":env"))
}

tasks.test {
    useJUnitPlatform()
}