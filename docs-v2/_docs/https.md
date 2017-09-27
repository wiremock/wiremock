---
layout: docs
title: HTTPS
toc_rank: 100
redirect_from: "/https.html"
description: Using WireMock with HTTPS.
---

<div class="mocklab-callout"> 
  <p class="mocklab-callout__text">
    HTTPS configuration can be tricky to get right. <strong>MockLab</strong> provides a hosted, 100% WireMock compatible mocking service, freeing you from the hassles of SSL, DNS and server configuration.    
  </p>
  <a href="http://get.mocklab.io/?utm_source=wiremock.org&utm_medium=docs-callout&utm_campaign=https" title="Learn more" class="mocklab-callout__learn-more-button">Learn more</a>
</div>

WireMock can optionally accept requests over HTTPS. By default it will serve its own self-signed TLS certificate, but this can be
overridden if required by providing a keystore containing another certificate.

## Handling HTTPS requests

To enable HTTPS using WireMock's self-signed certificate just specify an
HTTPS port:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443));
```

To use your own keystore you can specify its path and optionally its
password:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
    .httpsPort(8443)
    .keystorePath("/path/to/keystore.jks")
    .keystorePassword("verysecret")); // Defaults to "password" if omitted
```

The keystore type defaults to JKS, but this can be changed if you're using another keystore format e.g. Bouncycastle's BKS with Android:

```java
.keystoreType("BKS")
```

## Requiring client certificates

To make WireMock require clients to authenticate via a certificate you
need to supply a trust store containing the certs to trust and enable
client auth:

```java
@Rule
public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
    .httpsPort(8443)
    .needClientAuth(true)
    .trustStorePath("/path/to/truststore.jks")
    .trustStorePassword("mostsecret")); // Defaults to "password" if omitted
```

If you using WireMock as a proxy onto another system which requires client certificate authentication, you will also need to
specify a trust store containing the certificate(s).

## Common HTTPS issues

`javax.net.ssl.SSLException: Unrecognized SSL message, plaintext connection?`: Usually means you've tried to connect to the
HTTP port with a client that's expecting HTTPS (i.e. has https:// in the URL).

`org.apache.http.NoHttpResponseException: The target server failed to respond`: Could mean you've tried to connect to the HTTPS port with a
client expecting HTTP.

`javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target`: You are using WireMock's default (self-signed) TLS certificate or another certificate that isn't signed by a CA. In this case you need to specifically configure your HTTP client to trust the certificate being presented, or to trust all certificates. Here is an example of [how to do this with the Apache HTTP client](https://github.com/tomakehurst/wiremock/blob/{{ site.wiremock_version }}/src/main/java/com/github/tomakehurst/wiremock/http/HttpClientFactory.java).
