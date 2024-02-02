---
title: Overview
meta_title: WireMock Overview and Basics
toc_rank: 1
description: Top-level overview of WireMock
---

**WireMock** is a popular open-source tool for API mock testing
with over 5 million downloads per month.
It can help you to:  

- create environments for testing and development that are stable while isolating yourself from third parties.

	&mdash;and&mdash;

- simulate APIs that don't exist yet. 

Started in 2011 as a Java library by [Tom Akehurst](https://github.com/tomakehurst),
now WireMock spans across multiple programming languages and technology stacks.

WireMock can run, in many languages, as:

- a library.
- a client wrapper.
- a standalone server.

There is a large community behind the project and its ecosystem.

WireMock supports you in creating mock APIs: 

- in code, by making use of its REST API.
- as JSON objects.
- by recording HTTP traffic proxied to another destination.

WireMock has a rich matching system, allowing you to match any part of an incoming request against complex and precise criteria.
In addition, you can dynamically generate responses of any complexity using its Handlebars-based templating system.
Finally, WireMock makes it easy to use its numerous extension points and its comprehensive APIs, for integrating into your workflow.

## Key features

- HTTP response stubbing, matchable on URL, header and body content patterns
- Request verification
- Runs in unit tests, as a standalone process or as a WAR app
- Record/playback of stubs
- Configurable response delays and Fault injection
- Per-request conditional proxying
- Browser proxying for request inspection and replacement
- Stateful behaviour simulation

All the features are configurable using fluent Java API and JSON files,
or through JSON over HTTP for the standalone service.

## Getting Started

Check out WireMock Quick-starts and tutorials [here](./getting-started.md).

## WireMock Ecosystem

The WireMock ecosystem contains implementations and adapters for numerous languages and test frameworks.
It supports adapters and implementations for several technology stacks, including Python, .NET, Golang, and Rust.
For the JVM ecosystem, there are libraries for Spring Boot, Quarkus, Kotlin, Testcontainers, and others.
WireMock can also run on Android, and will soon provide official gRPC and GraphQL adapters.

You can learn more about [WireMock Ecosystem here](https://github.com/wiremock/ecosystem).
