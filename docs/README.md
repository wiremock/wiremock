---
title: "WireMock User Documentation"
docs-wide: true
description: >
  All of WireMock's features are accessible via its REST (JSON) interface and its Java API.
  Additionally, stubs can be configured via JSON files. Read the full doc here.
---

WireMock is a popular open-source tool for API mock testing,
with over 5 million downloads per month.
It can help you to create stable test and
development environments, isolate you from flakey 3rd parties, and
simulate APIs that don't exist yet.

<style>
  /* Hide Navigation sidebar but keep TOC */
  .md-sidebar--primary {
    width: 0
  }
</style>

## Getting Started

<div class="grid-container">
  <a class="card" href="./overview">
    <img src="../images/logos/doc-sections/summary.svg" />
    Overview
  </a>
  <a class="card" href="./getting-started">
    <img
      src="../images/logos/doc-sections/quickstart.svg"
    />
    Quick Start
  </a>
  <a class="card" href="./download-and-installation">
    <img src="../images/logos/doc-sections/download.svg" />
    Download
  </a>
  <a class="card" href="../support">
    <img src="../images/logos/doc-sections/help.svg" />
    Get Help
  </a>
</div>

## Distributions

WireMock provides the following generic distributions that allow running it as a
[standalone server](./standalone.md)
in a container or within a Java Virtual Machine.

<div class="grid-container">
  <a class="card" href="./running-standalone">
    <img src="../images/logos/technology/jar.svg" />
    Standalone JAR
  </a>
  <a class="card" href="./docker">
    <img src="../images/logos/technology/docker.svg" />
    Docker
  </a>
  <a class="card" href="./solutions/kubernetes">
    <img src="../images/logos/technology/helm.svg" />
    Helm (Experimental)
  </a>
  <a
    class="card"
    href="https://www.wiremock.io/product?utm_medium=referral&utm_sourcewiremock.org&utm_content=docs_nav"
    target="_blank"
  >
    <img
      src="../images/wiremock-cloud/wiremock_cloud_favicon.svg"
    />
    WireMock Cloud (commercial SaaS)
  </a>
  <a class="card" href="https://www.npmjs.com/package/wiremock" target="_blank">
    <img src="../images/logos/technology/npm.svg" />
    NPM
  </a>
</div>

## By use-case

Below you can find links to the documentation for WireMock key use-cases.
You can find more documentation pages on the sidebar.

<div class="grid-container">
  <a class="card card-use-case" href="./request-matching">
    <img
      src="../images/requestIcon.svg"
      alt="Wiremock Features"
    />
    Advanced request matching
  </a>
  <a class="card card-use-case" href="./response-templating">
    <img
      src="../images/responseIcon.svg"
      alt="wiremock dynamic response"
    />
    Dynamic response templating
  </a>
  <!-- TODO: replace by a generic test framework listing -->
  <a class="card card-use-case" href="./junit-jupiter">
    <img
      src="../images/logos/doc-sections/checklist.svg"
      alt="wiremock unit tests"
    />
    Use API Mocking in your unit tests
  </a>
  <a class="card card-use-case" href="./simulating-faults">
    <img
      src="../images/faultIcon.svg"
      alt="wiremock fault and latency"
    />
    Fault and latency injection
  </a>
  <a class="card card-use-case" href="./record-playback">
    <img
      src="../images/recordIcon.svg"
      alt="wiremock record playback"
    />
    Record / Playback
  </a>
  <!-- On the landing but no Root page
    <a class="card card-use-case" href="./">
        <img src="/images/httpIcon.svg" alt="WireMock java, python, htt APIs" />
        Java, Python, HTTP and JSON file APIs
    </a>
    -->
  <a class="card card-use-case" href="./mock-api-templates">
    <img
      src="../images/logos/doc-sections/template.svg"
      alt="WireMock API Templates"
    />
    Use pre-defined Mock API templates
  </a>
  <a class="card card-use-case" href="./extending-wiremock">
    <img
      src="../images/logos/doc-sections/extensibility.svg"
      alt="Extending WireMock"
    />
    Extending WireMock
  </a>
</div>

## By protocol

WireMock can serve all HTTP-based protocols and REST API. 
Through built-in features and extensions,
it provides additional capabilities for widely used protocols.

<div class="grid-container">
  <a class="card" href="./webhooks-and-callbacks">
    <img src="../images/logos/technology/webhooks.svg" />
    Webhooks and Callbacks
  </a>
  <a class="card" href="./https">
    <img src="../images/logos/technology/https.svg" />
    HTTPs
  </a>
  <a class="card" href="./grpc">
    <img src="../images/logos/technology/grpc.png" />
    gRPC
  </a>
  <a class="card" href="./solutions/graphql">
    <img src="../images/logos/technology/graphql.svg" />
    GraphQL
  </a>
</div>

## By technology

There are also solutions and guides for particular technologies and frameworks,
provided by the WireMock community and external contributors.

<div class="grid-container">
  <a class="card" href="./solutions/jvm">
    <img src="../images/logos/technology/java.svg" />
    Java and JVM
  </a>
  <a class="card" href="./solutions/python">
    <img src="../images/logos/technology/python.svg" />
    Python
  </a>
  <a class="card" href="./spring-boot">
    <img src="../images/logos/technology/spring.svg" />
    Spring Boot
  </a>
  <a class="card" href="./solutions/nodejs">
    <img
      class="card-image"
      src="../images/logos/technology/nodejs.svg"
    />
    Node.js
  </a>
  <a class="card" href="./solutions/android">
    <img
      class="card-image"
      src="../images/logos/technology/android.svg"
    />
    Android
  </a>
  <a class="card" href="./solutions/dotnet">
    <img
      class="card-image"
      src="../images/logos/technology/dotnet.svg"
    />
    .NET
  </a>
  <a class="card" href="./solutions/golang">
    <img
      class="card-image"
      src="../images/logos/technology/golang.svg"
    />
    Golang
  </a>
  <a class="card" href="./solutions/rust">
    <img
      class="card-image"
      src="../images/logos/technology/rust.svg"
    />
    Rust
  </a>
  <a class="card" href="./solutions/groovy">
    <img
      class="card-image"
      src="../images/logos/technology/groovy.svg"
    />
    Groovy
  </a>
  <a class="card" href="./solutions/kotlin">
    <img
      class="card-image"
      src="../images/logos/technology/kotlin.svg"
    />
    Kotlin
  </a>
  <a class="card" href="./solutions/kubernetes">
    <img
      class="card-image"
      src="../images/logos/technology/kubernetes.svg"
    />
    Kubernetes
  </a>
  <a class="card" href="./solutions/testcontainers">
    <img
      class="card-image"
      src="../images/logos/technology/testcontainers.svg"
    />
    Testcontainers
  </a>
  <a class="card" href="./solutions/quarkus">
    <img
      class="card-image"
      src="../images/logos/technology/quarkus.svg"
    />
    Quarkus
  </a>
  <a class="card" href="./solutions/c_cpp">
    <img
      class="card-image"
      src="../images/logos/technology/c.png"
    />
    C/C++
  </a>
</div>
