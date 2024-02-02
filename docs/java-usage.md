---
description: >
    At runtime, programmatically create, start and stop the server for using WireMock from Java.
---

# Running WireMock from plain Java and other JVM Languages

Programmatically create, start, and stop the server for using WireMock from Java, outside of JUnit.


## The Server

If you want to use WireMock from Java (or any other JVM language)
outside of JUnit, you can programmatically create, start, and stop the
server:

```java
WireMockServer wireMockServer = new WireMockServer(options().port(8089)); //No-args constructor will start on port 8080, no HTTPS
wireMockServer.start();

// Sometime later

wireMockServer.stop();
```

!!! info 

    If you're neither using JUnit nor any of the WireMock rules, to suitably manage the 
    server lifecycle, you can construct and start the server directly.

For more details of the `options()` builder accepted by the constructor see [Configuration](./configuration.md) for details.

As an alternative to calling the client, you can call the stubbing/verifying DSL from the server object. 
This works similarly to stubbing and verification using the [JUnit rule](./junit-extensions.md) 

### Managing ports

To change the port number and/or you're running the server on
another host, you must tell the client:

```java
WireMock.configureFor("wiremock.host", 8089);
```

When you deploy into a servlet container under a path other
than root, you need to set that too:

```java
WireMock.configureFor("tomcat.host", 8080, "/wiremock");
```

## The Client

The `WireMock` class provides an over-the-wire client to a WireMock
server (the local one by default).

### Configuring for static calls

To configure the static client for an alternative host and port:

```java
import static com.github.tomakehurst.wiremock.client.WireMock.*;

configureFor("wiremock.host", 8089);
stubFor(get(....));
```

As above, when you deploy the server into a servlet container under a path
other than root, you'll need to set that too:

```java
WireMock.configureFor("tomcat.host", 8080, "/wiremock");
```

### Newing up

If you need to talk to more than one server instance, new `WireMock` instances are useful.

Create a new instance as follows:

```java
WireMock wireMock = new WireMock("some.host", 9090, "/wm"); // As above, 3rd param is for non-root servlet deployments
wireMock.register(get(....)); // Equivalent to stubFor()
```
