import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.plugins.signing.Sign

buildscript {
  repositories {
    maven {
      url "https://oss.sonatype.org"
    }
    mavenCentral()
  }
}

plugins {
  id 'java-library'
  id 'java-test-fixtures'
  id 'scala'
  id 'signing'
  id 'maven-publish'
  id 'io.github.gradle-nexus.publish-plugin' version '2.0.0'
  id 'idea'
  id 'eclipse'
  id 'project-report'
  id 'com.diffplug.spotless' version '6.25.0'
  id 'com.github.johnrengelman.shadow' version '8.1.1'
  id "org.sonarqube" version "6.1.0.5360"
  id 'jacoco'
  id "me.champeau.jmh" version "0.7.3"
  id 'com.dorongold.task-tree' version '4.0.1'
}

group = 'org.wiremock'

project.ext {
  versions = [
    handlebars     : '4.3.1',
    jetty          : '12.0.16',
    guava          : '33.4.6-jre',
    jackson        : '2.18.3',
    xmlUnit        : '2.10.0',
    jsonUnit       : '2.40.1',
    junitJupiter   : '5.12.1'
  ]
}

configurations {
  standaloneOnly
}

dependencies {
  api platform("org.eclipse.jetty:jetty-bom:$versions.jetty")
  api platform("org.eclipse.jetty.ee10:jetty-ee10-bom:$versions.jetty")
  api "org.eclipse.jetty:jetty-server"
  api "org.eclipse.jetty:jetty-proxy"
  api "org.eclipse.jetty.http2:jetty-http2-server"
  api "org.eclipse.jetty:jetty-alpn-server"
  api "org.eclipse.jetty:jetty-alpn-java-server"
  api "org.eclipse.jetty:jetty-alpn-java-client"
  api "org.eclipse.jetty:jetty-alpn-client"
  api "org.eclipse.jetty.ee10:jetty-ee10-servlet"
  api "org.eclipse.jetty.ee10:jetty-ee10-servlets"
  api "org.eclipse.jetty.ee10:jetty-ee10-webapp"

  api "com.google.guava:guava:$versions.guava", {
    exclude group: 'com.google.code.findbugs', module: 'jsr305'
  }
  api platform("com.fasterxml.jackson:jackson-bom:$versions.jackson")
  api "com.fasterxml.jackson.core:jackson-core",
      "com.fasterxml.jackson.core:jackson-annotations",
      "com.fasterxml.jackson.core:jackson-databind",
      "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
  api "org.apache.httpcomponents.client5:httpclient5:5.4.3"
  api "org.xmlunit:xmlunit-core:$versions.xmlUnit"
  api "org.xmlunit:xmlunit-legacy:$versions.xmlUnit", {
    exclude group: 'junit', module: 'junit'
  }
  api "org.xmlunit:xmlunit-placeholders:$versions.xmlUnit"
  api "net.javacrumbs.json-unit:json-unit-core:$versions.jsonUnit"
  api "com.jayway.jsonpath:json-path:2.9.0", {
    exclude group: 'org.ow2.asm', module: 'asm'
  }

  implementation "org.slf4j:slf4j-api:1.7.36"
  standaloneOnly "org.slf4j:slf4j-nop:1.7.36"

  api "net.sf.jopt-simple:jopt-simple:5.0.4"

  compileOnly("junit:junit:4.13.2") {
    exclude group: "org.hamcrest", module: "hamcrest-core"
  }
  compileOnly(platform("org.junit:junit-bom:$versions.junitJupiter"))
  compileOnly("org.junit.jupiter:junit-jupiter")

  api "com.github.jknack:handlebars:$versions.handlebars", {
    exclude group: 'org.mozilla', module: 'rhino'
  }
  api("com.github.jknack:handlebars-helpers:$versions.handlebars") {
    exclude group: 'org.mozilla', module: 'rhino'
    exclude group: 'org.apache.commons', module: 'commons-lang3'
  }

  api 'commons-fileupload:commons-fileupload:1.5'

  api 'com.networknt:json-schema-validator:1.5.6'

  testFixturesApi("org.junit.jupiter:junit-jupiter:$versions.junitJupiter")
  testFixturesApi("org.junit.platform:junit-platform-testkit")
  testFixturesApi("org.junit.platform:junit-platform-launcher")
  testFixturesApi("org.junit.jupiter:junit-jupiter-params")
  testFixturesApi('org.junit-pioneer:junit-pioneer:2.3.0')
  testFixturesApi "org.hamcrest:hamcrest-core:3.0"
  testFixturesApi "org.hamcrest:hamcrest-library:3.0"
  testFixturesApi 'org.mockito:mockito-core:5.16.1'
  testFixturesApi 'org.mockito:mockito-junit-jupiter:5.16.1'
  testFixturesApi "net.javacrumbs.json-unit:json-unit:$versions.jsonUnit"
  testFixturesApi "org.skyscreamer:jsonassert:1.5.1"
  testFixturesApi 'com.toomuchcoding.jsonassert:jsonassert:0.8.0'
  testFixturesApi 'org.awaitility:awaitility:4.3.0'
  testFixturesApi "commons-io:commons-io:2.18.0"

  testImplementation "junit:junit:4.13.2"
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testImplementation 'org.scala-lang:scala-library:2.13.16'
  testImplementation 'com.tngtech.archunit:archunit-junit5:1.4.0'

  testImplementation "org.eclipse.jetty:jetty-client"
  testRuntimeOnly files('src/test/resources/classpath file source/classpathfiles.zip', 'src/test/resources/classpath-filesource.jar')

  testImplementation files('test-extension/test-extension.jar')

  testImplementation 'org.openjdk.jmh:jmh-core:1.37'
  testImplementation 'org.openjdk.jmh:jmh-generator-annprocess:1.37'

  testImplementation "org.eclipse.jetty.http2:jetty-http2-client:$versions.jetty"
  testImplementation "org.eclipse.jetty.http2:jetty-http2-client-transport:$versions.jetty"
  testImplementation "org.eclipse.jetty:jetty-alpn-java-client:$versions.jetty"

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

allprojects {
  apply plugin: 'com.diffplug.spotless'
  spotless {
    java {
      target 'src/**/*.java'
      googleJavaFormat('1.17.0')
      licenseHeaderFile "${rootDir}/gradle/spotless.java.license.txt"
      ratchetFrom 'origin/master'
      trimTrailingWhitespace()
      endWithNewline()
      targetExclude '**/Tmp*.java'
    }
    groovyGradle {
      target '**/*.gradle'
      greclipse()
      indentWithSpaces(2)
      trimTrailingWhitespace()
      endWithNewline()
    }
    json {
      target 'src/**/*.json'
      targetExclude '**/tmp*.json', 'src/test/resources/sample.json', 'src/main/resources/swagger/*.json', 'src/test/resources/filesource/subdir/deepfile.json', 'src/test/resources/schema-validation/*.json', 'src/test/resources/test-file-root/mappings/testjsonmapping.json', 'src/main/resources/assets/swagger-ui/swagger-ui-dist/package.json'
      simple().indentWithSpaces(2)
    }
  }

  tasks.withType(AbstractArchiveTask).configureEach {
    preserveFileTimestamps = false
    reproducibleFileOrder = true
  }

  ext {
    runningOnCI = System.getenv('CI') == 'true'

    repoUser =     this.hasProperty('sonatypeUser')     ? sonatypeUser : 'default'
    repoPassword = this.hasProperty('sonatypePassword') ? sonatypePassword : 'default'

    pomInfo = {
      name 'WireMock'
      url 'http://wiremock.org'
      scm {
        connection 'https://github.com/wiremock/wiremock.git'
        developerConnection 'https://github.com/wiremock/wiremock.git'
        url 'https://github.com/wiremock/wiremock'
      }
      licenses {
        license {
          name 'The Apache Software License, Version 2.0'
          url 'http://www.apache.org/license/LICENSE-2.0.txt'
          distribution 'repo'
        }
      }
      developers {
        developer {
          id 'tomakehurst'
          name 'Tom Akehurst'
        }
      }
    }
  }

  repositories {
    mavenCentral()
  }

  version = '4.0.0-beta.1'

  compileJava {
    options.encoding = 'UTF-8'

    // silences warnings about compiling against `sun` packages
    options.compilerArgs += '-XDenableSunApiLintControl'
    options.compilerArgs += '--add-exports=java.base/sun.security.x509=ALL-UNNAMED'
  }

  compileTestJava {
    options.encoding = 'UTF-8'
    options.compilerArgs += '-XDenableSunApiLintControl'
    options.compilerArgs += '--add-exports=java.base/sun.security.x509=ALL-UNNAMED'
  }

  compileTestFixturesJava {
    options.encoding = 'UTF-8'
  }

  test {
    // Set the timezone for testing somewhere other than my machine to increase the chances of catching timezone bugs
    systemProperty 'user.timezone', 'Australia/Sydney'

    useJUnitPlatform()
    exclude 'ignored/**'

    maxParallelForks = runningOnCI ? 1 : 3

    testLogging {
      events "FAILED", "SKIPPED"
      exceptionFormat "full"
    }
  }

  jacocoTestReport {
    reports {
      xml.required = true
    }
  }
  test.finalizedBy jacocoTestReport

  sonarqube {
    properties {
      property "sonar.projectKey", "wiremock_wiremock"
      property "sonar.organization", "wiremock"
      property "sonar.host.url", "https://sonarcloud.io"
    }
  }

  shadowJar.dependsOn jar
}

test.classpath += sourceSets.main.compileClasspath + sourceSets.main.runtimeClasspath

java {
  sourceCompatibility = 17
  targetCompatibility = 17
  withSourcesJar()
  withJavadocJar()
}

task testJar(type: Jar, dependsOn: testClasses) {
  archiveClassifier.set('tests')
  from sourceSets.test.output
}

final DOCS_DIR = project(':').rootDir.getAbsolutePath() + '/docs-v2'

jar {
  archiveBaseName.set('wiremock')
  manifest {
    attributes("Main-Class": "wiremock.Run")
    attributes("Add-Exports": "java.base/sun.security.x509")
    attributes("Implementation-Version": project.version)
    attributes("Implementation-Title": "WireMock")
  }
}

shadowJar {
  archiveBaseName.set('wiremock-standalone')
  archiveClassifier.set('')
  configurations = [
    project.configurations.runtimeClasspath,
    project.configurations.standaloneOnly
  ]

  relocate "org.mortbay", 'wiremock.org.mortbay'
  relocate "org.eclipse", 'wiremock.org.eclipse'
  relocate "org.codehaus", 'wiremock.org.codehaus'
  relocate "com.google", 'wiremock.com.google'
  relocate "com.google.thirdparty", 'wiremock.com.google.thirdparty'
  relocate "com.fasterxml.jackson", 'wiremock.com.fasterxml.jackson'
  relocate "org.apache", 'wiremock.org.apache'
  relocate "org.xmlunit", 'wiremock.org.xmlunit'
  relocate "org.hamcrest", 'wiremock.org.hamcrest'
  relocate "org.skyscreamer", 'wiremock.org.skyscreamer'
  relocate "org.json", 'wiremock.org.json'
  relocate "net.minidev", 'wiremock.net.minidev'
  relocate "com.jayway", 'wiremock.com.jayway'
  relocate "org.objectweb", 'wiremock.org.objectweb'
  relocate "org.custommonkey", "wiremock.org.custommonkey"
  relocate "net.javacrumbs", "wiremock.net.javacrumbs"
  relocate "net.sf", "wiremock.net.sf"
  relocate "com.github.jknack", "wiremock.com.github.jknack"
  relocate "org.antlr", "wiremock.org.antlr"
  relocate "jakarta.servlet", "wiremock.jakarta.servlet"
  relocate "org.checkerframework", "wiremock.org.checkerframework"
  relocate "org.hamcrest", "wiremock.org.hamcrest"
  relocate "org.slf4j", "wiremock.org.slf4j"
  relocate "joptsimple", "wiremock.joptsimple"
  exclude 'joptsimple/HelpFormatterMessages.properties'
  relocate "org.yaml", "wiremock.org.yaml"
  relocate "com.ethlo", "wiremock.com.ethlo"
  relocate "com.networknt", "wiremock.com.networknt"

  dependencies {
    exclude(dependency('junit:junit'))
  }

  mergeServiceFiles()

  exclude 'META-INF/maven/**'
  exclude 'META-INF/versions/17/**'
  exclude 'META-INF/versions/21/**'
  exclude 'META-INF/versions/22/**'
  exclude 'module-info.class'
  exclude 'handlebars-*.js'
}

javadoc {
  exclude "**/CertificateAuthority.java"
  options.addStringOption('Xdoclint:none', '-quiet')
}

signing {
  required {
    !version.toString().contains("SNAPSHOT") && (gradle.taskGraph.hasTask("uploadArchives") || gradle.taskGraph.hasTask("publish"))
  }
  def signingKey = providers.environmentVariable("OSSRH_GPG_SECRET_KEY").orElse("").get()
  def signingPassphrase = providers.environmentVariable("OSSRH_GPG_SECRET_KEY_PASSWORD").orElse("").get()
  if (!signingKey.isEmpty() && !signingPassphrase.isEmpty()) {
    useInMemoryPgpKeys(signingKey, signingPassphrase)
  }
  sign publishing.publications
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = "https://maven.pkg.github.com/wiremock/wiremock"
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }

  components.java.withVariantsFromConfiguration(configurations.testFixturesApiElements) { skip() }
  components.java.withVariantsFromConfiguration(configurations.testFixturesRuntimeElements) { skip() }

  getComponents().withType(AdhocComponentWithVariants).each { c ->
    c.withVariantsFromConfiguration(project.configurations.shadowRuntimeElements) {
      skip()
    }
  }

  publications {
    mavenJava(MavenPublication) { publication ->
      artifactId = "${jar.getArchiveBaseName().get()}"
      from components.java
      artifact testJar

      pom.withXml {
        asNode().appendNode('description', 'A web service test double for all occasions')
        asNode().children().last() + pomInfo
      }
    }

    standaloneJar(MavenPublication) { publication ->
      artifactId = "${jar.getArchiveBaseName().get()}-standalone"
      project.shadow.component(publication)

      artifact sourcesJar
      artifact javadocJar
      artifact testJar

      pom.packaging 'jar'
      pom.withXml {
        asNode().appendNode('description', 'A web service test double for all occasions - standalone edition')
        asNode().children().last() + pomInfo
      }
    }
  }

  nexusPublishing {
    // See https://github.com/wiremock/community/blob/main/infra/maven-central.md
    repositories {
      sonatype {
        def envUsername = providers.environmentVariable("OSSRH_USERNAME").orElse("").get()
        def envPassword = providers.environmentVariable("OSSRH_TOKEN").orElse("").get()
        if (!envUsername.isEmpty() && !envPassword.isEmpty()) {
          username.set(envUsername)
          password.set(envPassword)
        }
      }
    }
  }
}

