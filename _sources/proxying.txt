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

