---
description: >
    Manage one or more WireMock instances in your test cases with
    WireMock's JUnit Rule
---

# Using WireMock in JUnit 4 and Vintage

Manage one or more WireMock instances in your test cases with WireMock's JUnit Rule. 

The JUnit rule can handle the lifecycle for you, starting the server before
each test method and stopping it afterwards.

!!! note

    Compatible with JUnit 4.x and JUnit 5 Vintage.

## Basic usage

Make WireMock available to your tests on its default port (8080), along with the WireMock rule:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule();
```

You can add an `Options` instance to the rule's constructor, to override
various settings. 

This example creates an `Options` implementation using the
`WireMockConfiguration.options()` builder:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(options().port(8888).httpsPort(8889));
```

See [Configuration](./configuration.md) for additional details.

## Unmatched requests

The JUnit rule verifies that all requests received during the course of a test case are served by a configured stub, rather than the default 404. If any are not, the test fails and 
throws a `VerificationException`. 

You can disable this behaviour by passing an extra constructor flag:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(options().port(8888), false);
```

## Other @Rule configurations

With a bit more effort you can make the WireMock server continue to run
between test cases. This is easiest in JUnit 4.10:

```java
@ClassRule
@Rule
public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);
```

Unfortunately JUnit 4.11 and above prohibits `@Rule` on static members so a
slightly more verbose form is required, as in the following:

```java
@ClassRule
public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);

@Rule
public WireMockClassRule instanceRule = wireMockRule;
```


## Accessing the stubbing and verification DSL from the rule

In addition to the static methods on the `WireMock` you can also
configure stubs etc. via the rule object directly. 

There are two advantages to this:
- it's a bit faster as it avoids sending
commands over HTTP.
- if you want to mock multiple services you can
declare a rule per service but not have to create a client object for
each.

For example:

```java
@Rule
public WireMockRule service1 = new WireMockRule(8081);

@Rule
public WireMockRule service2 = new WireMockRule(8082);

@Test
public void bothServicesDoStuff() {
    service1.stubFor(get(urlEqualTo("/blah")).....);
    service2.stubFor(post(urlEqualTo("/blap")).....);

    ...
}
```
