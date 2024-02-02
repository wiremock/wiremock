---
description: >
    download and install WireMock.
---

# Download and Installation

WireMock is available as a standalone service for Docker or Java, Java library, NPM package, or SaaS.

## Download options

WireMock ditributes Java packages in two flavours:
- a standard JAR containing just WireMock.
- a standalone uber JAR containing WireMock plus all its dependencies.

Most of the standalone JAR's dependencies are shaded--they are hidden in alternative packages. This allows WireMock to be used in projects with
conflicting versions of its dependencies. The standalone JAR is also runnable (see [Running as a Standalone Process](./running-standalone.md)).

## Standalone Service

Run the following in a terminal:

=== "Docker"

    ```bash
    docker run -it --rm -p 8080:8080 --name wiremock \
    wiremock/wiremock:{{ versions.wiremock_version }}
    ```

## Declaring a dependency in your code

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>{{ versions.wiremock_version }}</version>
        <scope>test</scope>
    </dependency>
    ```

=== "Gradle Groovy"

    ```groovy
    testImplementation "org.wiremock:wiremock-standalone:{{ versions.wiremock_version }}"
    ```

Learn more in the [Docker guide](./standalone/docker.md).

### Direct download

If you want to run WireMock as a standalone process you can
<a id="wiremock-standalone-download" href="https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/{{ versions.wiremock_version }}/wiremock-standalone-{{ versions.wiremock_version }}.jar">download the standalone JAR from
here</a>
