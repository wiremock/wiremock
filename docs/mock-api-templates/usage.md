---
description: Run an API template after you've downloaded the JSON file from the library page.
---

# Using Mock API Templates

This guide explains how to run an API template after you've downloaded the JSON file from the library page.

## WireMock standalone JAR

1. Create a folder called `mappings` if one doesn't already exist in the directory containing the standalone JAR file.
2. Copy the downloaded JSON file into the `mappings` directory.
3. The JSON will be automatically loaded at startup, via the command line.

```bash
java -jar wiremock-jre8-standalone-{{ versions.wiremock_version }}.jar
```

See [this page](./../standalone/java-jar.md) for general details on running WireMock standalone.

## In WireMock Docker

1. Create a folder with a subfolder inside called `mappings`.
2. Copy the JSON file into the `mappings` folder.
3. Start the Docker container, mounting the parent folder e.g. assuming the current directory contains `mappings`:

```bash
docker run -it --rm \
  -p 8080:8080 \
  --name wiremock \
  -v $PWD:/home/wiremock \
  wiremock/wiremock:{{ versions.wiremock_version }}
```

See [this page](./../standalone/java-jar.md) for general details on running WireMock Docker.

## Plain Java

If you're running WireMock embedded in a Java program or test suite
you can place the JSON file in a folder called `mappings`,
then set its parent as the WireMock server's root at startup.

```java
WireMockServer wm =
  new WireMockServer(wireMockConfig()
    .withRootDirectory("path/to/root") // The parent folder of mappings
  );
```

See [this](./../java-usage.md) for general details on running WireMock in embedded Java.

## JUnit

To do the same thing using the JUnit Jupiter extension:

```java
@RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().withRootDirectory("path/to/root"))
            .build();
```

See [this](./../junit-jupiter.md) for general details on running WireMock with JUnit 5+ Jupiter.

## Pushing to a remotely running WireMock server

The mock API JSON can be pushed to a remotely running WireMock server via its [Admin API](./../standalone/administration.md).

For instance if the WireMock server is running on `wiremock.dev.mycompany.com` port 8080, you can POST the JSON file to it e.g.

```bash
curl -v -d@mockapi.json http://wiremock.dev.mycompany.com:8080/__admin/mappings/import
```