task checkReleasePreconditions {
  doLast {
    def REQUIRED_GIT_BRANCH = 'master'
    def currentGitBranch = 'git rev-parse --abbrev-ref HEAD'.execute().text.trim()
    assert currentGitBranch == REQUIRED_GIT_BRANCH, "Must be on the $REQUIRED_GIT_BRANCH branch in order to release to Sonatype"
  }
}

task addGitTag {
  doLast {
    println "git tag ${version}".execute().text
    println "git push origin --tags".execute().text
  }
}

publish.dependsOn checkReleasePreconditions
publish.dependsOn 'signStandaloneJarPublication'
publish.dependsOn 'signMavenJavaPublication'
tasks.withType(AbstractPublishToMaven).configureEach {
  def signingTasks = tasks.withType(Sign)
  mustRunAfter(signingTasks)
}

assemble.dependsOn jar, shadowJar

task release {
  dependsOn clean, assemble, publish, addGitTag
}

task localRelease {
  dependsOn clean, assemble, publishToMavenLocal
}

void updateFiles(String currentVersion, String nextVersion) {

  def filesWithVersion = [
    'build.gradle'                                      : { "version = '${it}" },
    'ui/package.json'                                   : { "\"version\": \"${it}\"" },
    'src/main/resources/version.properties'             : { "version=${it}" },
    'src/main/resources/swagger/wiremock-admin-api.json': { "\"version\": \"${it}\"" },
    'src/main/resources/swagger/wiremock-admin-api.yaml': {
      "version: ${it}"
    }
  ]

  filesWithVersion.each { fileName, lineWithVersionTemplates ->
    def file = file(fileName)
    def lineWithVersionTemplateList = [lineWithVersionTemplates].flatten()

    lineWithVersionTemplateList.each { lineWithVersionTemplate ->
      def oldLine = lineWithVersionTemplate.call(currentVersion)
      def newLine = lineWithVersionTemplate.call(nextVersion)
      println "Replacing '${oldLine}' with '${newLine}' in ${fileName}"
      file.text = file.text.replace(oldLine, newLine)
    }
  }
}

