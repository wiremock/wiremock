plugins {
    id("wiremock.common-conventions")
}

dependencies {
    implementation(platform(libs.okhttp.bom))
    api(libs.okhttp)

    api(project(":wiremock-core"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(project(":wiremock-jetty"))

    modules {
        module("org.apache.logging.log4j:log4j-core") {
            replacedBy("org.apache.logging.log4j:log4j-to-slf4j")
        }
        module("commons-logging:commons-logging") {
            replacedBy("org.slf4j:jcl-over-slf4j")
        }
        module("log4j:log4j") {
            replacedBy("org.slf4j:log4j-over-slf4j")
        }
        module("org.hamcrest:hamcrest-core") {
            replacedBy("org.hamcrest:hamcrest")
        }
        module("org.hamcrest:hamcrest-library") {
            replacedBy("org.hamcrest:hamcrest")
        }
    }
}

tasks.jar {
    archiveBaseName.set("wiremock-httpclient-okhttp")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = tasks.jar.get().archiveBaseName.get()
            from(components["java"])

            pom {
                name = "WireMock HTTP Client OkHttp"
                description = "A WireMock HTTP client implementation that uses the OkHttp client"
            }
        }
    }
}
