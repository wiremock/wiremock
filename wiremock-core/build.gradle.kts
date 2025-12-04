plugins {
    id("wiremock.common-conventions")
}

apply(from = "buildSchema.gradle")

dependencies {
    api(libs.commons.fileupload)
    api(libs.guava) {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }
    api(libs.handlebars) {
        exclude(group = "org.mozilla", module = "rhino")
    }

    api(platform(libs.jackson.bom))
    api(libs.jackson.annotations)
    api(libs.jackson.core)
    api(libs.jackson.databind)

    api(libs.json.schema.validator)
    api(libs.json.unit.core)

    api(libs.xmlunit.core)
    api(project(":wiremock-url"))

    implementation(libs.apache.http5.client)
    implementation(libs.handlebars.helpers) {
        exclude(group = "org.mozilla", module = "rhino")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.json.path) {
        exclude(group = "org.ow2.asm", module = "asm")
    }
    implementation(libs.slf4j.api)
    implementation(libs.xmlunit.legacy) {
        exclude(group = "junit", module = "junit")
    }
    implementation(libs.xmlunit.placeholders)

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

    constraints {
        implementation(libs.json.smart)
        constraints {
            implementation("org.apache.commons:commons-lang3:3.20.0") {
                because(
                    """
                    ✗ Uncontrolled Recursion [https://www.cve.org/CVERecord?id=CVE-2025-48924] in org.apache.commons:commons-lang3@3.12.0
                    This issue was fixed in versions: 3.18.0
                    """.trimIndent()
                )
            }
        }
    }
}

tasks.jar {
    archiveBaseName.set("wiremock-core")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = tasks.jar.get().archiveBaseName.get()
            from(components["java"])

            pom {
                name = "WireMock Core"
                description = "The core engine of WireMock"
            }
        }
    }
}
