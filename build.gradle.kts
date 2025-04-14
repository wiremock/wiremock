import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Container
import java.net.URI

buildscript {
  repositories {
    mavenCentral()
  }
}

plugins {
  id("java-library")
  id("java-test-fixtures")
  id("scala")
  id("signing")
  id("maven-publish")
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  id("idea")
  id("eclipse")
  id("project-report")
  id("com.diffplug.spotless") version "6.25.0"
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("org.sonarqube") version "6.1.0.5360"
  id("jacoco")
  id("me.champeau.jmh") version "0.7.3"
  id("com.dorongold.task-tree") version "4.0.1"
}

group = "org.wiremock"

@Suppress("ConstPropertyName")
object Versions {
  const val handlebars     = "4.3.1"
  const val jetty          = "12.0.16"
  const val guava          = "33.4.6-jre"
  const val jackson        = "2.18.3"
  const val xmlUnit        = "2.10.0"
  const val jsonUnit       = "2.40.1"
  const val junitJupiter   = "5.12.1"
}

val standaloneOnly: Configuration by configurations.creating

dependencies {
  api(platform("org.eclipse.jetty:jetty-bom:${Versions.jetty}"))
  api(platform("org.eclipse.jetty.ee10:jetty-ee10-bom:${Versions.jetty}"))
  api("org.eclipse.jetty:jetty-server")
  api("org.eclipse.jetty:jetty-proxy")
  api("org.eclipse.jetty.http2:jetty-http2-server")
  api("org.eclipse.jetty:jetty-alpn-server")
  api("org.eclipse.jetty:jetty-alpn-java-server")
  api("org.eclipse.jetty:jetty-alpn-java-client")
  api("org.eclipse.jetty:jetty-alpn-client")
  api("org.eclipse.jetty.ee10:jetty-ee10-servlet")
  api("org.eclipse.jetty.ee10:jetty-ee10-servlets")
  api("org.eclipse.jetty.ee10:jetty-ee10-webapp")

  api("com.google.guava:guava:${Versions.guava}") {
    exclude(group = "com.google.code.findbugs", module = "jsr305")
  }
  api(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
  api("com.fasterxml.jackson.core:jackson-core")
  api("com.fasterxml.jackson.core:jackson-annotations")
  api("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  api("org.apache.httpcomponents.client5:httpclient5:5.4.3")
  api("org.xmlunit:xmlunit-core:${Versions.xmlUnit}")
  api("org.xmlunit:xmlunit-legacy:${Versions.xmlUnit}") {
    exclude(group = "junit", module = "junit")
  }
  api("org.xmlunit:xmlunit-placeholders:${Versions.xmlUnit}")
  api("net.javacrumbs.json-unit:json-unit-core:${Versions.jsonUnit}")
  api("com.jayway.jsonpath:json-path:2.9.0") {
    exclude(group = "org.ow2.asm", module = "asm")
  }

  implementation("org.slf4j:slf4j-api:1.7.36")
  add("standaloneOnly", "org.slf4j:slf4j-nop:1.7.36")

  api("net.sf.jopt-simple:jopt-simple:5.0.4")

  compileOnly("junit:junit:4.13.2") {
    exclude(group = "org.hamcrest", module = "hamcrest-core")
  }
  compileOnly(platform("org.junit:junit-bom:${Versions.junitJupiter}"))
  compileOnly("org.junit.jupiter:junit-jupiter")

  api("com.github.jknack:handlebars:${Versions.handlebars}") {
    exclude(group = "org.mozilla", module = "rhino")
  }
  api("com.github.jknack:handlebars-helpers:${Versions.handlebars}") {
    exclude(group = "org.mozilla", module = "rhino")
    exclude(group = "org.apache.commons", module = "commons-lang3")
  }

  api("commons-fileupload:commons-fileupload:1.5")

  api("com.networknt:json-schema-validator:1.5.6")

  testFixturesApi("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
  testFixturesApi("org.junit.platform:junit-platform-testkit")
  testFixturesApi("org.junit.platform:junit-platform-launcher")
  testFixturesApi("org.junit.jupiter:junit-jupiter-params")
  testFixturesApi("org.junit-pioneer:junit-pioneer:2.3.0")
  testFixturesApi("org.hamcrest:hamcrest-core:3.0")
  testFixturesApi("org.hamcrest:hamcrest-library:3.0")
  testFixturesApi("org.mockito:mockito-core:5.16.1")
  testFixturesApi("org.mockito:mockito-junit-jupiter:5.16.1")
  testFixturesApi("net.javacrumbs.json-unit:json-unit:${Versions.jsonUnit}")
  testFixturesApi("org.skyscreamer:jsonassert:1.5.1")
  testFixturesApi("com.toomuchcoding.jsonassert:jsonassert:0.8.0")
  testFixturesApi("org.awaitility:awaitility:4.3.0")
  testFixturesApi("commons-io:commons-io:2.18.0")

  testImplementation("junit:junit:4.13.2")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testImplementation("org.scala-lang:scala-library:2.13.16")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")

  testImplementation("org.eclipse.jetty:jetty-client")
  testRuntimeOnly(files("src/test/resources/classpath file source/classpathfiles.zip", "src/test/resources/classpath-filesource.jar"))

  testImplementation(files("test-extension/test-extension.jar"))

  testImplementation("org.openjdk.jmh:jmh-core:1.37")
  testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")

  testImplementation("org.eclipse.jetty.http2:jetty-http2-client:${Versions.jetty}")
  testImplementation("org.eclipse.jetty.http2:jetty-http2-client-transport:${Versions.jetty}")
  testImplementation("org.eclipse.jetty:jetty-alpn-java-client:${Versions.jetty}")

  modules {
    module("org.apache.logging.log4j:log4j-core") {
      replacedBy("org.apache.logging.log4j:log4j-to-slf4j")
    }
    module("commons-logging:commons-logging") {
      replacedBy("org.slf4j:jcl-over-slf4j")
    }
    module("log4j:log4j") {
      replacedBy("org.slf4j:log4j-over-slf4j")
    }
    module("javax.activation:activation") {
      replacedBy("jakarta.activation:jakarta.activation-api")
    }
    module("javax.activation:javax.activation-api") {
      replacedBy("jakarta.activation:jakarta.activation-api")
    }
    module("javax.validation:validation-api") {
      replacedBy("jakarta.validation:jakarta.validation-api")
    }
    module("javax.xml.bind:jaxb-api") {
      replacedBy("jakarta.xml.bind:jakarta.xml.bind-api")
    }
    module("org.hamcrest:hamcrest-core") {
      replacedBy("org.hamcrest:hamcrest")
    }
    module("org.hamcrest:hamcrest-library") {
      replacedBy("org.hamcrest:hamcrest")
    }
    module("javax.ws.rs:jsr311-api") {
      replacedBy("jakarta.ws.rs:jakarta.ws.rs-api")
    }
    module("javax.ws.rs:javax.ws.rs-api") {
      replacedBy("jakarta.ws.rs:jakarta.ws.rs-api")
    }
    module("javax.servlet:javax.servlet-api") {
      replacedBy("jakarta.servlet:jakarta.servlet-api")
    }
    module("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api") {
      replacedBy("jakarta.servlet:jakarta.servlet-api")
    }
    module("javax.annotation:javax.annotation-api") {
      replacedBy("jakarta.annotation:jakarta.annotation-api")
    }
    module("com.sun.activation:jakarta.activation") {
      replacedBy("jakarta.activation:jakarta.activation-api")
    }
  }
}

val runningOnCI = System.getenv("CI") == "true"

val repoUser = if (hasProperty("sonatypeUser")) property("sonatypeUser") else "default"
val repoPassword = if (hasProperty("sonatypePassword")) property("sonatypePassword") else "default"

val pomInfo: MavenPom.() -> Unit = {
  name.set("WireMock")
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

allprojects {
  apply(plugin = "com.diffplug.spotless")
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

  repositories {
    mavenCentral()
  }

  version = "4.0.0-beta.1"

  tasks {

    compileJava {
      options.encoding = "UTF-8"
      options.compilerArgs.addAll(
        listOf(
          "-XDenableSunApiLintControl",
          "--add-exports=java.base/sun.security.x509=ALL-UNNAMED"
        )
      )
    }

    compileTestJava {
      options.encoding = "UTF-8"
      options.compilerArgs.addAll(
        listOf(
          "-XDenableSunApiLintControl",
          "--add-exports=java.base/sun.security.x509=ALL-UNNAMED"
        )
      )
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
}

tasks.test {
  // Without this the archunit tests fail. I do not know why.
  classpath += sourceSets.main.get().compileClasspath + sourceSets.main.get().runtimeClasspath
}

java {
  sourceCompatibility = VERSION_17
  targetCompatibility = VERSION_17
  withSourcesJar()
  withJavadocJar()
}

val testJar by tasks.registering(Jar::class) {
  archiveClassifier.set("tests")
  from(sourceSets.test.get().output)
}

tasks.jar {
  archiveBaseName.set("wiremock")
  manifest {
    attributes("Main-Class" to "wiremock.Run")
    attributes("Add-Exports" to "java.base/sun.security.x509")
    attributes("Implementation-Version" to project.version)
    attributes("Implementation-Title" to "WireMock")
  }
}

tasks.shadowJar {
  archiveBaseName = "wiremock-standalone"
  archiveClassifier = ""
  configurations = listOf(
    project.configurations.runtimeClasspath.get(),
    standaloneOnly,
  )

  relocate("org.mortbay", "wiremock.org.mortbay")
  relocate("org.eclipse", "wiremock.org.eclipse")
  relocate("org.codehaus", "wiremock.org.codehaus")
  relocate("com.google", "wiremock.com.google")
  relocate("com.google.thirdparty", "wiremock.com.google.thirdparty")
  relocate("com.fasterxml.jackson", "wiremock.com.fasterxml.jackson")
  relocate("org.apache", "wiremock.org.apache")
  relocate("org.xmlunit", "wiremock.org.xmlunit")
  relocate("org.hamcrest", "wiremock.org.hamcrest")
  relocate("org.skyscreamer", "wiremock.org.skyscreamer")
  relocate("org.json", "wiremock.org.json")
  relocate("net.minidev", "wiremock.net.minidev")
  relocate("com.jayway", "wiremock.com.jayway")
  relocate("org.objectweb", "wiremock.org.objectweb")
  relocate("org.custommonkey", "wiremock.org.custommonkey")
  relocate("net.javacrumbs", "wiremock.net.javacrumbs")
  relocate("net.sf", "wiremock.net.sf")
  relocate("com.github.jknack", "wiremock.com.github.jknack")
  relocate("org.antlr", "wiremock.org.antlr")
  relocate("jakarta.servlet", "wiremock.jakarta.servlet")
  relocate("org.checkerframework", "wiremock.org.checkerframework")
  relocate("org.hamcrest", "wiremock.org.hamcrest")
  relocate("org.slf4j", "wiremock.org.slf4j")
  relocate("joptsimple", "wiremock.joptsimple")
  exclude("joptsimple/HelpFormatterMessages.properties")
  relocate("org.yaml", "wiremock.org.yaml")
  relocate("com.ethlo", "wiremock.com.ethlo")
  relocate("com.networknt", "wiremock.com.networknt")

  dependencies {
    exclude(dependency("junit:junit"))
  }

  mergeServiceFiles()

  exclude("META-INF/maven/**")
  exclude("META-INF/versions/17/**")
  exclude("META-INF/versions/21/**")
  exclude("META-INF/versions/22/**")
  exclude("module-info.class")
  exclude("handlebars-*.js")
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
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])
      artifact(testJar)

      pom {
        description.set("A web service test double for all occasions")
        pomInfo()
      }
    }

    create<MavenPublication>("standaloneJar") {
      artifactId = "${tasks.jar.get().archiveBaseName.get()}-standalone"
      project.shadow.component(this)

      artifact(tasks.named("sourcesJar"))
      artifact(tasks.named("javadocJar"))
      artifact(testJar)

      pom.packaging = "jar"
      pom {
        description.set("A web service test double for all occasions - standalone edition")
        pomInfo()
      }
    }
  }

  nexusPublishing {
    // See https://github.com/wiremock/community/blob/main/infra/maven-central.md
    repositories {
      sonatype {
        val envUsername = providers.environmentVariable("OSSRH_USERNAME").orElse("").get()
        val envPassword = providers.environmentVariable("OSSRH_TOKEN").orElse("").get()
        if (envUsername.isNotEmpty() && envPassword.isNotEmpty()) {
          username.set(envUsername)
          password.set(envPassword)
        }
      }
    }
  }
}

@Suppress("UnstableApiUsage")
val checkReleasePreconditions by tasks.registering  {
  doLast {
    val requiredBranch = "master"
    val currentGitBranch = providers.exec {
      commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    }.standardOutput.asText.get()
    require(currentGitBranch == requiredBranch) {
      "Must be on the $requiredBranch branch in order to release to Sonatype"
    }
  }
}

@Suppress("UnstableApiUsage")
val addGitTag by tasks.registering {
  doLast {
    println(providers.exec { commandLine("git", "tag", version) }.standardOutput.asText.get())
    println(providers.exec { commandLine("git", "push", "origin", "--tags") }.standardOutput.asText.get())
  }
}

tasks.publish {
  dependsOn(
    checkReleasePreconditions,
    "signStandaloneJarPublication",
    "signMavenJavaPublication",
  )
}
tasks.withType<AbstractPublishToMaven>().configureEach {
  val signingTasks = tasks.withType<Sign>()
  mustRunAfter(signingTasks)
}

tasks.assemble {
  dependsOn(tasks.jar, tasks.shadowJar)
}

val release by tasks.registering {
  dependsOn(tasks.clean, tasks.assemble, tasks.publish, addGitTag)
}

val localRelease by tasks.registering {
  dependsOn(tasks.clean, tasks.assemble, tasks.publishToMavenLocal)
}

fun updateFiles(currentVersion: String, nextVersion: String) {

  val filesWithVersion: Map<String, (String) -> String> = mapOf(
    "build.gradle"                                       to { "version = '${it}" },
    "ui/package.json"                                    to { "\"version\": \"${it}\"" },
    "src/main/resources/version.properties"              to { "version=${it}" },
    "src/main/resources/swagger/wiremock-admin-api.json" to { "\"version\": \"${it}\"" },
    "src/main/resources/swagger/wiremock-admin-api.yaml" to { "version: $it" }
  )

  filesWithVersion.forEach { (fileName, lineWithVersionTemplates) ->
    val file = file(fileName)
    val lineWithVersionTemplateList = listOf(lineWithVersionTemplates)

    lineWithVersionTemplateList.forEach { lineWithVersionTemplate ->
      val oldLine = lineWithVersionTemplate(currentVersion)
      val newLine = lineWithVersionTemplate(nextVersion)
      println("Replacing '${oldLine}' with '${newLine}' in $fileName")
      file.writeText(file.readText().replace(oldLine, newLine))
    }
  }
}

tasks.register("bump-patch-version") {
  doLast {

    val currentVersion = project.version
    val nextVersion = "${getMajorVersion()}.${getMinorVersion()}.${getPatchVersion() + 1}"

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.register("bump-minor-version") {
  doLast {

    val currentVersion = project.version
    val nextVersion = "${getMajorVersion()}.${getMinorVersion() + 1}.0"

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.register("set-snapshot-version") {
  doLast {

    val currentVersion = project.version
    val nextVersion = project.findProperty("snapshotVersion")?.toString() ?: "${getMajorVersion()}.${getMinorVersion() + 1}.0-SNAPSHOT"

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.addAll(listOf(
    "--add-exports",
    "java.base/sun.security.x509=ALL-UNNAMED"
  ))
}

eclipse.classpath.file {
  whenMerged {
    (this as Classpath).entries
      .filterIsInstance<Container>()
      .filter { it.path.contains("JRE_CONTAINER") }
      .forEach {
        it.entryAttributes["module"] = true
        it.entryAttributes["add-exports"] = "java.base/sun.security.x509=ALL-UNNAMED"
      }
  }
}

fun getMajorVersion(): Int =
  Integer.valueOf(project.version.toString().substring(0, project.version.toString().indexOf('.')))

fun getMinorVersion(): Int =
  Integer.valueOf(project.version.toString().substring(project.version.toString().indexOf('.') + 1, project.version.toString().lastIndexOf('.')))

fun getPatchVersion(): Int =
  Integer.valueOf(project.version.toString().substring(project.version.toString().lastIndexOf('.') + 1))

fun getBetaVersion(): Int =
  Integer.valueOf(project.version.toString().substring(project.version.toString().lastIndexOf('-') + 1))

jmh {
  includes = listOf(".*benchmarks.*")
  threads = 50
}
