plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(project(":wiremock-core"))

    api(libs.junit4)

    testImplementation(testFixtures(project(":")))
    testImplementation(libs.apache.http5.client)
    testImplementation(libs.apache.http5.core)
    testImplementation(libs.hamcrest)
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.vintage.engine)
}

tasks.jar {
    archiveBaseName.set("wiremock-junit4")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = tasks.jar.get().archiveBaseName.get()
            from(components["java"])

            pom {
                name = "WireMock JUnit 4"
                description = "JUnit 4 integration for WireMock"
            }
        }
    }
}
