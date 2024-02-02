---
description: Run WireMock inside Java without the inbuilt HTTP layer.
---

# Running WireMock without an HTTP Server

You can run Wiremock inside another process, for example:

- wrap it in a serverless function such as on AWS Lambda.
- use it as part of an application's integration tests.

[Running as a Standalone Process](./standalone/java-jar.md) works well, however, it has the overhead of a full HTTP server and HTTP calls back and forth that in some cases may not be relevant. and adds a fair bit of overhead to each call, and the memory footprint of the application.


Since Wiremock v2.32.0, the `DirectCallHttpServer` provides the ability to run a Wiremock server without interacting with an HTTP layer.

Following is an example construction and use (adapted from `DirectCallHttpServerIntegrationTest`):

```java
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServer;
import com.github.tomakehurst.wiremock.direct.DirectCallHttpServerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
// ..

DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
WireMockServer wm = new WireMockServer(wireMockConfig().httpServerFactory(factory));
wm.start(); // no-op, not required

DirectCallHttpServer server = factory.getHttpServer();

Request request = new Request() {
  // fill in with the incoming request data
}

Response response = server.stubRequest(request);
// then use the `response`'s data, and map it accordingly
```

Note that prior to Wiremock v2.32.0, you can use [the workaround as described by Jamie Tanna](https://www.jvt.me/posts/2021/04/29/wiremock-serverless/), which uses internal APIs.
