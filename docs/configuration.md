---
description: progammatic config in Java.
---

# Configuring WireMock in Java
// consider adding an introductory sentence here

Both `WireMockServer` and the `WireMockRule` take a configuration builder as the parameter to their constructor like this example:

```java
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

WireMockServer wm = new WireMockServer(options().port(2345));

@Rule
WireMockRule wm = new WireMockRule(options().port(2345));
```

Every option has a sensible default, so only options for which you require an override should be specified.

## Network ports and binding

```java
// Statically set the HTTP port number. Defaults to 8080.
.port(8000)

// Statically set the HTTPS port number. Defaults to 8443.
.httpsPort(8001)

// Randomly assign the HTTP port on startup
.dynamicPort()

// Randomly asssign the HTTPS port on startup
.dynamicHttpsPort()

// Bind the WireMock server to this IP address locally. Defaults to the loopback adaptor.
.bindAddress("192.168.1.111")
```

## Jetty configuration

Typically it is only necessary to tweak these settings if you are doing performance testing under significant loads.

```java
// Set the number of request handling threads in Jetty. Defaults to 10.
.containerThreads(5)

// Set the number of connection acceptor threads in Jetty. Defaults to 2.
.jettyAcceptors(4)

// Set the Jetty accept queue size. Defaults to Jetty's default of unbounded.
.jettyAcceptQueueSize(100)

 // Set the size of Jetty's header buffer (to avoid exceptions when very large request headers are sent). Defaults to 8192.
.jettyHeaderBufferSize(16834)

// Enable asynchronous request processing in Jetty. Recommended when using WireMock for performance testing with delays, as it allows much more efficient use of container threads and therefore higher throughput. Defaults to false.
.asynchronousResponseEnabled(true)

// Set the number of asynchronous response threads. Effective only with asynchronousResponseEnabled=true. Defaults to 10.
.asynchronousResponseThreads(10)
```

## HTTPS configuration

WireMock can accept HTTPS connections from clients, require a client to present a certificate for authentication, and pass a client certificate on to another service when proxying.

```java
// Set the keystore containing the HTTPS certificate
.keystorePath("/path/to/https-certs-keystore.jks")

// Set the password to the keystore. Note: the behaviour of this changed in version 2.27.0.
// Previously this set Jetty's key manager password, whereas now it sets the keystore password value.
.keystorePassword("verysecret!")

// Set the password to the Jetty's key manager. Note: added in version 2.27.0.
.keyManagerPassword("donttell")

// Set the keystore type
.keystoreType("BKS")

// Require a client calling WireMock to present a client certificate
.needClientAuth(true)

// Path to the trust store containing the client certificate required in by the previous parameter
.trustStorePath("/path/to/trust-store.jks")

// The password to the trust store
.trustStorePassword("trustme")
```

WireMock uses the trust store for three purposes:

- As a server, when requiring client auth, WireMock will trust the client if it presents a public certificate in this trust store.
- As a proxy, WireMock will use the private key & certificate in this key store to authenticate its http client with target servers that require client auth.
- As a proxy, WireMock will trust a target server if it presents a public certificate in this trust store.

## Proxy settings

```java
// Set the timeout for requests to the proxy in milliseconds
.proxyTimeout(5000)

// Make WireMock behave as a forward proxy e.g. via browser proxy settings
.enableBrowserProxying(true)

// Send the Host header in the original request onwards to the system being proxied to
.preserveHostHeader(false)

 // Override the Host header sent when reverse proxying to another system (this and the previous parameter are mutually exclusive)
.proxyHostHeader("my.otherdomain.com")

 // When reverse proxying, also route via the specified forward proxy (useful inside corporate firewalls)
.proxyVia("my.corporate.proxy", 8080)

// When proxying, path to a security store containing client private keys and trusted public certificates for communicating with a target server
.trustStorePath("/path/to/trust-store.jks")

// The password to the trust store
.trustStorePassword("trustme")

// When proxying, a key store containing a root Certificate Authority private key and certificate that can be used to sign generated certificates
.caKeystorePath("/path/to/ca-key-store.jks")

// The password to the CA key store
.caKeystorePassword("trustme")

// The type of the CA key store
.caKeystoreType("JKS")
```

## File locations

If not configured otherwise, WireMock defaults to the filesystem root `src/test/resources` upon startup.

