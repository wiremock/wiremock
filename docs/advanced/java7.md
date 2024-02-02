---
description: >
    Recent WireMock versions do not support Java 1.7,
    but you can run older versions to achieve that
---

# Using old WireMock versions with Java 1.7

> **WARNING:** Recent WireMock versions do not support Java 1.7, but you can run older versions to achieve that.
> The Java 7 version was deprecated in the 2.x line and version 2.27.2 is the last release available.
> There will be no bugfixes and security patches provided.
> Make sure to update as soon as possible to Java 11 or above.

The Java 7 distribution is aimed primarily at Android developers and enterprise Java teams still using Java Runtime Environment (JRE) 1.7.
Some of its dependencies are not set to the latest versions e.g. Jetty 9.2.x is used,
as this is the last minor version to retain Java 7 compatibility.

## Maven dependencies

JUnit:

```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock</artifactId>
    <version>2.27.2</version>
    <scope>test</scope>
</dependency>
```

Standalone JAR:

```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>2.27.2</version>
    <scope>test</scope>
</dependency>
```

## Gradle dependencies

JUnit:

```groovy
testImplementation "com.github.tomakehurst:wiremock:2.27.2"
```

Standalone JAR:

```groovy
testImplementation "com.github.tomakehurst:wiremock-standalone:2.27.2"
```
