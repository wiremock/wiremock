---
description: Working with WireMock proxies.
---

# Proxying and proxy stub mappings 

WireMock has the ability to selectively proxy requests through to
other hosts. This supports a proxy/intercept setup where requests are by
default proxied to another (possibly real, live) service, but where
specific stubs are configured these are returned in place of the remote
service's response. Responses that the live service can't be forced to
generate on demand can thus be injected for testing. Proxying also
supports [record and playback](./record-playback.md).

# Proxy stub mappings

Define proxy responses in exactly the same manner as stubs&endash;meaning that the same request matching criteria can be used.

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
        "proxyBaseUrl": "http://otherhost.com/approot"
    }
}
```

# Proxy/intercept

The proxy/intercept pattern described above is achieved by adding a low
priority proxy mapping with a broad URL match and any number of higher
priority stub mappings. For example:

```java
// Low priority catch-all proxies to otherhost.com by default
stubFor(get(urlMatching(".*")).atPriority(10)
        .willReturn(aResponse().proxiedFrom("http://otherhost.com")));


// High priority stub will send a Service Unavailable response
// if the specified URL is requested
stubFor(get(urlEqualTo("/api/override/123")).atPriority(1)
        .willReturn(aResponse().withStatus(503)));
```

# Remove path prefix

The prefix of a request path can be removed before proxying the request:

```java
stubFor(get(urlEqualTo("/other/service/doc/123"))
        .willReturn(aResponse()
            .proxiedFrom("http://otherhost.com/approot")
            .withProxyUrlPrefixToRemove("/other/service")));
```

or

```json
{
    "request": {
        "method": "GET",
        "url": "/other/service/doc/123"
    },
    "response": {
        "proxyBaseUrl": "http://otherhost.com/approot",
        "proxyUrlPrefixToRemove": "/other/service"
    }
}
```

Requests using the above path are forwarded
to `http://otherhost.com/approot/doc/123`

# Additional headers

You can configure the proxy to add headers before forwarding
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
        "proxyBaseUrl": "http://otherhost.com",
        "additionalProxyRequestHeaders": {
            "User-Agent": "Mozilla/5.0 (iPhone; U; CPU iPhone)"
        }
    }
}
```

You can also add response headers via the same method as for non-proxy responses (see [Stubbing](./stubbing.md)).

## Standalone shortcut

It is possible to start the standalone running with the catch-all stub
already configured. 

Then it's simply a case of adding your stub mapping `.json` files under `mappings` as usual (see [Stubbing](./stubbing.md)).

## Running as a browser proxy

WireMock can also be made to work as a forward (browser) proxy.

One benefit of this is that it supports a website-based variant of the proxy/intercept pattern described above, allowing
you to modify specific AJAX requests or swap out CSS/Javascript files.

To configure your browser to proxy using WireMock, first start WireMock with browser proxying enabled:

```bash
$ java -jar wiremock-standalone-{{ versions.wiremock_version }}.jar --enable-browser-proxying --port 9999
```

Then open your browser's proxy settings and point them to the running server:
<img src="/images/firefox-proxy-screenshot.png" alt="Firefox proxy screenshot" style="width: 50%; height: auto; margin-top: 1em;"/>

After that, you can configure stubs as described in [Running Standalone](./standalone/java-jar.md#configuring-wiremock-using-the-java-client) and then browse to a website. Any resources fetched whose requests are matched by stubs you have configured will be overridden by the stub's response.

So for instance, say you're visiting
a web page that fetches a user profile via an AJAX call to `/users/12345.json` and you wanted to test how it responded to a server unavailable response. You could create a stub like this and the response from the server would be swapped for a 503 response:

```java
stubFor(get(urlEqualTo("/users/12345.json"))
  .willReturn(aResponse()
  .withStatus(503)));
