import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
  `java-library`
  `java-test-fixtures`
  jacoco
  id("com.diffplug.spotless")
  id("com.github.johnrengelman.shadow")
  id("org.sonarqube")
}

group = "org.wiremock"
version = "4.0.0-beta.2"

repositories {
  mavenCentral()
}

val runningOnCI = System.getenv("CI") == "true"

tasks {

  compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
      listOf(
        "-XDenableSunApiLintControl",
        "--add-exports=java.base/sun.security.x509=ALL-UNNAMED"
      )
    )
  }

  compileTestJava {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
      listOf(
        "-XDenableSunApiLintControl",
        "--add-exports=java.base/sun.security.x509=ALL-UNNAMED"
      )
    )
  }

  compileTestFixturesJava {
    options.encoding = "UTF-8"
  }

  test {
    // Set the timezone for testing somewhere other than my machine to increase the chances of catching timezone bugs
    systemProperty("user.timezone", "Australia/Sydney")

    useJUnitPlatform()
    exclude("ignored/**")

    maxParallelForks = if (runningOnCI) 1 else 3

    testLogging {
      events("FAILED", "SKIPPED")
      exceptionFormat = FULL
    }

    finalizedBy(jacocoTestReport)
  }

  jacocoTestReport {
    reports {
      xml.required = true
    }
  }

  sonarqube {
    properties {
      property( "sonar.projectKey", "wiremock_wiremock")
      property( "sonar.organization", "wiremock")
      property( "sonar.host.url", "https://sonarcloud.io")
    }
  }

  shadowJar {
    dependsOn(jar)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

spotless {
  java {
    target("src/**/*.java")
    googleJavaFormat("1.17.0")
    licenseHeaderFile("$rootDir/gradle/spotless.java.license.txt")
    ratchetFrom("origin/master")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/Tmp*.java")
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    indentWithSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
  groovyGradle {
    target("**/*.gradle")
    greclipse()
    indentWithSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
  json {
    target("src/**/*.json")
    targetExclude(
      "**/tmp*.json",
      "src/test/resources/sample.json",
      "src/main/resources/swagger/*.json",
      "src/test/resources/filesource/subdir/deepfile.json",
      "src/test/resources/schema-validation/*.json",
      "src/test/resources/test-file-root/mappings/testjsonmapping.json",
      "src/main/resources/assets/swagger-ui/swagger-ui-dist/package.json"
    )
    simple().indentWithSpaces(2)
  }
}

tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}
