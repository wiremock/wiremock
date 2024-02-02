---
description: >
  Use WireMock's JUnit Jupiter extension
---

# Using WireMock in JUnit 5 (Jupiter)

Use WireMock's includes a JUnit Jupiter extension to: 

- simplify running of one or more WireMock instances from a Jupiter test class. 
- manage the lifecycle and configuration of one or more WireMock instances in your test case.

It supports two modes of operation: 

- declarative (simple, wiht limited configuration options).
- programmatic (less simple, and very configurable).

These options are both explained in detail below.

## Basic usage - declarative

Use `@WireMockTest`. to apply the extension to your test class declaratively by annotating it. This runs a single
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

When the above example runs, the WireMock server starts up before the first test method in the test class, and stops after the last test method has completed.

Stub mappings and requests are reset before each test method.

### Fixing the port number

To run WireMock on a fixed port, use the `httpPort` parameter to pass the extension annotation:

```java
@WireMockTest(httpPort = 8080)
public class FixedPortDeclarativeWireMockTest {
    ...
}
```

### Enabling HTTPS

You can also enable HTTPS using the `httpsEnabled` annotation parameter. By default a random port is assigned:

```java
@WireMockTest(httpsEnabled = true)
public class HttpsRandomPortDeclarativeWireMockTest {
    ...
}
```

As with the HTTP port, you can also fix the HTTPS port number using the `httpsPort` parameter:

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

In the above example, as with the declarative form, each WireMock server is started before the first test method in the test class and stopped after the
last test method has completed, with a call to reset before each test method.

However, if the extension fields are declared at the instance scope (without the `static` modifier) then each WireMock server will
be created and started before each test method and stopped after the end of the test method.

### Configuring the static DSL

If you want to use the static DSL with one of the instances you have registered programmatically, you can declare
this by calling `configureStaticDsl(true)` on the extension builder. The configuration is automatically applied when the server is started:

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
[multi-domain mocking](./multi-domain-mocking.md).

### Declarative

In declarative mode this is done by setting the `proxyMode = true` in the annotation declaration. Then, provided your app's
HTTP client honours the JVM's proxy system properties, you can specify different domain (host) names when creating stubs.

### Programmatic

When using the programmatic form, enable proxy mode using the extension builder as in the following examples.

=== "Declarative"

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

=== "Programmatic"

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

To extend `WireMockExtension`, similar to what you can do in the JUnit 4.x rule, you can create subclasses by hooking into its lifecycle events.
This is also a good approach for creating a domain-specific API mock--by adding methods to stubs and verifying specific calls.


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

!!! note 

    The constructor takes the extension's builder as its parameter. 

By making this public when constructing your extension, you can pass an instance
of the builder in, as follows:

```java
  @RegisterExtension
  static MyMockApi myMockApi =
      new MyMockApi(
          WireMockExtension.extensionOptions()
              .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
              .configureStaticDsl(true));
```

This ensures that all parameters from the builder will be set as they would if you had constructed an instance of `WireMockExtension` from it.
