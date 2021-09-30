---
layout: docs
title: Running as a Standalone Process
toc_rank: 41
redirect_from: "/running-standalone.html"
description: Running WireMock as a standalone mock server.
---

<div class="mocklab-callout"> 
  <p class="mocklab-callout__text">
    Configuring servers can be a major distraction from building great software. <strong>MockLab</strong> provides a hosted, 100% WireMock compatible mocking service, freeing you from the hassles of SSL, DNS and server configuration.    
  </p>
  <a href="http://get.mocklab.io/?utm_source=wiremock.org&utm_medium=docs-callout&utm_campaign=running-standalone" title="Learn more" class="mocklab-callout__learn-more-button">Learn more</a>
</div>

The WireMock server can be run in its own process, and configured via
the Java API, JSON over HTTP or JSON files.

Once you have [downloaded the standalone JAR](https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/{{ site.wiremock_version }}/wiremock-jre8-standalone-{{ site.wiremock_version }}.jar) you can run it simply by doing this:

```bash
$ java -jar wiremock-jre8-standalone-{{ site.wiremock_version }}.jar
```

## Command line options

The following can optionally be specified on the command line:

`--port`: Set the HTTP port number e.g. `--port 9999`. Use `--port 0` to dynamically determine a port.

`--disable-http`: Disable the HTTP listener, option available only if HTTPS is enabled.

`--https-port`: If specified, enables HTTPS on the supplied port.
Note: When you specify this parameter, WireMock will still, additionally, bind to an HTTP port (8080 by default). So when running multiple WireMock servers you will also need to specify the `--port` parameter in order to avoid conflicts.

`--bind-address`: The IP address the WireMock server should serve from. Binds to all local network adapters if unspecified.

`--https-keystore`: Path to a keystore file containing an SSL
certificate to use with HTTPS. The keystore must have a password of
"password". This option will only work if `--https-port` is specified.
If this option isn't used WireMock will default to its own self-signed
certificate.

`--keystore-type`: The HTTPS keystore type. Usually JKS or PKCS12.

`--keystore-password`: Password to the keystore, if something other than
"password".
Note: the behaviour of this changed in version 2.27.0. Previously this set Jetty's key manager password, whereas now it
sets the keystore password value. The key manager password can be set with the (new) parameter below. 

`--key-manager-password`: The password used by Jetty to access individual keys in the store, if something other than
"password".

