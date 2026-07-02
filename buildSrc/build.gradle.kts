import org.gradle.api.JavaVersion.VERSION_17

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  // Exposes the type-safe `libs` version catalog accessors to precompiled script plugins
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation("com.diffplug.gradle.spotless:com.diffplug.gradle.spotless.gradle.plugin:6.25.0")
  implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.4.3")
  implementation("org.sonarqube:org.sonarqube.gradle.plugin:6.2.0.5505")
  implementation("com.vanniktech.maven.publish.base:com.vanniktech.maven.publish.base.gradle.plugin:0.35.0")
  implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.1.0")
}

java {
  sourceCompatibility = VERSION_17
  targetCompatibility = VERSION_17
}
