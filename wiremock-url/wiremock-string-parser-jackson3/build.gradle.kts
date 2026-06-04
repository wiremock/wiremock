plugins {
  id("wiremock.common-conventions")
}

tasks.jar {
  archiveBaseName.set("wiremock-string-parser-jackson3")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
  api(project(":wiremock-url:wiremock-string-parser"))
  api("tools.jackson.core:jackson-core:3.0.0")
  api("tools.jackson.core:jackson-databind:3.0.0")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock String Parser Jackson 3"
        description = "Jackson 3 module for WireMock String Parser"
      }
    }
  }
}
