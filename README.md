WireMock - a web service test double for all occasions
======================================================

[![Build Status](https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml)
[![Docs](https://img.shields.io/static/v1?label=Documentation&message=public&color=green)](https://wiremock.org/docs/)
[![a](https://img.shields.io/badge/slack-Join%20us-brightgreen?style=flat&logo=slack)](https://slack.wiremock.org/)
[![Participate](https://img.shields.io/static/v1?label=Contributing&message=guide&color=orange)](./CONTRIBUTING.md)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.tomakehurst/wiremock-jre8.svg)](https://search.maven.org/artifact/com.github.tomakehurst/wiremock-jre8)

## Key Features
	
-	HTTP response stubbing, matchable on URL, header and body content patterns
-	Request verification
-	Runs in unit tests, as a standalone process or as a WAR app
-	Configurable via a fluent Java API, JSON files and JSON over HTTP
-	Record/playback of stubs
-	Fault injection
-	Per-request conditional proxying
-   Browser proxying for request inspection and replacement
-	Stateful behaviour simulation
-	Configurable response delays
 

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
