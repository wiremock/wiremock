@file:Suppress("VulnerableLibrariesLocal")

plugins {
  id("wiremock.common-conventions")
}

tasks.jar {
  archiveBaseName.set("wiremock-string-parser-jackson2")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
  api(project(":wiremock-url:wiremock-string-parser"))
  api("com.fasterxml.jackson.core:jackson-core:2.5.0")
  api("com.fasterxml.jackson.core:jackson-databind:2.5.0")
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
