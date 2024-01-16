# WireMock - flexible and open source API mocking

<p align="center">
    <a href="https://wiremock.org" target="_blank">
        <img width="512px" src="https://wiremock.org/images/logos/wiremock/logo_wide.svg" alt="WireMock Logo"/>
    </a>
</p> 

[![Build Status](https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml)
[![Docs](https://img.shields.io/static/v1?label=Documentation&message=public&color=green)](https://wiremock.org/docs/)
[![a](https://img.shields.io/badge/slack-Join%20us-brightgreen?style=flat&logo=slack)](https://slack.wiremock.org/)
[![Participate](https://img.shields.io/static/v1?label=Contributing&message=guide&color=orange)](./CONTRIBUTING.md)
[![Maven Central](https://img.shields.io/maven-central/v/org.wiremock/wiremock.svg)](https://search.maven.org/artifact/org.wiremock/wiremock)

WireMock is a popular open-source tool for API mock testing with over 5 million downloads per month.
It can help you to create stable test and development environments,
isolate yourself from flakey 3rd parties and simulate APIs that don’t exist yet.

Started in 2011 as a Java library by [Tom Akehurst](https://github.com/tomakehurst),
now WireMock spans across multiple programming languages and technology stacks.
It can run as a library or client wrapper in many languages, or as a standalone server.
There is a big community behind the project and its ecosystem.

WireMock supports several approaches for creating mock APIs -
in code, via its REST API, as JSON files and by recording HTTP traffic proxied to another destination.
WireMock has a rich matching system, allowing any part of an incoming request to be matched against complex and precise criteria.
Responses of any complexity can be dynamically generated via the Handlebars based templating system.
Finally, WireMock is easy to integrate into any workflow due to its numerous extension points and comprehensive APIs.

## Key Features

WireMock can run in unit tests, as a standalone process or a container.
Key features include:

- HTTP response stubbing, matchable on URL, header and body content patterns
- Configuration via a fluent Java API, JSON files and JSON over HTTP
- Record/playback of stubs
- Request verification
- Fault and response delays injection
- Per-request conditional proxying
- Browser proxying for request inspection and replacement
- Stateful behaviour simulation
- Extensibility

Full documentation can be found at [wiremock.org/docs](https://wiremock.org/docs).

## Questions and Issues

If you have a question about WireMock, or are experiencing a problem you're not sure is a bug please post a message to the
[WireMock Community Slack](https://slack.wiremock.org) in the `#help` channel.

On the other hand if you're pretty certain you've found a bug please open an issue.

## Log4j Notice

WireMock only uses log4j in its test dependencies. Neither the thin nor standalone JAR depends on or embeds log4j, so
you can continue to use WireMock 2.32.0 and above without any risk of exposure to the recently discovered vulnerability.

## Contributing

WireMock exists and continues to thrive due to the efforts of contributors.
Regardless of your expertise and time you could dedicate,
there're opportunities to participate and help the project!

See the [Contributing Guide](./CONTRIBUTING.md) for more information.
