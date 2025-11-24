plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(project(":wiremock-core"))

    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    implementation(libs.junit.platform.commons)

    testImplementation(libs.apache.http5.client)
    testImplementation(libs.apache.http5.core)
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.junit.platform.testkit)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.jetty.http)
    testRuntimeOnly(libs.junit.jupiter)
    testRuntimeOnly(project(":wiremock-jetty"))
    testImplementation(project(":wiremock-httpclient-apache5"))
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
