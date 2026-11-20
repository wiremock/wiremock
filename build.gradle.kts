import com.github.gundy.semver4j.model.Version
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Container
import java.net.URLClassLoader

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
  id("idea")
  id("eclipse")
  id("project-report")
  alias(libs.plugins.jmh)
  alias(libs.plugins.task.tree)
}

dependencies {
  api(project(":wiremock-core"))
  api(project(":wiremock-jetty"))
  implementation(project(":wiremock-httpclient-apache5"))
  implementation(project(":wiremock-url:wiremock-url"))

  implementation(libs.jopt.simple)

  testFixturesApi(project(":wiremock-core"))

  testFixturesApi(libs.apache.http5.client)
  testFixturesApi(libs.apache.http5.core)
  testFixturesApi(platform(libs.okhttp.bom))
  testFixturesImplementation(libs.okhttp.sse)
  testFixturesApi(libs.guava)
  testFixturesApi(libs.hamcrest)
  testFixturesApi(libs.handlebars)
  testFixturesApi(libs.jakarta.servlet.api)
  testFixturesApi(libs.jakarta.websockets)
  testFixturesApi(libs.jsonassert)
  testFixturesApi(libs.junit.jupiter.api)

  testFixturesImplementation(platform(libs.jetty.bom))
  testFixturesImplementation(libs.jetty.util)
  testFixturesImplementation(platform(libs.junit.bom))
  testFixturesImplementation(libs.mockito.core)
  testFixturesImplementation(libs.xmlunit.core)
  testFixturesImplementation(libs.awaitility)

  testImplementation(platform(libs.okhttp.bom))
  testImplementation(libs.okhttp)
  testImplementation(libs.okio)

  testImplementation(project(":wiremock-junit5"))
  testImplementation(libs.apache.http5.client)
  testImplementation(libs.apache.http5.core)
  testImplementation(libs.guava)
  testImplementation(libs.handlebars)
  testImplementation(libs.commons.lang)

  testImplementation(platform(libs.jackson.bom))
  testImplementation(libs.jackson.core)
  testImplementation(libs.jackson.annotations)

  testImplementation(libs.jakarta.servlet.api)

  testImplementation(platform(libs.jetty.bom))
  testImplementation(platform(libs.jetty.ee11.bom))
  testImplementation(libs.jetty.ee11.servlet)
  testImplementation(libs.jetty.io)
  testImplementation(libs.jetty.server)
  testImplementation(libs.jetty.util)
  testImplementation(libs.json.schema.validator)

  testImplementation(libs.xmlunit.core)
  testImplementation(libs.json.unit.core)

  testImplementation(libs.json.path) {
    // See https://github.com/json-path/JsonPath/issues/224
    exclude(group = "org.ow2.asm", module = "asm")
  }
  testImplementation(libs.slf4j.api)

  testImplementation(project(":wiremock-junit5"))
  testImplementation(libs.android.json)
  testImplementation(libs.archunit)
  testImplementation(libs.archunit.junit5.api)
  testImplementation(libs.assertj.core)
  testImplementation(libs.jackson.databind)
  testImplementation(libs.jetty.client)
  testImplementation(libs.jetty.ee11.webapp)
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
  testImplementation(libs.awaitility)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.junit.jupiter)
  testImplementation(libs.scala.library)
  testImplementation(libs.bouncycastle.bcpkix)
  testImplementation(project(":wiremock-core:certificate-generator"))

  testRuntimeOnly(files("src/test/resources/classpath file source/classpathfiles.zip", "src/test/resources/classpath-filesource.jar"))
  testRuntimeOnly(files("test-extension/test-extension.jar"))
  testRuntimeOnly(libs.archunit.junit5)
  testRuntimeOnly(libs.jmh.generator.annprocess)
  testRuntimeOnly(libs.junit.vintage.engine)
  testRuntimeOnly(libs.junit.jupiter)
  testRuntimeOnly(libs.junit4)

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

