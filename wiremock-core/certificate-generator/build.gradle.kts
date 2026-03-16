plugins {
  id("wiremock.common-conventions")
  id("com.gradleup.shadow")
}

tasks.shadowJar {
  archiveClassifier = "with-deps"
  description = "Create a shadow JAR with bouncy castle dependencies minimized"
  minimize()
  configurations = listOf(
    project.configurations.runtimeClasspath.get(),
  )
}

dependencies {
  api(libs.bouncycastle.bcpkix)
  implementation(libs.bouncycastle.bcprov)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])
      pom {
        name = "WireMock certificate generator"
        description = "Wiremock certificate generator"
      }
    }
  }
}
