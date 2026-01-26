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
  api(project(":wiremock-url:wiremock-string-parser"))
  api("tools.jackson.core:jackson-core:3.0.0")
  api("tools.jackson.core:jackson-databind:3.0.0")

  annotationProcessor("com.uber.nullaway:nullaway:0.13.0")
  errorprone("com.google.errorprone:error_prone_core:2.46.0")
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
