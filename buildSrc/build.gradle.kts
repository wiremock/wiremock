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
  implementation("org.sonarqube:org.sonarqube.gradle.plugin:6.1.0.5360")
}

java {
  sourceCompatibility = VERSION_17
  targetCompatibility = VERSION_17
}