`--https-truststore`: Path to a keystore file containing client public
certificates, proxy target public certificates & private keys to use when
authenticate with a proxy target that require client authentication. See
[HTTPS configuration](/docs/configuration/#https-configuration)
and [Running as a browser proxy](/docs/proxying#running-as-a-browser-proxy) for
details.

`--truststore-type`: The HTTPS trust store type. Usually JKS or PKCS12.

`--truststore-password`: Optional password to the trust store. Defaults
to "password" if not specified.

`--https-require-client-cert`: Force clients to authenticate with a
client certificate. See https for details.

`--verbose`: Turn on verbose logging to stdout

`--root-dir`: Sets the root directory, under which `mappings` and
`__files` reside. This defaults to the current directory.

`--record-mappings`: Record incoming requests as stub mappings. See
record-playback.

`--match-headers`: When in record mode, capture request headers with the
keys specified. See record-playback.

`--proxy-all`: Proxy all requests through to another base URL e.g.
`--proxy-all="http://api.someservice.com"` Typically used in conjunction
with `--record-mappings` such that a session on another service can be
recorded.

`--preserve-host-header`: When in proxy mode, it passes the Host header
as it comes from the client through to the proxied service. When this
option is not present, the Host header value is deducted from the proxy
URL. This option is only available if the `--proxy-all` option is
specified.

`--proxy-via`: When proxying requests (either by using --proxy-all or by
creating stub mappings that proxy to other hosts), route via another
proxy server (useful when inside a corporate network that only permits
internet access via an opaque proxy). e.g.
`--proxy-via webproxy.mycorp.com` (defaults to port 80) or
`--proxy-via webproxy.mycorp.com:8080`. Also supports proxy authentication,
e.g. `--proxy-via http://username:password@webproxy.mycorp.com:8080/`.

`--enable-browser-proxying`: Run as a browser proxy. See
browser-proxying.

`--ca-keystore`: A key store containing a root Certificate Authority private key
and certificate that can be used to sign generated certificates when
browser proxying https. Defaults to `$HOME/.wiremock/ca-keystore.jks`.

`--ca-keystore-password`: Password to the ca-keystore, if something other than
"password".

`--ca-keystore-type`: Type of the ca-keystore, if something other than `jks`.

`--trust-all-proxy-targets`: Trust all remote certificates when running as a
browser proxy and proxying HTTPS traffic.

`--trust-proxy-target`: Trust a specific remote endpoint's certificate when
running as a browser proxy and proxying HTTPS traffic. Can be specified multiple
times. e.g. `--trust-proxy-target dev.mycorp.com --trust-proxy-target localhost`
would allow proxying to `https://dev.mycorp.com` or `https://localhost:8443`
despite their having invalid certificate chains in some way.

`--no-request-journal`: Disable the request journal, which records
incoming requests for later verification. This allows WireMock to be run
(and serve stubs) for long periods (without resetting) without
exhausting the heap. The `--record-mappings` option isn't available if
this one is specified.

`--container-threads`: The number of threads created for incoming
requests. Defaults to 10.

`--max-request-journal-entries`: Set maximum number of entries in
request journal (if enabled). When this limit is reached oldest entries
will be discarded.

`--jetty-acceptor-threads`: The number of threads Jetty uses for
accepting requests.

`--jetty-accept-queue-size`: The Jetty queue size for accepted requests.

`--jetty-header-buffer-size`: The Jetty buffer size for request headers,
e.g. `--jetty-header-buffer-size 16384`, defaults to 8192K.

`--async-response-enabled`: Enable asynchronous request processing in Jetty. 
Recommended when using WireMock for performance testing with delays, as it allows much more efficient use of container threads and therefore higher throughput. Defaults to `false`.

`--async-response-threads`: Set the number of asynchronous (background) response threads. 
Effective only with `asynchronousResponseEnabled=true`. Defaults to 10.

`--extensions`: Extension class names e.g.
com.mycorp.HeaderTransformer,com.mycorp.BodyTransformer. See extending-wiremock.

`--print-all-network-traffic`: Print all raw incoming and outgoing network traffic to console.

`--global-response-templating`: Render all response definitions using Handlebars templates.

`--local-response-templating`: Enable rendering of response definitions using Handlebars templates for specific stub mappings.

`--max-template-cache-entries`: Set the maximum number of compiled template fragments to cache. Only has any effect when response templating is enabled. Defaults to no limit.

`--use-chunked-encoding`: Set the policy for sending responses with `Transfer-Encoding: chunked`. Valid values are `always`, `never` and `body_file`. 
The last of these will cause chunked encoding to be used only when a stub defines its response body from a file.

`--disable-gzip`: Prevent response bodies from being gzipped. 

`--disable-request-logging`: Prevent requests and responses from being sent to the notifier. Use this when performance testing as it will save memory and CPU even when info/verbose logging is not enabled. 

`--disable-banner`: Prevent WireMock logo from being printed on startup 

`--permitted-system-keys`: Comma-separated list of regular expressions for names of permitted environment variables and system properties accessible from response templates. Only has any effect when templating is enabled. Defaults to `wiremock.*`.

`--enable-stub-cors`: Enable automatic sending of cross-origin (CORS) response headers. Defaults to off.

`--help`: Show command line help

## Configuring WireMock using the Java client

The WireMock Java API can be used against a running server on a different host if required. If you're only planning to configure a single remote instance from within your program you can configure the static DSL to point to it:

```java
WireMock.configureFor("my.remote.host", 8000);

// or for HTTPS
WireMock.configureFor("https", "my.remote.host", 8443);
```

Alternatively you can create an instance of the client (or as many as there are servers to configure):

```java
WireMock wireMock1 = new WireMock("1st.remote.host", 8000);
WireMock wireMock2 = new WireMock("https", "2nd.remote.host", 8001);
```

## Configuring via JSON over HTTP

You can create a stub mapping by posting to WireMock's HTTP API:

```bash
$ curl -X POST \
--data '{ "request": { "url": "/get/this", "method": "GET" }, "response": { "status": 200, "body": "Here it is!\n" }}' \
http://localhost:8080/__admin/mappings/new
```

And then fetch it back:

```bash
$ curl http://localhost:8080/get/this
Here it is!
```

The full stubbing API syntax is described in [Stubbing](/docs/stubbing/).

## JSON file configuration

You can also use the JSON API via files. When the WireMock server starts
it creates two directories under the current one: `mappings` and
`__files`.

To create a stub like the one above by this method, drop a file with a
`.json` extension under `mappings` with the following content:

```json
{
    "request": {
        "method": "GET",
        "url": "/api/mytest"
    },
    "response": {
        "status": 200,
        "body": "More content\n"
    }
}
```

After restarting the server you should be able to do this:

```bash
$ curl http://localhost:8080/api/mytest
More content
```

See [stubbing](/docs/stubbing/) and [verifying](/docs/verifying/) for more on the JSON API.


### Multi-stub JSON files

JSON files containing multiple stub mappings can also be used. These are of the form:

```json
{
  "mappings": [
    {
      "request": {
        "method": "GET",
        "url": "/one"
      },
      "response": {
        "status": 200
      }
    },
    {
      "id": "8c5db8b0-2db4-4ad7-a99f-38c9b00da3f7",
      "request": {
        "url": "/two"
      },
      "response": {
        "body": "Updated"
      }
    }
  ]
}
```

> **note**
>
> Stubs loaded from multi-mapping files are read-only, so any attempt to update or remove (including remove all) will cause an error to be thrown. 


## Pushing JSON files to a remote WireMock instance
You can push a collection of stub mappings and associated files to a remote WireMock or MockLab instance via the
Java API as follows:

```java
WireMock wireMock = WireMock.create()
    .scheme("http")
    .host("my-wiremock.example.com")
    .port(80)
    .build();

// The root directory of the WireMock project, under which the mappings and __files directories should be found
wireMock.loadMappingsFrom("/wiremock-stuff");
``` 


## File serving

When running the standalone JAR, files placed under the `__files` directory will
be served up as if from under the docroot, except if stub mapping
matching the URL exists. For example if a file exists
`__files/things/myfile.html` and no stub mapping will match
`/things/myfile.html` then hitting
`http://<host>:<port>/things/myfile.html` will serve the file.

## Packaging the stubs into a standalone JAR

If you want to package your stubs into the standalone JAR, so you can distribute an executable JAR with all the stubs intact, you can do this using the `--load-resources-from-classpath` option.

For example, let's say have the following directory structure:

```
src/main/resources
src/main/resources/wiremock-stuff
src/main/resources/wiremock-stuff/__files
src/main/resources/wiremock-stuff/mappings
```

You could then run the packaged JAR as:

```
java -jar custom-wiremock.jar --load-resources-from-classpath 'wiremock-stuff'
```

Which will load your files and mappings from the packaged JAR.

Note that it is not currently possible to load from the root of the classpath.

### Shutting Down

To shutdown the server, either call `WireMock.shutdownServer()` or post
a request with an empty body to `http://<host>:<port>/__admin/shutdown`.
