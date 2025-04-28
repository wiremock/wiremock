plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(project(":wiremock-common"))

    api(libs.jakarta.servlet.api)

    api(platform(libs.jetty.bom))
    api(platform(libs.jetty.ee10.bom))
    api(libs.jetty.ee10.servlet)
    api(libs.jetty.io)
    api(libs.jetty.server)
    api(libs.jetty.util)

    implementation(libs.jetty.alpn.server)
    implementation(libs.jetty.ee10.servlets)
    implementation(libs.jetty.http)
    implementation(libs.jetty.http2.common)
    implementation(libs.jetty.http2.server)

    runtimeOnly(libs.jetty.alpn.java.client)
    runtimeOnly(libs.jetty.alpn.java.server)
    runtimeOnly(libs.jetty.ee10.webapp)

    implementation(libs.guava)

    modules {
        module("javax.activation:activation") {
            replacedBy("jakarta.activation:jakarta.activation-api")
        }
        module("javax.activation:javax.activation-api") {
            replacedBy("jakarta.activation:jakarta.activation-api")
        }
        module("javax.validation:validation-api") {
            replacedBy("jakarta.validation:jakarta.validation-api")
        }
        module("javax.xml.bind:jaxb-api") {
            replacedBy("jakarta.xml.bind:jakarta.xml.bind-api")
        }
        module("javax.ws.rs:jsr311-api") {
            replacedBy("jakarta.ws.rs:jakarta.ws.rs-api")
        }
        module("javax.ws.rs:javax.ws.rs-api") {
            replacedBy("jakarta.ws.rs:jakarta.ws.rs-api")
        }
        module("javax.servlet:javax.servlet-api") {
            replacedBy("jakarta.servlet:jakarta.servlet-api")
        }
        module("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api") {
            replacedBy("jakarta.servlet:jakarta.servlet-api")
        }
        module("javax.annotation:javax.annotation-api") {
            replacedBy("jakarta.annotation:jakarta.annotation-api")
        }
        module("com.sun.activation:jakarta.activation") {
            replacedBy("jakarta.activation:jakarta.activation-api")
        }
    }
}

tasks.jar {
    archiveBaseName.set("wiremock-jetty")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = tasks.jar.get().archiveBaseName.get()
            from(components["java"])

            pom {
                name = "WireMock Jetty"
                description = "A Jetty implementation of WireMock's HttpServer"
            }
        }
    }
}
