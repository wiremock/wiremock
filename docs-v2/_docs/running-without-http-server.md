---
layout: docs
title: Running without the HTTP Server
toc_rank: 42
redirect_from: "/running-without-http-server.html"
description: Running WireMock inside a Java process, without running the inbuilt HTTP layer.
---
If you want to run Wiremock inside another process, such as wrapping it in a serverless function such as on AWS Lambda, or using it as part of an application's integration tests, you previously would need to resort to [Running as a Standalone Process](/docs/running-standalone/).

This works well, but has the overhead of a full HTTP server and HTTP calls back and forth that in some cases may not be relevant, and adds a fair bit of overhead to each call, and the memory footprint of the application.

Since Wiremock v2.32.2, the `DirectCallHttpServer` provides the ability to run a Wiremock server without ever interacting with an HTTP layer.

It can be constructed and used like so (example usage is adapted from `DirectCallHttpServerIntegrationTest`):

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

Note that prior to Wiremock v2.32.2, you can use [the workaround as described by Jamie Tanna](https://www.jvt.me/posts/2021/04/29/wiremock-serverless/), which uses internal APIs for this.
