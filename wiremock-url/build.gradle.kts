plugins {
  id("wiremock.common-conventions")
}

tasks.jar {
  archiveBaseName.set("wiremock-url")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
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
