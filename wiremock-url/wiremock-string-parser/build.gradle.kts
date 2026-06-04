plugins {
  id("wiremock.common-conventions")
}

tasks.jar {
  archiveBaseName.set("wiremock-string-parser")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock String Parser"
        description = "Utility interfaces for String parsing"
      }
    }
  }
}
