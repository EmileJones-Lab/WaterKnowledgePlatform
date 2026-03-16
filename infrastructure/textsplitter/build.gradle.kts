plugins {
    id("buildsrc.convention.base")
}

dependencies {
    api(project(":infrastructure:textsplitter:mission-manager"))
    api(project(":infrastructure:textsplitter:structure-extraction"))
}