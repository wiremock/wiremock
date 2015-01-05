.. _proxying:

********
Proxying
********

.. rubric::
    WireMock has the ability to selectively proxy requests through to other hosts. This supports a proxy/intercept setup
    where requests are by default proxied to another (possibly real, live) service, but where specific stubs are configured these
    are returned in place of the remote serivce's response. Responses that the live service can't be forced to generate
    on demand can thus be injected for testing. Proxying also supports :ref:`record-playback`.

.. _proxying-proxy-stub-mappings:

Proxy stub mappings
===================

Proxy responses are defined in exactly the same manner as stubs, meaning that the same request matching criteria can be
used.

The following code will proxy all GET requests made to ``http://<host>:<port>/other/service/.*`` to
``http://otherservice.com/approot``, e.g. when running WireMock locally a request to
``http://localhost:8080/other/service/doc/123`` would be forwarded to
``http://otherservice.com/approot/other/service/doc/123``.

.. code-block:: java

    stubFor(get(urlMatching("/other/service/.*"))
            .willReturn(aResponse().proxiedFrom("http://otherhost.com/approot")));

The JSON equivalent would be:

.. code-block:: javascript

    {
        "request": {
            "method": "GET",
            "urlPattern": "/other/service/.*"
        },
        "response": {
            "proxyBaseUrl" : "http://otherhost.com/approot"
        }
    }


.. _proxying-proxy-intercept:

Proxy/intercept
===============

The proxy/intercept pattern described above is achieved by adding a low priority proxy mapping with a broad URL match
and any number of higher priority stub mappings e.g.

.. code-block:: java

    // Low priority catch-all proxies to otherhost.com by default
    stubFor(get(urlMatching(".*")).atPriority(10)
            .willReturn(aResponse().proxiedFrom("http://otherhost.com")));


    // High priority stub will send a Service Unavailable response
    // if the specified URL is requested
    stubFor(get(urlEqualTo("/api/override/123")).atPriority(1)
            .willReturn(aResponse().withStatus(503)));            
            
Additional headers
==================

It is possible to configure the proxy to add headers before forwarding the request to the destination:

.. code-block:: java

    // Inject user agent to trigger rendering of mobile version of website
    stubFor(get(urlMatching(".*"))
            .willReturn(aResponse()
            	.proxiedFrom("http://otherhost.com")
            	.withAdditionalRequestHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone)"));

or

.. code-block:: javascript

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

You can also add response headers via the same method as for non-proxy responses (see :ref:`stubbing`).


Standalone shortcut
-------------------

It is possible to start the standalone running with the catch-all stub already configured:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar --proxy-all="http://someotherhost.com"

Then it's simply a case of adding your stub mapping ``.json`` files under ``mappings`` as usual (see :ref:`stubbing`).


.. _browser-proxying:

Running as a browser proxy
==========================

WireMock can be made to work as a browser proxy. This supports a website based variant of the proxy/intercept pattern
described above, allowing you to modify specific AJAX requests or swap out CSS/Javascript files.

This currently only works in standalone mode:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar --enable-browser-proxying


Proxying via another proxy server
=================================

If you're inside a network that only permits HTTP traffic out to the internet via an opaque proxy you might wish to
set up proxy mappings that route via this server. This can be configured programmatically by passing a configuration
object to the constructor of ``WireMockServer`` or the JUnit rules like this:

.. code-block:: java

    import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
    ...

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().proxyVia("proxy.mycorp.com", 8080);


.. _proxy-client-certs:

Proxying to a target server that requires client certificate authentication
===========================================================================

WireMock's proxy client will send a client certificate if the target service requires it and a trust store containing
the certificate is configured:

.. code-block:: java

     @Rule
     public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
         .trustStorePath("/path/to/truststore.jks")
         .trustStorePassword("mostsecret")); // Defaults to "password" if omitted

See :ref:`running-standalone` for command line equivalent.