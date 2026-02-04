# WireMock - flexible, open source API mocking

<p align="center">
    <a href="https://wiremock.org" target="_blank">
        <img width="512px" src="https://wiremock.org/images/logos/wiremock/logo_wide.svg" alt="WireMock Logo"/>
    </a>
</p>

<p align="center">
    <a href="https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml">
        <img src="https://github.com/tomakehurst/wiremock/actions/workflows/build-and-test.yml/badge.svg" alt="Build Status"/>
    </a>
    <a href="https://wiremock.org/docs/">
        <img src="https://img.shields.io/static/v1?label=Documentation&message=public&color=green" alt="Docs"/>
    </a>
    <a href="https://slack.wiremock.org/">
        <img src="https://img.shields.io/badge/slack-Join%20us-brightgreen?style=flat&logo=slack" alt="Join us on Slack"/>
    </a>
    <a href="./CONTRIBUTING.md">
        <img src="https://img.shields.io/static/v1?label=Contributing&message=guide&color=orange" alt="Contributing Guide"/>
    </a>
    <a href="https://search.maven.org/artifact/org.wiremock/wiremock">
        <img src="https://img.shields.io/maven-central/v/org.wiremock/wiremock.svg" alt="Maven Central"/>
    </a>
</p>

---

<table>
<tr>
<td>
<img src="https://wiremock.org/images/wiremock-cloud/wiremock_cloud_logo.png" alt="WireMock Cloud Logo" height="20" align="left">
<strong>WireMock open source is supported by <a href="https://www.wiremock.io/cloud-overview?utm_source=github.com&utm_campaign=wiremock-README.md-banner">WireMock Cloud</a>. Please consider trying it out if your team needs advanced capabilities such as OpenAPI, dynamic state, data sources and more.</strong>
</td>
</tr>
</table>

---

WireMock is the popular open source tool for API mocking, with over 6 million downloads per month,
and powers [WireMock Cloud](https://www.wiremock.io/comparison?utm_source=github.com&utm_campaign=wiremock-README.md).

It can help you to create stable test and development environments,
isolate yourself from flaky 3rd parties and simulate APIs that don’t exist yet.

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

## Recording with Capture All Headers

When recording API traffic, WireMock can capture all request headers using the `captureAllHeaders` option.
By default, only headers explicitly specified in `captureHeaders` are recorded.

### JSON API

```json
{
  "targetBaseUrl": "http://example.com",
  "captureAllHeaders": true,
  "persist": true
}
```

**Example:**

```bash
# Start recording with all headers captured
curl -X POST http://localhost:8080/__admin/recordings/start \
  -H "Content-Type: application/json" \
  -d '{
    "targetBaseUrl": "http://example.com",
    "captureAllHeaders": true
  }'

# Make requests through WireMock proxy...

# Stop recording
curl -X POST http://localhost:8080/__admin/recordings/stop
```

### Java DSL

```java
import static com.github.tomakehurst.wiremock.client.WireMock.*;

// Capture all headers
WireMock.startRecording(
    recordSpec()
        .forTarget("http://example.com")
        .captureAllHeaders()
        .build()
);

// Capture all headers with specific header settings (e.g., case-insensitive)
WireMock.startRecording(
    recordSpec()
        .forTarget("http://example.com")
        .captureAllHeaders()
        .captureHeader("Authorization", true)  // case-insensitive matching
        .build()
);
```

### Result

When `captureAllHeaders` is `true`, the generated stub mapping includes all request headers:

```json
{
  "request": {
    "url": "/api/users",
    "method": "GET",
    "headers": {
      "Accept": { "equalTo": "application/json" },
      "Authorization": { "equalTo": "Bearer token123" },
      "X-Request-Id": { "equalTo": "abc-123" },
      "User-Agent": { "equalTo": "curl/8.0" }
    }
  },
  "response": {
    "status": 200,
    "body": "..."
  }
}
```

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
