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
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation(libs.assertj.core)

  testRuntimeOnly("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