// Runs the AcceptanceTestBase suites against WireMock launched as a separate process, so that the
// standalone JVM jar and the GraalVM native binary are held to the same behaviour.
// -PnativeAgent runs the JVM target under the GraalVM tracing agent to harvest reachability metadata.
// RecordingDslAcceptanceTest is left out: it records through an in-process proxy, not the external server.
val externalAcceptanceTests = listOf(
  "com.github.tomakehurst.wiremock.AdminApiTest",
  "com.github.tomakehurst.wiremock.BasicAuthAcceptanceTest",
  "com.github.tomakehurst.wiremock.ChannelProviderApiAcceptanceTest",
  "com.github.tomakehurst.wiremock.CookieMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.DateHeaderAcceptanceTest",
  "com.github.tomakehurst.wiremock.DebugHeadersAcceptanceTest",
  "com.github.tomakehurst.wiremock.EditMappingAcceptanceTest",
  "com.github.tomakehurst.wiremock.EditStubMappingAcceptanceTest",
  "com.github.tomakehurst.wiremock.FaultsAcceptanceTest",
  "com.github.tomakehurst.wiremock.FixedMessageChannelAcceptanceTest",
  "com.github.tomakehurst.wiremock.GlobalSettingsAcceptanceTest",
  "com.github.tomakehurst.wiremock.GzipAcceptanceTest",
  "com.github.tomakehurst.wiremock.HeaderMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.JsonSchemaMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.LogTimingAcceptanceTest",
  "com.github.tomakehurst.wiremock.LooseUrlAcceptanceTest",
  "com.github.tomakehurst.wiremock.MappingsAcceptanceTest",
  "com.github.tomakehurst.wiremock.MessageChannelApiValidationAcceptanceTest",
  "com.github.tomakehurst.wiremock.MessageMappingValidationAcceptanceTest",
  "com.github.tomakehurst.wiremock.MultipartBodyMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.NearMissesAcceptanceTest",
  "com.github.tomakehurst.wiremock.NetworkTrafficListenerAcceptanceTest",
  "com.github.tomakehurst.wiremock.QueuedThreadPoolAcceptanceTest",
  "com.github.tomakehurst.wiremock.RecordApiAcceptanceTest",
  "com.github.tomakehurst.wiremock.RecorderServeEventTransformerAcceptanceTest",
  "com.github.tomakehurst.wiremock.RemoteMappingsLoaderAcceptanceTest",
  "com.github.tomakehurst.wiremock.RemoveStubMappingAcceptanceTest",
  "com.github.tomakehurst.wiremock.RemoveStubMappingsAcceptanceTest",
  "com.github.tomakehurst.wiremock.SavingMappingsAcceptanceTest",
  "com.github.tomakehurst.wiremock.ScenarioAcceptanceTest",
  "com.github.tomakehurst.wiremock.ServeEventLogAcceptanceTest",
  "com.github.tomakehurst.wiremock.SseAcceptanceTest",
  "com.github.tomakehurst.wiremock.StandaloneAcceptanceTest",
  "com.github.tomakehurst.wiremock.StandaloneDistributionAcceptanceTest",
  "com.github.tomakehurst.wiremock.StubImportAcceptanceTest",
  "com.github.tomakehurst.wiremock.StubMetadataAcceptanceTest",
  "com.github.tomakehurst.wiremock.StubRequestLoggingAcceptanceTest",
  "com.github.tomakehurst.wiremock.StubbingAcceptanceTest",
  "com.github.tomakehurst.wiremock.SubServeEventsAcceptanceTest",
  "com.github.tomakehurst.wiremock.UrlMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.UrlPathTemplateMatchingTest",
  "com.github.tomakehurst.wiremock.VerificationAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketConnectionAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketEntityDefinitionAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketHttpTriggerAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketMessageJournalAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketMessageStubAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketStubMatchingAcceptanceTest",
  "com.github.tomakehurst.wiremock.extension.ExtensionLifeCycleAcceptanceTest",
)

// Tests that depend on in-process state, or on HttpAdminClient defects that the in-process client hides
val externalAcceptanceTestExclusions = listOf(
  "com.github.tomakehurst.wiremock.AdminApiTest.getVersionRequest*",
  "com.github.tomakehurst.wiremock.WebsocketEntityDefinitionAcceptanceTest.textEntityDefinitionWithDataStore*",
  "com.github.tomakehurst.wiremock.WebsocketEntityDefinitionAcceptanceTest.textEntityDefinitionWithFilePathResolvesFromFilesStore",
  "com.github.tomakehurst.wiremock.WebsocketEntityDefinitionAcceptanceTest.binaryMessageFromDataStoreCanBeSentAsResponse",
  // HttpAdminClient cannot deserialise SendChannelMessageResult ("channelsMessaged")
  "com.github.tomakehurst.wiremock.WebsocketConnectionAcceptanceTest",
  "com.github.tomakehurst.wiremock.WebsocketMessageJournalAcceptanceTest.messageJournalRecordsSentEventWhenMessageSentViaApi*",
  // HttpAdminClient fails on the empty 404 body instead of returning an empty result
  "com.github.tomakehurst.wiremock.WebsocketMessageStubAcceptanceTest.*ReturnsFalseIsPresentWhenNotFound",
)

