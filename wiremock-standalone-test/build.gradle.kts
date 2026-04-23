plugins {
    java
}

val wiremockVersion = "4.0.0-beta.31"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // The standalone fat JAR (contains WireMock + relocated third-party deps)
    testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")

    // Test fixtures from the root project (WireMockTestClient, TestFiles, etc.)
    testImplementation(testFixtures("org.wiremock:wiremock:$wiremockVersion"))

    // WireMock JUnit 5 extension (not included in standalone JAR)
    testImplementation("org.wiremock:wiremock-junit5:$wiremockVersion")

    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.14.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Hamcrest (unrelocated, needed by test code directly)
    testImplementation("org.hamcrest:hamcrest:3.0")

    // SLF4J (logging)
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.17")
}

tasks.test {
    useJUnitPlatform()

    // Match the timezone setting from the main project
    systemProperty("user.timezone", "Australia/Sydney")

    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
