plugins {
    id("wiremock.common-conventions")
    alias(libs.plugins.japicmp)
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
    api(project(":wiremock-url:wiremock-url"))

    api(libs.jspecify)

    api(project(":wiremock-core:certificate-generator"))

    implementation(libs.apache.http5.client)
    implementation(libs.handlebars.helpers) {
        exclude(group = "org.mozilla", module = "rhino")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    implementation(libs.jackson.datatype.jsr310)
    implementation(project(":wiremock-url:wiremock-url-jackson2"))
    implementation(libs.json.path) {
        exclude(group = "org.ow2.asm", module = "asm")
    }
    implementation(libs.slf4j.api)
    implementation(libs.xmlunit.legacy) {
        exclude(group = "junit", module = "junit")
    }
    implementation(libs.xmlunit.placeholders)
    compileOnly(libs.errorprone.annotations)

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

// Full transitive classpath of the 3.x baseline, used for type resolution
val japicmpBaseline by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

// Just the baseline JAR itself, used to tell japicmp what to compare (not its deps)
val japicmpBaselineJar by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    japicmpBaseline("org.wiremock:wiremock:3.13.2")
    japicmpBaselineJar("org.wiremock:wiremock:3.13.2") {
        isTransitive = false
    }
}

tasks.register<me.champeau.gradle.japicmp.JapicmpTask>("japicmp") {
    dependsOn(tasks.jar)
    // Full classpaths for resolving referenced types (Jackson, Guava, etc.)
    oldClasspath.from(configurations["japicmpBaseline"])
    newClasspath.from(tasks.jar.map { it.archiveFile }, configurations.runtimeClasspath)
    // Specific JARs to compare — keeps the diff to WireMock classes only
    oldArchives.from(configurations["japicmpBaselineJar"])
    newArchives.from(tasks.jar.map { it.archiveFile })
    onlyBinaryIncompatibleModified.set(true)
    // annotationIncludes only works when the annotation exists in both the old and new JARs.
    // For the 3.x→4.x comparison the annotation didn't exist in 3.x, so we scope by package
    // instead. Switch back to annotationIncludes for future 4.x→4.x+1 comparisons.
    packageIncludes.set(listOf(
        "com.github.tomakehurst.wiremock",
        "com.github.tomakehurst.wiremock.client",
        "com.github.tomakehurst.wiremock.common",
        "com.github.tomakehurst.wiremock.core",
        "com.github.tomakehurst.wiremock.extension",
        "com.github.tomakehurst.wiremock.http",
        "com.github.tomakehurst.wiremock.recording",
        "com.github.tomakehurst.wiremock.security",
        "com.github.tomakehurst.wiremock.stubbing",
        "com.github.tomakehurst.wiremock.verification",
        "org.wiremock"
    ))
    htmlOutputFile.set(layout.buildDirectory.file("reports/japicmp/breaking-changes.html"))
    xmlOutputFile.set(layout.buildDirectory.file("reports/japicmp/breaking-changes.xml"))
    txtOutputFile.set(layout.buildDirectory.file("reports/japicmp/breaking-changes.txt"))
}