fun registerExternalAcceptanceTest(name: String, targetTask: String, command: Test.() -> List<String>) =
  tasks.register<Test>(name) {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = tasks.test.get().classpath
    systemProperty("user.timezone", "Australia/Sydney")
    filter {
      externalAcceptanceTests.forEach { includeTestsMatching(it) }
      externalAcceptanceTestExclusions.forEach { excludeTestsMatching(it) }
      isFailOnNoMatchingTests = false
    }
    dependsOn(targetTask)
    outputs.upToDateWhen { false }
    val testTask = this
    // One argument per line, so paths may contain spaces
    doFirst { testTask.systemProperty("wiremock.acceptance.externalCommand", testTask.command().joinToString("\n")) }
  }

val standaloneJar = layout.projectDirectory.file("wiremock-standalone/build/libs/wiremock-standalone-$version.jar")
val nativeBinaryName = if (System.getProperty("os.name").startsWith("Windows")) "wiremock.exe" else "wiremock"
val nativeBinary = layout.projectDirectory.file("wiremock-standalone/build/native/nativeCompile/$nativeBinaryName")
val nativeAgentDir = layout.projectDirectory.dir("wiremock-standalone/build/native-agent")

val traceWithNativeAgent = providers.gradleProperty("nativeAgent").isPresent

if (traceWithNativeAgent) {
  tasks.test {
    val callerFilter = file("wiremock-standalone/src/native-agent/caller-filter.json")
    jvmArgs("-agentlib:native-image-agent=config-output-dir=${nativeAgentDir.asFile.absolutePath}/test-{pid},caller-filter-file=$callerFilter")
  }
}

registerExternalAcceptanceTest(
  "standaloneJvmAcceptanceTest",
  if (traceWithNativeAgent) "classes" else ":wiremock-standalone:shadowJar",
) {
  val java = javaLauncher.get().executablePath.asFile.absolutePath
  if (traceWithNativeAgent) {
    // The agent must see the same unrelocated classes and extra resources that the native image is built from
    val agentOutput = "${nativeAgentDir.asFile.absolutePath}/standalone-{pid}"
    listOf(java, "-agentlib:native-image-agent=config-output-dir=$agentOutput", "-cp", (sourceSets.main.get().runtimeClasspath + files("wiremock-standalone/src/native-image/resources")).asPath, "wiremock.Run")
  } else {
    listOf(java, "-jar", standaloneJar.asFile.absolutePath)
  }
}

registerExternalAcceptanceTest("nativeAcceptanceTest", ":wiremock-standalone:nativeCompile") {
  listOf(nativeBinary.asFile.absolutePath)
}

