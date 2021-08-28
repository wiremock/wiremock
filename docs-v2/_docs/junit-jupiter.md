---
layout: docs
title: 'JUnit 5+ Jupiter Usage'
toc_rank: 21
description: Running WireMock with the JUnit 5 Jupiter test framework. 
---

The JUnit Jupiter extension simplifies running of one or more WireMock instances in a Jupiter test class.

It supports two modes of operation - declarative (simple, not configurable) and programmatic (less simple, configurable).
These are both explained in detail below.

## Basic usage - declarative
The extension can be invoked by your test class declaratively via the `@ExtendWith` annotation. This will run a single 
WireMock server on a random port, HTTP only (no HTTPS).

To get the running port number, base URL or a DSL instance you can declare a parameter of type `WireMockRuntimeInfo`
in your test or lifecycle methods.

```java
@ExtendWith(WireMockExtension.class)
public class DeclarativeWireMockTest {

    @Test
    void test_something_with_wiremock(WireMockRuntimeInfo wmRuntimeInfo) {
        // The static DSL will be configured for you
        stubFor(get("/static-dsl").willReturn(ok()));
      
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/instance-dsl").willReturn(ok()));
        
        int port = wmRuntimeInfo.getHttpPort();
        
        // Do some testing...
    }
}
```

### WireMock server lifecycle
In the above example a WireMock server will be started before the first test method in the test class and stopped after the
last test method has completed.

Stub mappings and requests will be reset before each test method.


## Advanced usage - programmatic
Invoking the extension programmatically with `@RegisterExtension` allows you to run any number of WireMock instances and provides full control
over configuration.

```java
public class ProgrammaticWireMockTest {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    @RegisterExtension
    static WireMockExtension wm2 = WireMockExtension.newInstance()
            .options(wireMockConfig()
                     .dynamicPort()
                     .extensions(new ResponseTemplateTransformer(true)))
            .build();

    @Test
    void test_something_with_wiremock() {
        // You can get ports, base URL etc. via WireMockRuntimeInfo
        WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();
        int httpsPort = wm1RuntimeInfo.getHttpsPort();
        
        // or directly via the extension field
        int httpPort = wm1.port();  
      
        // You can use the DSL directly from the extension field
        wm1.stubFor(get("/api-1-thing").willReturn(ok()));
        
        wm2.stubFor(get("/api-2-stuff").willReturn(ok()));
    }
}
```


### Static vs. instance
In the above example, as with the declarative form, each WireMock server will be started before the first test method in the test class and stopped after the
last test method has completed, with a call to reset before each test method.

However, if the extension fields are declared at the instance scope (without the `static` modifier) each WireMock server will
be created and started before each test method and stopped after the end of the test method.


### Configuring the static DSL
If you want to use the static DSL with one of the instances you have registered programmatically you can declare
this by calling `configureStaticDsl(true)` on the extension builder. The configuration will be automatically applied when the server is started:

```java
public class AutomaticStaticDslConfigTest {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .configureStaticDsl(true)
            .build();

    @RegisterExtension
    static WireMockExtension wm2 = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    @Test
    void test_something_with_wiremock() {
        // Will communicate with the instance called wm1
        stubFor(get("/static-dsl").willReturn(ok()));
        
        // Do test stuff...
    }
}
```


## Unmatched request behaviour
By default, in either the declarative or programmatic form, if the WireMock instance receives unmatched requests during a
test run an assertion error will be thrown and the test will fail.

This behavior can be changed by calling `.failOnUnmatchedRequests(false)` on the extension builder when using the programmatic form.
