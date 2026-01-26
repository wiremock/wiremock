import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  id("wiremock.common-conventions")
  id("net.ltgt.errorprone") version "4.4.0"
}

tasks.jar {
  archiveBaseName.set("wiremock-string-parser-jackson3")
}

dependencies {
  api("org.jspecify:jspecify:1.0.0")
  api(project(":wiremock-string-parser"))
  api("tools.jackson.core:jackson-core")
  api("tools.jackson.core:jackson-databind")

  compileOnly(platform("tools.jackson:jackson-bom:3.0.4"))
  compileOnly("org.jetbrains:annotations:26.0.2-1")

  annotationProcessor("com.uber.nullaway:nullaway:0.12.15")
  errorprone("com.google.errorprone:error_prone_core:2.42.0")
}

tasks.compileJava {
  options.errorprone {
    check("NullAway", CheckSeverity.ERROR)
    check("NullableOptional", CheckSeverity.OFF)
    check("ClassInitializationDeadlock", CheckSeverity.OFF)
    option("NullAway:AnnotatedPackages", "org.wiremock.url")
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = tasks.jar.get().archiveBaseName.get()
      from(components["java"])

      pom {
        name = "WireMock String Parser Jackson 3"
        description = "Jackson 3 module for WireMock String Parser"
      }
    }
  }
}
