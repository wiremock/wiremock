import com.github.gundy.semver4j.model.Version
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Container

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("com.github.gundy:semver4j:0.16.4")
  }
}

plugins {
  id("wiremock.common-conventions")
  id("scala")
  alias(libs.plugins.nexus.publish)
  id("idea")
  id("eclipse")
  id("project-report")
  alias(libs.plugins.jmh)
  alias(libs.plugins.task.tree)
}

val standaloneOnly: Configuration by configurations.creating

dependencies {
  api(project(":wiremock-common"))
  api(project(":wiremock-jetty"))
  testImplementation(libs.apache.http5.client)
  testImplementation(libs.apache.http5.core)
  testImplementation(libs.guava)
  testImplementation(libs.handlebars)

  testImplementation(platform(libs.jackson.bom))
  testImplementation(libs.jackson.annotations)

  testImplementation(libs.jakarta.servlet.api)

  testImplementation(platform(libs.jetty.bom))
  testImplementation(platform(libs.jetty.ee10.bom))
  testImplementation(libs.jetty.ee10.servlet)
  testImplementation(libs.jetty.io)
  testImplementation(libs.jetty.server)
  testImplementation(libs.jetty.util)
  testImplementation(libs.json.schema.validator)

  testImplementation(libs.xmlunit.core)

  implementation(libs.jopt.simple)
  testImplementation(libs.json.path) {
    // See https://github.com/json-path/JsonPath/issues/224
    exclude(group = "org.ow2.asm", module = "asm")
  }
  testImplementation(libs.slf4j.api)

  // We do not want JUnit on the classpath, users should provide it themselves
  compileOnly(libs.junit4)
  compileOnly(platform(libs.junit.bom))
  compileOnly(libs.junit.jupiter.api)
  compileOnly(libs.junit.platform.commons)

  add("standaloneOnly", libs.slf4j.nop)

  testFixturesApi(project(":wiremock-common"))

  testFixturesApi(libs.apache.http5.client)
  testFixturesApi(libs.apache.http5.core)
  testFixturesApi(libs.guava)
  testFixturesApi(libs.hamcrest)
  testFixturesApi(libs.handlebars)
  testFixturesApi(libs.jakarta.servlet.api)
  testFixturesApi(libs.jsonassert)

  testFixturesImplementation(libs.jetty.util)
  testFixturesImplementation(platform(libs.junit.bom))
  testFixturesImplementation(libs.junit.jupiter.api)
  testFixturesImplementation(libs.mockito.core)
  testFixturesImplementation(libs.xmlunit.core)

  testImplementation(libs.android.json)
  testImplementation(libs.archunit)
  testImplementation(libs.archunit.junit5.api)
  testImplementation(libs.assertj.core)
  testImplementation(libs.awaitility)
  testImplementation(libs.jackson.databind)
  testImplementation(libs.jetty.client)
  testImplementation(libs.jetty.ee10.webapp)
  testImplementation(libs.jetty.http)
  testImplementation(libs.jetty.http2.client)
  testImplementation(libs.jetty.http2.client.transport)
  testImplementation(libs.jmh.core)
  testImplementation(libs.json.unit)
  testImplementation(libs.jsonassert.toomuchcoding)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.junit.pioneer)
  testImplementation(libs.junit.platform.engine)
  testImplementation(libs.junit.platform.launcher)
  testImplementation(libs.junit.platform.testkit)
  testImplementation(libs.junit4)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.junit.jupiter)
  testImplementation(libs.scala.library)

  testRuntimeOnly(files("src/test/resources/classpath file source/classpathfiles.zip", "src/test/resources/classpath-filesource.jar"))
  testRuntimeOnly(files("test-extension/test-extension.jar"))
  testRuntimeOnly(libs.archunit.junit5)
  testRuntimeOnly(libs.jmh.generator.annprocess)
  testRuntimeOnly(libs.junit.vintage.engine)
  testRuntimeOnly(libs.junit.jupiter)

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

tasks {
  check {
    dependsOn(buildHealth)
  }
}

tasks.test {
  // Without this the archunit tests fail. I do not know why.
  classpath += sourceSets.main.get().compileClasspath + sourceSets.main.get().runtimeClasspath
}

val testJar by tasks.registering(Jar::class) {
  archiveClassifier.set("tests")
  from(sourceSets.test.get().output)
}

tasks.jar {
  archiveBaseName.set("wiremock")
  manifest {
    attributes("Main-Class" to "wiremock.Run")
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

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])
      artifact(testJar)

      pom {
        name = "WireMock"
        description = "A web service test double for all occasions"
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
        name = "WireMock"
        description = "A web service test double for all occasions - standalone edition"
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
    val releaseBranches = listOf("master", "v4.x")
    val currentGitBranch = providers.exec {
      commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    }.standardOutput.asText.get().trim()
    require(currentGitBranch in releaseBranches) {
      "Must be on one of $releaseBranches branches in order to release to Sonatype; was on [$currentGitBranch]"
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

tasks.register("release") {
  dependsOn(tasks.clean, tasks.assemble, tasks.publish, addGitTag)
}

tasks.register("localRelease") {
  dependsOn(tasks.clean, tasks.assemble, tasks.publishToMavenLocal)
}

fun updateFiles(currentVersion: String, nextVersion: String) {

  val filesWithVersion: Map<String, (String) -> String> = mapOf(
    "buildSrc/src/main/kotlin/wiremock.common-conventions.gradle.kts" to { "version = \"${it}\"" },
    "ui/package.json"                                                 to { "\"version\": \"${it}\"" },
    "src/main/resources/version.properties"                           to { "version=${it}" },
    "src/main/resources/swagger/wiremock-admin-api.json"              to { "\"version\": \"${it}\"" },
    "src/main/resources/swagger/wiremock-admin-api.yaml"              to { "version: $it" },
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

    val currentVersion = Version.fromString(project.version.toString())
    val nextVersion = currentVersion.incrementPatch().toString()

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.register("bump-minor-version") {
  doLast {

    val currentVersion = Version.fromString(project.version.toString())
    val nextVersion = currentVersion.incrementMinor().toString()

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.register("bump-pre-release-version") {
  doLast {

    val currentVersion = Version.fromString(project.version.toString())
    val preReleaseType = currentVersion.preReleaseIdentifiers.getOrNull(0) ?: "beta"
    val preReleaseVersion = currentVersion.preReleaseIdentifiers.getOrNull(1)?.toString()?.toInt() ?: 0

    val nextVersion = "${currentVersion.major}.${currentVersion.minor}.${currentVersion.patch}-${preReleaseType}.${preReleaseVersion + 1}"

    updateFiles(currentVersion.toString(), nextVersion)
  }
}

tasks.register("set-snapshot-version") {
  doLast {

    val currentVersion = Version.fromString(project.version.toString())
    val nextVersion = project.findProperty("snapshotVersion")?.toString()
      ?: "${currentVersion.incrementMinor()}-SNAPSHOT"

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
    project(project.path) {
      onUnusedDependencies {
        exclude(
          ":wiremock-jetty",
          ":wiremock-common",
        )
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