task 'bump-patch-version' {
  doLast {

    def currentVersion = project.version
    def nextVersion = "${majorVersion}.${minorVersion}.${patchVersion + 1}"

    updateFiles(currentVersion, nextVersion)
  }
}

task 'bump-minor-version' {
  doLast {

    def currentVersion = project.version
    def nextVersion = "${majorVersion}.${minorVersion + 1}.0"

    updateFiles(currentVersion, nextVersion)
  }
}

task 'set-snapshot-version' {
  doLast {

    def currentVersion = project.version
    def nextVersion = project.findProperty('snapshotVersion') ?: "${majorVersion}.${minorVersion + 1}.0-SNAPSHOT"

    updateFiles(currentVersion, nextVersion)
  }
}

tasks.withType(JavaCompile) {
  options.compilerArgs.addAll([
    "--add-exports",
    "java.base/sun.security.x509=ALL-UNNAMED"
  ])
}

eclipse.classpath.file {
  whenMerged {
    entries.find{ it.path ==~ '.*JRE_CONTAINER.*' }.each {
      it.entryAttributes['module'] = true
      it.entryAttributes['add-exports'] = 'java.base/sun.security.x509=ALL-UNNAMED'
    }
  }
}

int getMajorVersion() {
  Integer.valueOf(project.version.substring(0, project.version.indexOf('.')))
}

int getMinorVersion() {
  Integer.valueOf(project.version.substring(project.version.indexOf('.') + 1, project.version.lastIndexOf('.')))
}

int getPatchVersion() {
  Integer.valueOf(project.version.substring(project.version.lastIndexOf('.') + 1))
}

int getBetaVersion() {
  Integer.valueOf(project.version.substring(project.version.lastIndexOf('-') + 1))
}

wrapper {
  gradleVersion = '8.6'
  distributionType = Wrapper.DistributionType.BIN
}

jmh {
  includes = ['.*benchmarks.*']
  threads = 50
}
