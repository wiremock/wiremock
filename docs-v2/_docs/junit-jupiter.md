---
layout: docs
title: 'JUnit 5+ Jupiter Usage'
toc_rank: 21
description: Running WireMock with the JUnit 5 Jupiter test framework. 
---

The JUnit Jupiter extension simplifies running of one or more WireMock instances in a Jupiter test class.

It supports two modes of operation - declarative (simple, limited configuration options) and programmatic (less simple, very configurable).
These are both explained in detail below.

## Basic usage - declarative
The extension can be applied to your test class declaratively by annotating it with `@WireMockTest`. This will run a single 
WireMock server, defaulting to a random port, HTTP only (no HTTPS).

To get the running port number, base URL or a DSL instance you can declare a parameter of type `WireMockRuntimeInfo`
in your test or lifecycle methods.

```java
@WireMockTest
public class DeclarativeWireMockTest {

    @Test
    void test_something_with_wiremock(WireMockRuntimeInfo wmRuntimeInfo) {
        // The static DSL will be automatically configured for you
        stubFor(get("/static-dsl").willReturn(ok()));
      
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/instance-dsl").willReturn(ok()));
       
        // Info such as port numbers is also available
        int port = wmRuntimeInfo.getHttpPort();
        
        // Do some testing...
    }
}
```

### WireMock server lifecycle
In the above example a WireMock server will be started before the first test method in the test class and stopped after the
last test method has completed.

Stub mappings and requests will be reset before each test method.


### Fixing the port number
If you need to run WireMock on a fixed port you can pass this via the `httpPort` parameter to the extension annotation:

```java
@WireMockTest(httpPort = 8080)
public class FixedPortDeclarativeWireMockTest {
    ...
}
```

### Enabling HTTPS
You can also enable HTTPS via the `httpsEnabled` annotation parameter. By default a random port will be assigned:

```java
@WireMockTest(httpsEnabled = true)
public class HttpsRandomPortDeclarativeWireMockTest {
    ...
}
```

But like with the HTTP port you can also fix the HTTPS port number via the `httpsPort` parameter: 

```java
@WireMockTest(httpsEnabled = true, httpsPort = 8443)
public class HttpsFixedPortDeclarativeWireMockTest {
    ...
}
```



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


## Proxy mode
The JUnit Jupiter extension can be configured to enable "proxy mode" which simplifies configuration and supports
[multi-domain mocking](/docs/multi-domain-mocking/).

### Declarative
In declarative mode this is done by setting the `proxyMode = true` in the annotation declaration. Then, provided your app's
HTTP client honours the JVM's proxy system properties, you can specify different domain (host) names when creating stubs:

```java
@WireMockTest(proxyMode = true)
public class JUnitJupiterExtensionJvmProxyDeclarativeTest {

  CloseableHttpClient client;

  @BeforeEach
  void init() {
    client = HttpClientBuilder.create()
      .useSystemProperties() // This must be enabled for auto proxy config
      .build();
  }

  @Test
  void configures_jvm_proxy_and_enables_browser_proxying() throws Exception {
    stubFor(get("/things")
      .withHost(equalTo("one.my.domain"))
      .willReturn(ok("1")));

    stubFor(get("/things")
      .withHost(equalTo("two.my.domain"))
      .willReturn(ok("2")));

    assertThat(getContent("http://one.my.domain/things"), is("1"));
    assertThat(getContent("http://two.my.domain/things"), is("2"));
  }

  private String getContent(String url) throws Exception {
    try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return EntityUtils.toString(response.getEntity());
    }
  }
}
```

### Programmatic
Proxy mode can be enabled via the extension builder when using the programmatic form:

```java
public class JUnitJupiterProgrammaticProxyTest {

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
    .proxyMode(true)
    .build();

  CloseableHttpClient client;

  @BeforeEach
  void init() {
    client = HttpClientBuilder.create()
      .useSystemProperties() // This must be enabled for auto proxy config
      .build();
  }

  @Test
  void configures_jvm_proxy_and_enables_browser_proxying() throws Exception {
    wm.stubFor(get("/things")
      .withHost(equalTo("one.my.domain"))
      .willReturn(ok("1")));

    wm.stubFor(get("/things")
      .withHost(equalTo("two.my.domain"))
      .willReturn(ok("2")));

    assertThat(getContent("http://one.my.domain/things"), is("1"));
    assertThat(getContent("http://two.my.domain/things"), is("2"));
  }

  private String getContent(String url) throws Exception {
    try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
      return EntityUtils.toString(response.getEntity());
    }
  }
}
```

## Subclassing the extension

Like the JUnit 4.x rule, `WireMockExtension` can be subclassed in order to extend its behaviour by hooking into its lifecycle events.
This can also be a good approach for creating a domain-specific API mock, by adding methods to stub and verify specific calls.

```java
public class MyMockApi extends WireMockExtension {

    public MyMockApi(WireMockExtension.Builder builder) {
      super(builder);
    }

    @Override
    protected void onBeforeAll(WireMockRuntimeInfo wireMockRuntimeInfo) {
      // Do things before any tests have run
    }

    @Override
    protected void onBeforeEach(WireMockRuntimeInfo wireMockRuntimeInfo) {
      // Do things before each test
    }

    @Override
    protected void onAfterEach(WireMockRuntimeInfo wireMockRuntimeInfo) {
      // Do things after each test
    }

    @Override
    protected void onAfterAll(WireMockRuntimeInfo wireMockRuntimeInfo) {
      // Do things after all tests have run
    }
}
```

Note the constructor, which takes the extension's builder as its parameter. By making this public, you can pass an instance
of the builder in when constructing your extension as follows:

```java
  @RegisterExtension
  static MyMockApi myMockApi =
      new MyMockApi(
          WireMockExtension.extensionOptions()
              .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
              .configureStaticDsl(true));
```

This will ensure that all parameters from the builder will be set as they would if you had constructed an instance of
`WireMockExtension` from it.
