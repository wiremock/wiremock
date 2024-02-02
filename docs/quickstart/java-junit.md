---
description: 
    Write a test API Client with WireMock and JUnit 4
---

# Quick Start: API Mocking with Java and JUnit 4

This guide shows you how to write an API Unit test with WireMock and JUnit 4.

## Prerequisites

- Java 11 or 17
- Maven or Gradle, recent versions
- A Java project, based on Maven and Gradle
- A Unit test file which we will use as a playground

<!-- TODO: Would be nice to introduce an archetype or a clone-able demo repo -->

## Add WireMock Dependency to your project

WireMock is distributed via Maven Central and can be included in your project using common build tools' dependency management.
To add the standard WireMock JAR as a project dependency, put the dependencies below section of your build file.

In our test, we will also use AssertJ to verify the responses.
To send the requests, we will use the embedded HTTP client available in Java 11+.
If you want to add a Java 1.8 test, you will need to add an external HTTP Client implementation
like [Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.2.x/#).

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock</artifactId>
        <version>{{ versions.wiremock_version }}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
    ```

=== "Gradle Groovy"

    ```groovy
    testImplementation "org.wiremock:wiremock:{{ versions.wiremock_version }}"
    testImplementation "org.assertj:assertj-core:3.24.2"
    ```

## Add the WireMock rule

WireMock ships with some JUnit rules to manage the server's lifecycle
and setup/tear-down tasks.

To use WireMock's fluent API add the following import:

```java
import static com.github.tomakehurst.wiremock.client.WireMock.*;
```

To automatically start and stop WireMock per-test case, add
the following to your test class (or a superclass of it):

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080
```

## Write a test

Now you're ready to write a test case like this:

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// ...

@Test
public void exampleTest() {
    // Setup the WireMock mapping stub for the test
    stubFor(post("/my/resource")
        .withHeader("Content-Type", containing("xml"))
        .willReturn(ok()
            .withHeader("Content-Type", "text/xml")
            .withBody("<response>SUCCESS</response>")));

    // Setup HTTP POST request (with HTTP Client embedded in Java 11+)
    final HttpClient client = HttpClient.newBuilder().build();
    final HttpRequest request = HttpRequest.newBuilder()
        .uri(wiremockServer.getRequestURI("/my/resource"))
        .header("Content-Type", "text/xml")
        .POST().build();

    // Send the request and receive the response
    final HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

    // Verify the response (with AssertJ)
    assertThat(response.statusCode()).as("Wrong response status code").isEqualTo(200);
    assertThat(response.body()).as("Wrong response body").contains("<response>SUCCESS</response>");
}
```

## Extend the test

For a bit more control over the settings of the WireMock server created
by the rule you can pass a fluently built Options object to either rule's constructor.
Let's change the port numbers as an example.

### Change the port numbers

```java
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
///...

@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089).httpsPort(8443));
```

### Random port numbers

You can have WireMock (or more accurately the JVM) pick random, free
HTTP and HTTPS ports.
It is a great idea if you want to run your tests concurrently.

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());
```

Then find out which ports to use from your tests as follows:

```java
int port = wireMockRule.port();
int httpsPort = wireMockRule.httpsPort();
```

## Further reading

- For more details on verifying requests and stubbing responses, see [Stubbing](./../stubbing.md) and [Verifying](./../verifying.md)
- For more information on the JUnit rules see [The JUnit 4 Rule](./../junit-extensions.md).
- For many more examples of JUnit tests check out the
[WireMock's own acceptance tests](https://github.com/wiremock/wiremock/tree/master/src/test/java/com/github/tomakehurst/wiremock)
