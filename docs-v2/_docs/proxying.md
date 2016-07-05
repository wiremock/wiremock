---
layout: docs
title: Proxying
toc_rank: 65
redirect_from: "/proxying.html"
description: Using WireMock as a conditional proxy to another API. Intercepting and replacing responses.
---

WireMock has the ability to selectively proxy requests through to
other hosts. This supports a proxy/intercept setup where requests are by
default proxied to another (possibly real, live) service, but where
specific stubs are configured these are returned in place of the remote
service's response. Responses that the live service can't be forced to
generate on demand can thus be injected for testing. Proxying also
supports [record and playback](/docs/record-playback/).

Proxy stub mappings
===================

Proxy responses are defined in exactly the same manner as stubs, meaning
that the same request matching criteria can be used.

The following code will proxy all GET requests made to
`http://<host>:<port>/other/service/.*` to
`http://otherservice.com/approot`, e.g. when running WireMock locally a
request to `http://localhost:8080/other/service/doc/123` would be
forwarded to `http://otherservice.com/approot/other/service/doc/123`.

```java
stubFor(get(urlMatching("/other/service/.*"))
        .willReturn(aResponse().proxiedFrom("http://otherhost.com/approot")));
```

The JSON equivalent would be:

```json
{
    "request": {
        "method": "GET",
        "urlPattern": "/other/service/.*"
    },
    "response": {
        "proxyBaseUrl" : "http://otherhost.com/approot"
    }
}
```

Proxy/intercept
===============

The proxy/intercept pattern described above is achieved by adding a low
priority proxy mapping with a broad URL match and any number of higher
priority stub mappings e.g.

```java
// Low priority catch-all proxies to otherhost.com by default
stubFor(get(urlMatching(".*")).atPriority(10)
        .willReturn(aResponse().proxiedFrom("http://otherhost.com")));


// High priority stub will send a Service Unavailable response
// if the specified URL is requested
stubFor(get(urlEqualTo("/api/override/123")).atPriority(1)
        .willReturn(aResponse().withStatus(503)));            
```

Additional headers
==================

It is possible to configure the proxy to add headers before forwarding
the request to the destination:

```java
// Inject user agent to trigger rendering of mobile version of website
stubFor(get(urlMatching(".*"))
        .willReturn(aResponse()
            .proxiedFrom("http://otherhost.com")
            .withAdditionalRequestHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone)"));
```

or

```json
{
    "request": {
        "method": "GET",
        "urlPattern": ".*"
    },
    "response": {
        "proxyBaseUrl" : "http://otherhost.com",
        "additionalProxyRequestHeaders": {
            "User-Agent": "Mozilla/5.0 (iPhone; U; CPU iPhone)",
        }
    }
}
```

You can also add response headers via the same method as for non-proxy responses (see [Stubbing](/docs/stubbing/)).

Standalone shortcut
-------------------

It is possible to start the standalone running with the catch-all stub
already configured:

Then it's simply a case of adding your stub mapping `.json` files under `mappings` as usual (see [Stubbing](/docs/stubbing/)).

## Running as a browser proxy

WireMock can be made to work as a forward (browser) proxy.

One benefit of this is that it supports a website-based variant of the proxy/intercept pattern described above, allowing
you to modify specific AJAX requests or swap out CSS/Javascript files.

To configure your browser to proxy via WireMock, first start WireMock with browser proxying enabled:

```bash
$ java -jar wiremock-standalone-2.1.0-beta.jar --enable-browser-proxying --port 9999
```

Then open your browser's proxy settings and point them to the running server:
<img src="{{ base_path }}/images/firefox-proxy-screenshot.png" alt="Firefox proxy screenshot" style="width: 50%; height: auto; margin-top: 1em;"/>

After that, you can configure stubs as described in [Running Standalone](/docs/running-standalone/#configuring-wiremock-using-the-java-client) and then browse to a website. Any resources fetched whose requests are matched by stubs you have configured will be overridden by the stub's response.

So for instance, say you're visiting
a web page that fetches a user profile via an AJAX call to `/users/12345.json` and you wanted to test how it responded to a server unavailable response. You could create a stub like this and the response from the server would be swapped for a 503 response:

```java
stubFor(get(urlEqualTo("/users/12345.json"))
  .willReturn(aResponse()
  .withStatus(503)));
```

## Proxying via another proxy server

If you're inside a network that only permits HTTP traffic out to the
internet via an opaque proxy you might wish to set up proxy mappings
that route via this server. This can be configured programmatically by
passing a configuration object to the constructor of `WireMockServer` or
the JUnit rules like this:

```java
WireMockServer wireMockServer = new WireMockServer(options()
  .proxyVia("proxy.mycorp.com", 8080)
);
```

## Proxying to a target server that requires client certificate authentication


WireMock's proxy client will send a client certificate if the target
service requires it and a trust store containing the certificate is
configured:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
    .trustStorePath("/path/to/truststore.jks")
    .trustStorePassword("mostsecret")); // Defaults to "password" if omitted
```

See [Running as a Standalone Process](/docs/running-standalone/) for command line equivalent.