```

Also, we can enable/disable pass through unmatched requests to the target indicated by the original requests by enabling/disabling proxyPassThrough flag. By default, flag is set to true. 

This flag can be enabled/disabled at startup either by passing CLI option while running jar as described in [Running Standalone](./standalone/java-jar.md#command-line-options) or by passing as options in Java client as shown below.

```java
WireMockServer wireMockServer = new WireMockServer(options().proxyPassThrough(false));
```

We can also update this flag without WireMock restart either by using Admin API as described in [API section](../api/#tag/System/paths/~1__admin~1settings/post) if we are running as standalone or by updating the global settings in Java client.

Json payload to update via admin API:

```json
{
  ...
  "proxyPassThrough": false
}
```

```java
WireMock.updateSettings(WireMock.getSettings().copy().proxyPassThrough(false).build());
```

### Browser proxying of HTTPS

WireMock allows forward proxying, stubbing & recording of HTTPS traffic.

This happens automatically when browser proxying is enabled.

_We strongly recommend using WireMock over HTTP to proxy HTTPS_; there are no associated security concerns, and proxying HTTPS over HTTPS is poorly supported by many clients.

Note that when clients / operating systems distinguish between HTTP & HTTPS proxies they are often referring to the scheme of the target server, not the scheme the proxy server is listening on.

#### Getting your client to trust the certificate presented by WireMock

Normally when proxying HTTPS the proxy creates a TCP tunnel between the client and the target server, so the HTTPS session is between the client and the target server.
While the proxy passes the bytes back and forward, it cannot understand them because there is end-to-end encryption between the client and the target.

WireMock needs to decrypt the traffic in order to record or replace it with stubs.
Consequently, there have to be two separate HTTPS sessions - one between WireMock and the target server, and one between the client and WireMock.
This means that when you request https://www.example.com proxied via WireMock the HTTPS certificate will be presented by WireMock, not www.example.com.
Inevitably it cannot be trusted by default - otherwise no internet traffic would be secure.

WireMock uses a root Certificate Authority private key to sign a certificate for each host that it proxies.
By default, WireMock will use a CA key store at `$HOME/.wiremock/ca-keystore.jks`.
If this key store does not exist, WireMock will generate it with a new secure private key which should be entirely private to the system on which WireMock is running.
You can provide a key store containing such a private key & certificate yourself using the `--ca-keystore`, `--ca-keystore-password` & `--ca-keystore-type` options.

> See [this script](https://github.com/tomakehurst/wiremock/blob/master/scripts/create-ca-keystore.sh)
> for an example of how to build a key & valid self-signed root certificate called
> ca-cert.crt already imported into a keystore called ca-cert.jks.

This CA certificate can be downloaded from WireMock: [http://localhost:8080/\_\_admin/certs/wiremock-ca.crt](http://localhost:8080/__admin/certs/wiremock-ca.crt).
There's a link to the certificate on the recorder UI page at [http://localhost:8080/\_\_admin/recorder](http://localhost:8080/__admin/recorder).
Trusting this certificate will trust all certificates generated by it, allowing you to browse without client warnings.

> On OS/X a certificate can be trusted by dragging ca-cert.crt onto Keychain Access,
> double clicking on the certificate and setting SSL to "always trust".

A few caveats:

-   This depends on internal sun classes; it works with OpenJDK 1.8 -> 14, but may
    stop working in future versions or on other runtimes
-   It's your responsibility to keep the private key & keystore secure - if you
    add it to your trusted certs then anyone getting hold of it could potentially
    get access to any service you use on the web.

#### Trusting targets with invalid HTTPS certificates

For convenience when acting as a _reverse_ proxy WireMock ignores HTTPS certificate problems from the target such as untrusted certificates or incorrect hostnames on the certificate.
When browser proxying, however, it is normal to proxy all traffic, often for the entire operating system.
This would present a substantial security risk, so by default WireMock will verify the target certificates when browser proxying.
You can trust specific hosts as follows:

```bash
$ java -jar wiremock-standalone-{{ versions.wiremock_version }}.jar --enable-browser-proxying --trust-proxy-target localhost --trust-proxy-target dev.mycorp.com
```

or if you're not interested in security you can trust all hosts:

```bash
$ java -jar wiremock-standalone-{{ versions.wiremock_version }}.jar --enable-browser-proxying --trust-all-proxy-targets
```

Additional trusted public certificates can also be added to the keystore
specified via the `--https-truststore`, and WireMock will then trust them without
needing the `--trust-proxy-target` parameter (so long as they match the
requested host).

#### Proxying HTTPS on the HTTPS endpoint

The only use case we can think of for this is if you are using WireMock to test
a generic HTTPS client, and want that HTTPS client to support proxying HTTPS over
HTTPS. It has several problems. However, if you really must, there is limited support
for doing so.

Please be aware that many clients do not work very well with this
configuration. For instance:

Postman seems not to cope with an HTTPS proxy even to proxy HTTP traffic.

Older versions of curl fail trying to do the CONNECT call because they try to do so
over HTTP/2 (newer versions only offer HTTP/1.1 for the CONNECT call). At time
of writing it works using `curl 7.64.1 (x86_64-apple-darwin19.0) libcurl/7.64.1 (SecureTransport) LibreSSL/2.8.3 zlib/1.2.11 nghttp2/1.39.2` as so:

```bash
curl --proxy-insecure -x https://localhost:8443 -k 'https://www.example.com/'
```

You can force HTTP/1.1 in curl as so:

```bash
curl --http1.1 --proxy-insecure -x https://localhost:8443 -k 'https://www.example.com/'
```

Please check your client's behaviour proxying via another https proxy such as
https://hub.docker.com/r/wernight/spdyproxy to see if it is a client problem before asking for help:

```bash
docker run --rm -it -p 44300:44300 wernight/spdyproxy
curl --proxy-insecure -x https://localhost:44300 -k 'https://www.example.com/'
```

#### Security concerns

Acting as a "man in the middle" for HTTPS traffic introduces risk.
Whilst best efforts have been taken to reduce your risk, you should be aware you are granting WireMock unencrypted access to all HTTPS traffic proxied using WireMock,
and that as part of its normal operation WireMock may store that traffic, in memory or on the file system, or print it to the console.
If you choose to trust the root CA certificate WireMock is using, or you choose to bypass HTTPS verification for some or all target servers,
you should understand the risk involved.

## Proxying via another proxy server

If you're inside a network that only permits HTTP traffic out to the
internet with an opaque proxy you might wish to set up proxy mappings
that route via this server. This can be configured programmatically by
passing a configuration object to the constructor of `WireMockServer` or
the JUnit rules like this:

```java
WireMockServer wireMockServer = new WireMockServer(options()
  .proxyVia("proxy.mycorp.com", 8080)
);
```

## Proxying to a target server that requires client certificate authentication

If the target service requires it, WireMock's proxy client sends a client certificate, and a trust store containing the certificate is
configured:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
    .trustStorePath("/path/to/truststore.jks")
    .trustStorePassword("mostsecret")); // Defaults to "password" if omitted
```

See [Running as a Standalone Process](./standalone/java-jar.md) for command line equivalent.
