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
  alias(libs.plugins.nexus.publish)
  id("idea")
  id("eclipse")
  id("project-report")
  alias(libs.plugins.spotless)
  alias(libs.plugins.shadow)
  alias(libs.plugins.sonarqube)
  id("jacoco")
  alias(libs.plugins.jmh)
  alias(libs.plugins.task.tree)
}

group = "org.wiremock"

val standaloneOnly: Configuration by configurations.creating

dependencies {
  api(libs.apache.http5.client)
  api(libs.commons.fileupload)
  api(libs.guava) {
    exclude(group = "com.google.code.findbugs", module = "jsr305")
  }
  api(libs.handlebars) {
    exclude(group = "org.mozilla", module = "rhino")
  }
  api(libs.handlebars.helpers) {
    exclude(group = "org.mozilla", module = "rhino")
    exclude(group = "org.apache.commons", module = "commons-lang3")
  }

  api(platform(libs.jackson.bom))
  api(libs.jackson.annotations)
  api(libs.jackson.core)
  api(libs.jackson.databind)
  api(libs.jackson.datatype.jsr310)

  api(platform(libs.jetty.bom))
  api(platform(libs.jetty.ee10.bom))
  api(libs.jetty.alpn.client)
  api(libs.jetty.alpn.java.client)
  api(libs.jetty.alpn.java.server)
  api(libs.jetty.alpn.server)
  api(libs.jetty.ee10.servlet)
  api(libs.jetty.ee10.servlets)
  api(libs.jetty.ee10.webapp)
  api(libs.jetty.http2.server)
  api(libs.jetty.proxy)
  api(libs.jetty.server)
  api(libs.jopt.simple)
  api(libs.json.path) {
    exclude(group = "org.ow2.asm", module = "asm")
  }
  api(libs.json.schema.validator)
  api(libs.json.unit.core)

  api(platform(libs.junit.bom))

  api(libs.xmlunit.core)
  api(libs.xmlunit.legacy) {
    exclude(group = "junit", module = "junit")
  }
  api(libs.xmlunit.placeholders)

  implementation(libs.slf4j.api)

  compileOnly(libs.junit4) {
    exclude(group = "org.hamcrest", module = "hamcrest-core")
  }
  compileOnly(libs.junit.jupiter)

  add("standaloneOnly", libs.slf4j.nop)

  testFixturesApi(libs.awaitility)
  testFixturesApi(libs.commons.io)
  testFixturesApi(libs.hamcrest.core)
  testFixturesApi(libs.hamcrest.library)
  testFixturesApi(libs.json.unit)
  testFixturesApi(libs.jsonassert)
  testFixturesApi(libs.jsonassert.toomuchcoding)
  testFixturesApi(libs.junit.jupiter)
  testFixturesApi(libs.junit.jupiter.params)
  testFixturesApi(libs.junit.pioneer)
  testFixturesApi(libs.junit.platform.launcher)
  testFixturesApi(libs.junit.platform.testkit)
  testFixturesApi(libs.mockito.core)
  testFixturesApi(libs.mockito.junit.jupiter)

  testImplementation(libs.archunit.junit5)
  testImplementation(files("test-extension/test-extension.jar"))
  testImplementation(libs.jetty.alpn.java.client)
  testImplementation(libs.jetty.client)
  testImplementation(libs.jetty.http2.client)
  testImplementation(libs.jetty.http2.client.transport)
  testImplementation(libs.jmh.core)
  testImplementation(libs.jmh.generator.annprocess)
  testImplementation(libs.junit4)
  testImplementation(libs.scala.library)

  testRuntimeOnly(files("src/test/resources/classpath file source/classpathfiles.zip", "src/test/resources/classpath-filesource.jar"))
  testRuntimeOnly(libs.junit.vintage.engine)

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

tasks {
  check {
    dependsOn(buildHealth)
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

tasks.register("listRuntimeDependencies") {
    group = "help"
    description = "Writes a flat, sorted list of runtime dependencies to a file"

    val outputFile = layout.buildDirectory.file("reports/flat-runtime-dependencies.txt")

    inputs.files(configurations.runtimeClasspath)
    outputs.file(outputFile)

    doLast {
        val dependencies = configurations.runtimeClasspath.get()
            .resolvedConfiguration
            .resolvedArtifacts
            .map { "${it.moduleVersion.id.group}:${it.name}:${it.moduleVersion.id.version}" }
            .toSortedSet()

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.printWriter().use { writer ->
            writer.println("Runtime dependencies:")
            dependencies.forEach { writer.println("  $it") }
        }
    }
}

dependencyAnalysis {
  issues {
    // configure for all projects
    all {
      // set behavior for all issue types
      onAny {
        severity("fail")
      }
      onDuplicateClassWarnings {
        severity("fail")
      }
    }
  }
  useTypesafeProjectAccessors(true)
  usage {
    analysis {
      checkSuperClasses(true)
    }
  }
}
