import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  id("wiremock.common-conventions")
  id("net.ltgt.errorprone") version "4.4.0"
}

tasks.jar {
  archiveBaseName.set("wiremock-url-jackson3")
}

dependencies {
  api(project(":wiremock-url"))
  api(project(":wiremock-string-parser-jackson3"))

  implementation(project(":wiremock-string-parser"))
  compileOnly(platform("tools.jackson:jackson-bom:3.0.0"))

  compileOnly("tools.jackson.core:jackson-databind")
  compileOnly("org.jspecify:jspecify:1.0.0")
  compileOnly("org.jetbrains:annotations:26.0.2-1")

  testImplementation(platform("org.junit:junit-bom:6.0.1"))
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.assertj.core)

  testImplementation("io.github.classgraph:classgraph:4.8.184")

  testImplementation(platform("tools.jackson:jackson-bom:3.0.0"))
  testImplementation("tools.jackson.core:jackson-databind")

  testRuntimeOnly(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)

  annotationProcessor("com.uber.nullaway:nullaway:0.12.15")
  errorprone("com.google.errorprone:error_prone_core:2.42.0")
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

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock URL Jackson 3"
        description = "Jackson 3 module for WireMock URL"
      }
    }
  }
}