```java
// Set the root of the filesystem WireMock will look under for files and mappings
.usingFilesUnderDirectory("/path/to/files-and-mappings-root")

// Set a path within the classpath as the filesystem root
.usingFilesUnderClasspath("root/path/under/classpath")
```

## Request journal

 Wiremock contains a request journal that records all requests it receives. The verification features require this, and will throw errors if it is disabled.

```java
// Do not record received requests. Typically needed during load testing to avoid JVM heap exhaustion.
.disableRequestJournal()

// Limit the size of the request log (for the same reason as above).
.maxRequestJournalEntries(Optional.of(100))
```

## Notification (logging)

WireMock wraps all logging in its own `Notifier` interface. It ships with no-op, Slf4j and console (stdout) implementations.

```java
// Provide an alternative notifier. The default logs to slf4j.
.notifier(new ConsoleNotifier(true))
```

## Gzip

Gzipping of responses can be disabled.

```java
.gzipDisabled(true)
```

## Extensions

For details see [Extending WireMock](./extending-wiremock.md).

```java
// Add extensions
.extensions("com.mycorp.ExtensionOne", "com.mycorp.ExtensionTwo")
```

## Transfer encoding

By default WireMock sends all responses chunk encoded, which means it has a `Transfer-Encoding: chunked` header present and no `Content-Length` header.

You can modify the encoding policy:

```java
.useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
```

Valid values are:

-   `NEVER` - Never use chunked encoding. Warning: this will buffer all response bodies in order to calculate the size.
    This might put a lot of strain on the garbage collector if you're using large response bodies.
-   `BODY_FILE` - Use chunked encoding for body files but calculate a `Content-Length` for directly configured bodies.
-   `ALWAYS` - Always use chunk encoding - the default.

## Cross-origin response headers (CORS)

WireMock always sends CORS headers with admin API responses, but not by default with stub responses.
To enable automatic sending of CORS headers on stub responses, do the following:

```java
.stubCorsEnabled(true)
```

## Limiting logged response body size

By default, the journal records each entire response body. To prevent out-of-memory errors when working with large response bodies, you can set a limit, in bytes, to the response body size, with the result that WireMock truncates the larger ones upon saving.

```java
.maxLoggedResponseSize(100000) // bytes
```

## Preventing proxying to and recording from specific target addresses

For security, you can set limits on prxying from WireMock, using using rules lists that specify allowed and denied addresses. WireMock evaluates the allowed list first.

Each rule can be one of the following:

* A single IP address
* An IP address range, as in `10.1.1.1-10.2.2.2`
* A hostname wildcard, as in `dev-*.example.com`

The ruleset is built and applied as in the following example:

```java
.limitProxyTargets(NetworkAddressRules.builder()
  .allow("192.168.56.42")
  .allow("192.0.1.1-192.168.254.1")
  .deny("*.acme.com")
  .build()
)
```

## Filename template

WireMock can set up specific filename template formats based on stub information.
The main rule for set up specify stub metadata information in handlebar format.
For instance for endpoint `PUT /hosts/{id}` and format
{% raw %} `{{{method}}}-{{{request.url}}}.json`{% endraw %}
will be generated: `put-hosts-id.json` filename.
Default template: {% raw %} `{{{method}}}-{{{path}}}-{{{id}}}.json` {% endraw %}.

{% raw %}

```java
.filenameTemplate("{{{request.url}}}-{{{request.url}}}.json")
```

{% endraw %}

Note: starting from [3.0.0-beta-8](https://github.com/wiremock/wiremock/releases/tag/3.0.0-beta-8)

## Listening for raw traffic

When debugging, you can observe raw HTTP traffic to and from Jetty
 with a `WiremockNetworkTrafficListener`.

 !!! Warning ""

   Using WireMock's request listener extension points in some cases do not show some  alterations that Jetty can make, in the response from Wiremock, before sending it to the client. For example, if you set it to append a --gzip postfix to the ETag response header for gzipped responses, it does not show.



To output all raw traffic to console use `ConsoleNotifyingWiremockNetworkTrafficListener`, for example:

```java
.networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener()));
```

To collect raw traffic for other purposes, like adding to your acceptance test's output,
you can use the `CollectingNetworkTrafficListener`.
