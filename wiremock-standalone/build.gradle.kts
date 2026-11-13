import java.net.URLClassLoader

plugins {
  id("wiremock.common-conventions")
  alias(libs.plugins.graalvm.native)
}

shadow{
  addShadowVariantIntoJavaComponent = true
}

configurations.configureEach {
  resolutionStrategy.dependencySubstitution {
    substitute(project(":wiremock-core:certificate-generator"))
      .using(variant(project(":wiremock-core:certificate-generator")) {
        attributes {
          attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
      })
      .because("In standalone we use the shadowed and minimized version of certificate-generator")
  }
}

dependencies {
  runtimeOnly(project(":"))
}

tasks.shadowJar {
  archiveBaseName = "wiremock-standalone"
  archiveClassifier = ""
  configurations = listOf(
    project.configurations.runtimeClasspath.get(),
  )
  manifest {
    attributes("Main-Class" to "wiremock.Run")
  }
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
  relocate("org.yaml", "wiremock.org.yaml")
  relocate("com.ethlo", "wiremock.com.ethlo")
  relocate("com.networknt", "wiremock.com.networknt")
  // json-schema-validator loads its messages from jsv-messages*.properties at the jar root
  relocate("jsv-messages", "wiremock.jsv-messages")
  relocate("org.jspecify", "wiremock.org.jspecify")
  relocate("org.bouncycastle", "wiremock.org.bouncycastle")

  dependencies {
    exclude(dependency("junit:junit"))
  }

  filesMatching("META-INF/services/**") {
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
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
    create<MavenPublication>("standaloneJar") {
      artifactId = tasks.shadowJar.get().archiveBaseName.get()
      from(components.findByName("shadow"))
      artifact(tasks.sourcesJar)
      artifact(tasks.javadocJar)

      pom.packaging = "jar"
      pom {
        name = "WireMock"
        description = "A web service test double for all occasions - standalone edition"
      }
    }
  }
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

tasks.publish {
  dependsOn(
    "signStandaloneJarPublication",
  )
}

// Disable the plain jar from being published/consumed
tasks.jar {
  enabled = false
}

configurations.apiElements {
  outgoing.artifacts.clear()
  outgoing.artifact(tasks.shadowJar)
}

configurations.runtimeElements {
  outgoing.artifacts.clear()
  outgoing.artifact(tasks.shadowJar)
}

// Built from the unshaded runtime classpath so that library-supplied and repository metadata match class names.
// Native config lives outside src/main/resources so it never leaks into the relocated standalone jar:
//   src/native-image/generated  agent-traced metadata, rewritten by updateNativeMetadata
//   src/native-image/resources  native-only classpath entry: hand-written metadata and service registrations
graalvmNative {
  binaries.named("main") {
    imageName = "wiremock"
    mainClass = "wiremock.Run"
    sharedLibrary = false
    // The plugin otherwise compiles the relocated shadow jar
    classpath.setFrom(configurations.runtimeClasspath, file("src/native-image/resources"))
    configurationFileDirectories.from(file("src/native-image/generated"))
    buildArgs.addAll("--no-fallback", "-H:+AddAllCharsets")
  }
  metadataRepository {
    enabled = true
  }
}

// Regenerating the metadata:
//   ./gradlew test standaloneJvmAcceptanceTest -PnativeAgent   (traces the in-process suite and the standalone process)
//   ./gradlew :wiremock-standalone:updateNativeMetadata          (merges the traces into src/native-image/generated)
// Entries for types that only exist on the test classpath are dropped, because the tests share packages with main.
val updateNativeMetadata by tasks.registering {
  group = "native"
  description = "Merges native-image-agent traces into src/native-image, keeping only runtime classpath types"
  val agentOutput = layout.buildDirectory.dir("native-agent")
  val metadataDir = file("src/native-image/generated")
  val runtimeClasspath = configurations.runtimeClasspath
  val javaHome = providers.environmentVariable("GRAALVM_HOME").orElse(providers.systemProperty("java.home"))
  doLast {
    // A trace without a metadata file (only the agent's .lock) is from a process that was killed mid-write
    val traces = agentOutput.get().asFile.listFiles { f -> f.resolve("reachability-metadata.json").exists() }
      .orEmpty().sorted()
    require(traces.isNotEmpty()) { "No agent traces in ${agentOutput.get()}; run the tests with -PnativeAgent first" }
    val merged = temporaryDir.resolve("merged").apply { deleteRecursively() }
    val inputs = (traces + metadataDir.takeIf { it.resolve("reachability-metadata.json").exists() })
      .filterNotNull().map { "--input-dir=$it" }
    providers.exec {
      commandLine(listOf("${javaHome.get()}/bin/native-image-configure", "generate", "--output-dir=$merged") + inputs)
    }.result.get().assertNormalExitValue()

    val loader = URLClassLoader(
      runtimeClasspath.get().files.map { it.toURI().toURL() }.toTypedArray(),
      ClassLoader.getPlatformClassLoader(),
    )
    fun exists(type: Any?): Boolean = when (type) {
      is String -> loader.getResource(type.removeSuffix("[]").replace("[]", "").replace('.', '/') + ".class") != null
      is Map<*, *> -> (type["proxy"] as? List<*>).orEmpty().all { exists(it) }
      else -> false
    }
    fun resourceExists(entry: Any?): Boolean {
      val glob = (entry as? Map<*, *>)?.get("glob") as? String ?: return true
      return glob.contains('*') || loader.getResource(glob) != null
    }

    @Suppress("UNCHECKED_CAST")
    val metadata = groovy.json.JsonSlurper().parse(merged.resolve("reachability-metadata.json")) as MutableMap<String, Any?>
    listOf("reflection", "jni").forEach { key ->
      (metadata[key] as? List<*>)?.let { entries -> metadata[key] = entries.filter { exists((it as Map<*, *>)["type"]) } }
    }
    (metadata["resources"] as? List<*>)?.let { entries -> metadata["resources"] = entries.filter(::resourceExists) }
    loader.close()

    metadataDir.mkdirs()
    metadataDir.resolve("reachability-metadata.json")
      .writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(metadata)).replace(Regex("\\[\\s+]"), "[]") + "\n")
  }
}
