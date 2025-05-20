import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import java.net.URI

plugins {
  `java-library`
  `java-test-fixtures`
  jacoco
  signing
  `maven-publish`
  id("com.diffplug.spotless")
  id("com.github.johnrengelman.shadow")
  id("org.sonarqube")
}

group = "org.wiremock"
version = "4.0.0-beta.9"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = VERSION_17
  targetCompatibility = VERSION_17
  withSourcesJar()
  withJavadocJar()
}

tasks.jar {
  manifest {
    attributes("Add-Exports" to "java.base/sun.security.x509")
    attributes("Implementation-Version" to project.version)
    attributes("Implementation-Title" to "WireMock")
  }
}

val runningOnCI = System.getenv("CI") == "true"

tasks {

  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
      "-XDenableSunApiLintControl",
      "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
    ))
  }

  compileTestFixturesJava {
    options.encoding = "UTF-8"
  }

  test {
    // Set the timezone for testing somewhere other than my machine to increase the chances of catching timezone bugs
    systemProperty("user.timezone", "Australia/Sydney")

    useJUnitPlatform()
    exclude("ignored/**")

    maxParallelForks = if (runningOnCI) 1 else 3

    testLogging {
      events("FAILED", "SKIPPED")
      exceptionFormat = FULL
    }

    finalizedBy(jacocoTestReport)
  }

  jacocoTestReport {
    reports {
      xml.required = true
    }
  }

  sonarqube {
    properties {
      property( "sonar.projectKey", "wiremock_wiremock")
      property( "sonar.organization", "wiremock")
      property( "sonar.host.url", "https://sonarcloud.io")
    }
  }

  shadowJar {
    dependsOn(jar)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

spotless {
  java {
    target("src/**/*.java")
    googleJavaFormat("1.17.0")
    licenseHeaderFile("$rootDir/gradle/spotless.java.license.txt")
    ratchetFrom("origin/master")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/Tmp*.java")
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    indentWithSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
  groovyGradle {
    target("**/*.gradle")
    greclipse()
    indentWithSpaces(2)
    trimTrailingWhitespace()
    endWithNewline()
  }
  json {
    target("src/**/*.json")
    targetExclude(
      "**/tmp*.json",
      "src/test/resources/sample.json",
      "src/main/resources/swagger/*.json",
      "src/test/resources/filesource/subdir/deepfile.json",
      "src/test/resources/schema-validation/*.json",
      "src/test/resources/test-file-root/mappings/testjsonmapping.json",
      "src/main/resources/assets/swagger-ui/swagger-ui-dist/package.json"
    )
    simple().indentWithSpaces(2)
  }
}

tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

fun MavenPom.pomInfo() {
  url.set("https://wiremock.org")
  scm {
    connection.set("https://github.com/wiremock/wiremock.git")
    developerConnection.set("https://github.com/wiremock/wiremock.git")
    url.set("https://github.com/wiremock/wiremock")
  }
  licenses {
    license {
      name.set("The Apache Software License, Version 2.0")
      url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
      distribution.set("repo")
    }
  }
  developers {
    developer {
      id.set("tomakehurst")
      name.set("Tom Akehurst")
    }
  }
}

tasks.javadoc {
  exclude("**/CertificateAuthority.java")
  options.quiet()
  (options as StandardJavadocDocletOptions)
    .addBooleanOption("Xdoclint:none", true)
}

signing {
  isRequired = !version.toString().contains("SNAPSHOT") && (gradle.taskGraph.hasTask("uploadArchives") || gradle.taskGraph.hasTask("publish"))
  val signingKey = providers.environmentVariable("OSSRH_GPG_SECRET_KEY").orElse("").get()
  val signingPassphrase = providers.environmentVariable("OSSRH_GPG_SECRET_KEY_PASSWORD").orElse("").get()
  if (signingKey.isNotEmpty() && signingPassphrase.isNotEmpty()) {
    useInMemoryPgpKeys(signingKey, signingPassphrase)
  }
  sign(publishing.publications)
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = URI.create("https://maven.pkg.github.com/wiremock/wiremock")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }

  (components["java"] as AdhocComponentWithVariants).withVariantsFromConfiguration(configurations.testFixturesApiElements.get()) { skip() }
  (components["java"] as AdhocComponentWithVariants).withVariantsFromConfiguration(configurations.testFixturesRuntimeElements.get()) { skip() }

  getComponents().withType<AdhocComponentWithVariants>().forEach { c ->
    c.withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) {
      skip()
    }
  }

  publications {
    withType<MavenPublication> {
      pom {
        pomInfo()
      }
    }
  }
}
