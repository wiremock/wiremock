@file:Suppress("VulnerableLibrariesLocal")

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  id("wiremock.common-conventions")
  id("net.ltgt.errorprone") version "4.4.0"
}

tasks.jar {
  archiveBaseName.set("wiremock-string-parser-jackson2")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
  api(project(":wiremock-url:wiremock-string-parser"))
  api("com.fasterxml.jackson.core:jackson-core:2.5.0")
  api("com.fasterxml.jackson.core:jackson-databind:2.5.0")

  annotationProcessor("com.uber.nullaway:nullaway:0.13.0")
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

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock String Parser Jackson 2"
        description = "Jackson 2 module for WireMock String Parser"
      }
    }
  }
}
