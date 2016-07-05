---
layout: docs
title: 'Configuration'
toc_rank: 40
description: Configuring WireMockServer and the JUnit rule programmatically.
---

Both ``WireMockServer`` and the ``WireMockRule`` take a configuration builder as the parameter to their constructor e.g.

```java
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

WireMockServer wm = new WireMockServer(options().port(2345));

@Rule
WireMockRule wm = new WireMockRule(options().port(2345));
```

Every option has a sensible default, so only options that you require an override for should be specified.

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
```

## HTTPS configuration

WireMock can accept HTTPS connections from clients, require a client to present a certificate for authentication, and pass a client certificate on to another service when proxying.

```java
// Set the keystore containing the HTTPS certificate
.keystorePath("/path/to/https-certs-keystore.jks")

// Set the password to the keystore
.keystorePassword("verysecret!")

// Set the keystore type
.keystoreType("BKS")

// Require a client calling WireMock to present a client certificate
.needClientAuth(true)

// Path to the trust store containing the client certificate required in by the previous parameter
.trustStorePath("/path/to/trust-store.jks")

// The password to the trust store
.trustStorePassword("trustme")
```

The client certificate in the trust store defined in the last two options will also be used when proxying to another service that requires a client certificate for authentication.

## Proxy settings

```java
// Make WireMock behave as a forward proxy e.g. via browser proxy settings
.enableBrowserProxying(true)

// Send the Host header in the original request onwards to the system being proxied to
.preserveHostHeader(false)

 // Override the Host header sent when reverse proxying to another system (this and the previous parameter are mutually exclusive)
.proxyHostHeader("my.otherdomain.com")

 // When reverse proxying, also route via the specified forward proxy (useful inside corporate firewalls)
.proxyVia("my.corporate.proxy", 8080)
```


## File locations

WireMock, when started programmatically, will default to `src/test/resources` as a filesystem root if not configured otherwise.

```java
// Set the root of the filesystem WireMock will look under for files and mappings
.usingFilesUnderDirectory("/path/to/files-and-mappings-root")

// Set a path within the classpath as the filesystem root
.usingFilesUnderClasspath("root/path/under/classpath")
```

## Request journal

The request journal records requests received by WireMock. It is required by the verification features, so these will throw errors if it is disabled.

```java
// Do not record received requests. Typically needed during load testing to avoid JVM heap exhaustion.
.disableRequestJournal()

// Limit the size of the request log (for the same reason as above).
.maxRequestJournalEntries(Optional.of(100))
```

## Notification (logging)

WireMock wraps all logging in its own ``Notifier`` interface. It ships with no-op, Slf4j and console (stdout) implementations.

```java
// Provide an alternative notifier. The default logs to slf4j.
.notifier(new ConsoleNotifier(true))
```


## Extensions

For details see [Extending WireMock](/docs/extending-wiremock/).

```java
// Add extensions
.extensions("com.mycorp.ExtensionOne", "com.mycorp.ExtensionTwo")
```
