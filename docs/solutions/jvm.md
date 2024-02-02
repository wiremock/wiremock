---
layout: solution
title: "WireMock for Java and JVM languages"
meta_title: "Java and Java Virtual Machine Solutions | WireMock"
description: "Additional solutions for WireMock when using Java or other JVM based languages"
logo: /images/logos/technology/java.svg
hide-disclaimer: true
---

WireMock was originally created for Java development,
and there are plenty of solutions when developing applications powered by the Java Virtual Machine.

## WireMock

_WireMock_, also known as _WireMock Java_ is the flagman implementation of WireMock functionality and specifications,
maintained on the WireMock GitHub organization.
It is included into many distributions (including [WireMock Docker](../standalone/docker.md)), test framework adapters and products.
Most of the documentation on this website is about _WireMock Java_, unless specified explicitly.

Usage:

- [Running WireMock as a Standalone server](../standalone.md)
- [Using WireMock in plain Java without frameworks](./../java-usage.md)

References:

- [WireMock Java on GitHub](https://github.com/wiremock/wiremock)

## WireMock Extensions

_WireMock Java_ is [extensible](../extending-wiremock.md),
and there is a number of available extensions that can be included into WireMock
to extend its functionality, including but not limited to request filters, observability, storage, etc.

See the list of WireMock Extensions [here](../extensions/README.md).

## Integrations

### Integrations with test frameworks

WireMock has integrations with many popular Java test frameworks
for unit and integration testing.

- [JUnit 5+ and Jupiter](../junit-jupiter.md)
- [JUnit 4 and Vintage](../junit-extensions.md)
- [Testcontainers for Java](./testcontainers.md)
- [Spock](https://github.com/felipefzdz/spock-wiremock-extension) - maintained outside WireMock's organization on GitHub

### By JVM language

In addition to core Java, WireMock offers specialized integrations
(e.g. DSL Bindings or test framework libraries)
for the following languages:

- [Kotlin](./kotlin.md)
- [Scala](https://docs.google.com/document/d/1TQccT9Bk-o2lvRVN8_mMaGttaOnwbYFLkn0DsmwGIOA/edit#heading=h.gvb3rxc1ab9p)
- [Clojure](https://docs.google.com/document/d/1TQccT9Bk-o2lvRVN8_mMaGttaOnwbYFLkn0DsmwGIOA/edit#heading=h.gvb3rxc1ab9p)
- [Groovy](./groovy.md)

### By JVM Framework

- [Spring Boot](./spring-boot.md)
- [Quarkus](./quarkus.md)

## Related topics

- [WireMock on Android](./android.md)
