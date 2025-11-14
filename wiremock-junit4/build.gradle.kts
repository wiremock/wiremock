plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(project(":wiremock-common"))

    api(libs.junit4) {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }
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
