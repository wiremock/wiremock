---
description: WireMock allows for several approaches to mocking APIs, including multi-domain mocking.
---

# Multi-domain API Mocking with WireMock

While running a WireMock instance per API is typical, and requires that you mock and configure your app to treat the instances as endpoints, you can also mock multiple APIs in a single instance by the use of proxying and hostname matching features.

There are advantages of this approach: 

- lower overhead (memory, startup/shutdown time).
- no need to modify each base URL in your app's configuration. 
- avoid some of the headaches associated with binding to random ports.

Key steps to enabling this configuration are:

1. Enable browser (forward) proxying using `.enableBrowserProxying(true)` in the startup options.
2. Configure the JVM's proxy settings to point to the WireMock instance using `JvmProxyConfigurer`.

The following sections detail how to achieve this in various deployment contexts.

## Configuring for JUnit Jupiter

The simplest way to enable this mode if you're using JUnit Jupiter it to toggle it on using the `WireMockExtension`. See the
[Junit Jupiter Proxy Mode](./junit-jupiter.md#proxy-mode) for details.

## Configuring for JUnit 4.x

To use this mode with the JUnit 4.x rule:

1. Create the rule as usual with browser proxying enabled.
2. Ensure our HTTP client (the one used by our app to talk to the API we're mocking) honours the system properties relating to proxy servers.
3. Set the proxy properties using `JvmProxyConfigurer` before each test case and unset them afterwards.
4. Specify the host name we're targeting when creating stubs.

```java
public class MultiDomainJUnit4Test {

  @Rule
  public WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .enableBrowserProxying(true)
  );

  HttpClient httpClient = HttpClientBuilder.create()
    .useSystemProperties() // This must be enabled for auto proxy config
    .build();

  @Before
  public void init() {
    JvmProxyConfigurer.configureFor(wm);
  }

  @After
  public void cleanup() {
    JvmProxyConfigurer.restorePrevious();
  }

  @Test
  public void testViaProxy() throws Exception {
      wm.stubFor(get("/things")
        .withHost(equalTo("my.first.domain"))
        .willReturn(ok("Domain 1")));

      wm.stubFor(get("/things")
        .withHost(equalTo("my.second.domain"))
        .willReturn(ok("Domain 2")));

      HttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
      String responseBody = EntityUtils.toString(response.getEntity());
      assertEquals("Domain 1", responseBody);

      response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
      responseBody = EntityUtils.toString(response.getEntity());
      assertEquals("Domain 2", responseBody);
  }
}
```

## Configuring for other Java

To use this mode from Java code:

1. Create and start a `WireMockServer` instance with browser proxying enabled.
2. Ensure our HTTP client (the one used by our app to talk to the API we're mocking) honours the system properties relating to proxy servers.
3. Set the proxy properties using `JvmProxyConfigurer` before each bit of work and unset them afterwards.
4. Specify the host name we're targeting when creating stubs.

```java
public void testViaProxyUsingServer() throws Exception {
  WireMockServer wireMockServer = new WireMockServer(options()
    .dynamicPort()
    .enableBrowserProxying(true)
  );
  wireMockServer.start();

  HttpClient httpClient = HttpClientBuilder.create()
    .useSystemProperties() // This must be enabled for auto proxy config
    .build();

  JvmProxyConfigurer.configureFor(wireMockServer);

  wireMockServer.stubFor(get("/things")
    .withHost(equalTo("my.first.domain"))
    .willReturn(ok("Domain 1")));

  wireMockServer.stubFor(get("/things")
    .withHost(equalTo("my.second.domain"))
    .willReturn(ok("Domain 2")));

  HttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
  String responseBody = EntityUtils.toString(response.getEntity()); // Should == Domain 1

  response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
  responseBody = EntityUtils.toString(response.getEntity()); // Should == Domain 2

  wireMockServer.stop();
  JvmProxyConfigurer.restorePrevious();
}
```
