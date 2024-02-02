---
description: >
    Frequently asked questions and best practices regarding anything WireMock.
---

# Frequently Asked Questions

In this FAQ, you can find information about what API mocking and WireMock are, along with recommendations and best practices for different challenges in various areas of WireMock.

## API mocking and WireMock as a service

### What is WireMock?

WireMock is a free API mocking tool that you can run as a standalone server, or in a hosted version via the [WireMock Cloud](https://wiremock.io/) managed service.

### What is API mocking?

API mocking involves enabling fast and reliable development and testing by creating a simple simulation of an API and using it to accept requests and return responses that are identically structured to those used in the real API. 

### When do you need to mock APIs?

Because it allows you to focus on building your app without worrying about 3rd party APIs or sandboxes breaking, API mocking is typically used during development and testing. 
Another important use is rapid prototyping of APIs that don’t yet exist.

### How do you create an API mock?

WireMock supports several approaches for creating mock APIs--in code, via its REST API, as JSON files, and by recording HTTP traffic proxied to another destination.

### What makes WireMock unique?

WireMock has a rich [matching system](./request-matching.md), allowing any part of an incoming request to be matched against complex and precise criteria.
Responses of any complexity can be dynamically generated via the Handlebars based templating system.
Finally, WireMock is easy to integrate into any workflow due to its numerous [extension points](./extending-wiremock.md) and comprehensive APIs.

### Is WireMock open source?

Yes, WireMock is a completely open source API mocking tool [GitHub repository](https://github.com/wiremock/wiremock).
If you’re looking for a hosted version of WireMock, check out [WireMock Cloud](https://wiremock.io/).

### Is WireMock a free service?

WireMock is completely free under the Apache 2.0 license.

## Technical questions

### How do I manage many mocks across different use cases and teams?

This question tends to arise at the point when it gets difficult to keep track of the intended test case(s) for which specific mocks were built.

#### Potential solutions
- Create your stubs (or most of them at least) in the test cases themselves, then [reset them](./stubbing.md#reset) each time.
- Use the [`metadata` element](./stub-metadata.md) in the stub data to tag stubs with info relating them to specific test cases.

#### Potential solutions for WireMock standalone
- Use configuration-as-code, and store your definitions in a repository. You can have a hierarchical structure of Mappings and Files to specify teams.
    - Disabling the modifying APIs after moving to configuration-as-code is also highly recommended, so that teams cannot break each other's mocks.
- Introduce "subprojects" by having each app/team to use `$WIREMOCK_URL/$PROJECT_ID` or even `$WIREMOCK_URL/$TEAM_ID/$PROJECT_ID`.
- Do performance monitoring for your instance, because a single shared WireMock instance can be overloaded if multiple teams execute performance/stress tests on it.
If the workload is exceeded, you can split it into multiple instances, or consider [WireMock Cloud](https://www.wiremock.io/) which is scalable.
