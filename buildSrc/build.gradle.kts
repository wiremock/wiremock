import org.gradle.api.JavaVersion.VERSION_17

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("com.diffplug.gradle.spotless:com.diffplug.gradle.spotless.gradle.plugin:6.25.0")
  implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:8.1.1")
  implementation("org.sonarqube:org.sonarqube.gradle.plugin:6.2.0.5505")
  implementation("com.vanniktech.maven.publish.base:com.vanniktech.maven.publish.base.gradle.plugin:0.32.0")

  // Jackson dependencies for JSON/YAML schema consolidation
  implementation("com.fasterxml.jackson.core:jackson-core:2.19.0")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.0")
}

java {
  sourceCompatibility = VERSION_17
  targetCompatibility = VERSION_17
}
