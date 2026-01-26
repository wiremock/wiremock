import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  id("wiremock.common-conventions")
  alias(libs.plugins.jmh)
  id("net.ltgt.errorprone") version "4.4.0"
}

tasks.jar {
  archiveBaseName.set("wiremock-url")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
  api(project(":wiremock-url:wiremock-string-parser"))
  compileOnly("org.jetbrains:annotations:26.0.2-1")

  testImplementation(platform("org.junit:junit-bom:6.0.1"))
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.assertj.core)

  testImplementation(platform(libs.jackson.bom))
  testImplementation(libs.jackson.core)
  testImplementation(libs.jackson.databind)
  testImplementation(libs.jackson.annotations)
  testImplementation(libs.commons.lang)
  testImplementation(libs.apache.commons.text)

  testRuntimeOnly(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)

  // JMH
  jmh(libs.jmh.core)
  jmh(libs.jmh.generator.annprocess)

  // JMH needs access to test dependencies for benchmark data
  jmh(platform(libs.jackson.bom))
  jmh(libs.jackson.core)
  jmh(libs.jackson.databind)
  jmh(libs.commons.lang)

  annotationProcessor("com.uber.nullaway:nullaway:0.13.0")
  errorprone("com.google.errorprone:error_prone_core:2.46.0")
}

tasks.compileJava {
  options.errorprone {
    check("NullAway", CheckSeverity.ERROR)
    check("NullableOptional", CheckSeverity.OFF)
    check("ClassInitializationDeadlock", CheckSeverity.OFF)
    option("NullAway:AnnotatedPackages", "org.wiremock.url")
  }
}

tasks.compileTestJava {
  options.errorprone {
    disableAllChecks = true
  }
}

jmh {
  includes.add(".*ParsePerformanceBenchmark.*")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock URL"
        description = "loose URL type"
      }
    }
  }
}

spotless {
  json {
    targetExclude("src/test/resources/org/wiremock/url/whatwg/*.json")
  }
}
