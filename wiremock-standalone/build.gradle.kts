plugins {
  id("wiremock.common-conventions")
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
  relocate("org.jspecify", "wiremock.org.jspecify")
  relocate("org.bouncycastle", "wiremock.org.bouncycastle")

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
