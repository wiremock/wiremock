plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(project(":wiremock-common"))

    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    implementation(libs.junit.platform.commons)
}

tasks.jar {
    archiveBaseName.set("wiremock-junit5")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = tasks.jar.get().archiveBaseName.get()
            from(components["java"])

            pom {
                name = "WireMock JUnit 5"
                description = "JUnit 5 (Jupiter) integration for WireMock"
            }
        }
    }
}