// Regenerating the metadata:
//   ./gradlew test standaloneJvmAcceptanceTest -PnativeAgent   (traces the in-process suite and the standalone process)
//   ./gradlew updateNativeMetadata                    (merges the traces into src/native-image/generated)
// Entries for types that only exist on the test classpath are dropped, because the tests share packages with main.
val updateNativeMetadata by tasks.registering {
  group = "native"
  description = "Merges native-image-agent traces into src/native-image/generated, dropping test-only entries"
  val agentOutput = nativeAgentDir
  val metadataFile = file("wiremock-standalone/src/native-image/generated/reachability-metadata.json")
  val runtimeClasspath = sourceSets.main.get().runtimeClasspath
  val testClasspath = sourceSets.test.get().runtimeClasspath
  val javaHome = providers.environmentVariable("GRAALVM_HOME").orElse(providers.systemProperty("java.home"))
  doLast {
    // A trace without a metadata file (only the agent's .lock) is from a process that was killed mid-write
    val traces = agentOutput.asFile.listFiles { f -> f.resolve("reachability-metadata.json").exists() }
      .orEmpty().sorted()
    require(traces.isNotEmpty()) { "No agent traces in $agentOutput; run the tests with -PnativeAgent first" }
    val merged = temporaryDir.resolve("merged").apply { deleteRecursively() }
    providers.exec {
      commandLine(
        listOf("${javaHome.get()}/bin/native-image-configure", "generate", "--output-dir=$merged") +
          traces.map { "--input-dir=$it" },
      )
    }.result.get().assertNormalExitValue()

    fun loaderOf(files: Iterable<File>) =
      URLClassLoader(files.map { it.toURI().toURL() }.toTypedArray(), ClassLoader.getPlatformClassLoader())
    val runtime = loaderOf(runtimeClasspath)
    val test = loaderOf(testClasspath)
    // Entries are kept unless they only exist for the tests. Lookups that failed everywhere stay too:
    // they are probes for optional classes and services, which exact metadata must also record.
    fun keep(path: String) = runtime.getResource(path) != null || test.getResource(path) == null
    fun classFile(type: String) = type.substringBefore("[").replace('.', '/') + ".class"
    fun keepType(type: Any?): Boolean = when (type) {
      is String -> keep(classFile(type))
      is Map<*, *> -> (type["proxy"] as? List<*>).orEmpty().all { keepType(it) }
      else -> true
    }
    fun keepResource(entry: Any?): Boolean {
      val glob = (entry as? Map<*, *>)?.get("glob") as? String ?: return true
      // Class files read as resources come from bytecode tooling in the tests (ArchUnit, ByteBuddy)
      return !glob.endsWith(".class") && (glob.contains('*') || keep(glob))
    }

    @Suppress("UNCHECKED_CAST")
    val metadata = groovy.json.JsonSlurper().parse(merged.resolve("reachability-metadata.json")) as MutableMap<String, Any?>
    listOf("reflection", "jni").forEach { key ->
      (metadata[key] as? List<*>)?.let { entries -> metadata[key] = entries.filter { keepType((it as Map<*, *>)["type"]) } }
    }
    (metadata["resources"] as? List<*>)?.let { entries -> metadata["resources"] = entries.filter(::keepResource) }
    runtime.close()
    test.close()

    metadataFile.parentFile.mkdirs()
    metadataFile.writeText(
      // Two-space indentation to match the gson formatting spotless applies to src/**/*.json
      groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(metadata))
        .replace(Regex("\\[\\s+]"), "[]")
        .replace(Regex("(?m)^( {4})+")) { " ".repeat(it.value.length / 2) } + "\n",
    )
  }
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
    "signMavenJavaPublication",
  )
}
tasks.withType<AbstractPublishToMaven>().configureEach {
  val signingTasks = tasks.withType<Sign>()
  mustRunAfter(signingTasks)
}

tasks.assemble {
  dependsOn(tasks.jar)
}

tasks.register("release") {
  dependsOn(tasks.clean, tasks.assemble, tasks.publish, ":wiremock-standalone:publish", addGitTag)
}

tasks.register("localRelease") {
  dependsOn(tasks.clean, tasks.assemble, tasks.publishToMavenLocal)
  dependsOn(subprojects.filter { it.plugins.hasPlugin("maven-publish") }.map { "${it.path}:publishToMavenLocal" })
}

fun updateFiles(currentVersion: String, nextVersion: String) {

  val filesWithVersion: Map<String, (String) -> String> = mapOf(
    "ui/package.json"                                                    to { "\"version\": \"${it}\"" },
    "wiremock-core/src/main/resources/version.properties"              to { "version=${it}" },
    "wiremock-core/src/main/resources/swagger/wiremock-admin-api.json" to { "\"version\": \"${it}\"" },
    "wiremock-core/src/main/resources/swagger/wiremock-admin-api.yaml" to { "version: $it" },
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

tasks.register("apply-release-version") {
  doLast {
    val releaseVersion = requireNotNull(project.findProperty("releaseVersion")?.toString()) {
      "releaseVersion property must be specified (e.g. -PreleaseVersion=4.0.1)"
    }
    updateFiles("0.0.0-dev", releaseVersion)
  }
}

tasks.register("set-snapshot-version") {
  doLast {
    val currentVersionStr = project.version.toString()
    val nextVersion = project.findProperty("snapshotVersion")?.toString()
      ?: run {
          val baseVersionStr = if (currentVersionStr == "0.0.0-dev") {
            providers.exec {
              commandLine("git", "describe", "--tags", "--abbrev=0", "--match=[0-9]*")
            }.standardOutput.asText.get().trim().removePrefix("v")
          } else {
            currentVersionStr
          }
          "${Version.fromString(baseVersionStr).incrementMinor()}-SNAPSHOT"
        }

    updateFiles(currentVersionStr, nextVersion)
  }
}

eclipse.classpath.file {
  whenMerged {
    (this as Classpath).entries
      .filterIsInstance<Container>()
      .filter { it.path.contains("JRE_CONTAINER") }
      .forEach {
        it.entryAttributes["module"] = true
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
      onAny {
        exclude(
          ":wiremock-jetty",
          ":wiremock-core",
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
