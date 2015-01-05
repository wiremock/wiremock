.. _java-usage:

**********************
Java (Non-JUnit) Usage
**********************

The Server
==========

If you want to use WireMock from Java (or any other JVM language) outside of JUnit you can programmatically create, start and stop the server:

.. code-block:: java

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8089)); //No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();

    // Sometime later

    wireMockServer.stop();


Like with :ref:`stubbing-and-verification-via-rule` you can call the stubbing/verifying DSL from the server object as
an alternative to calling the client.


The Client
==========

The ``WireMock`` class provides an over-the-wire client to a WireMock server (the local one by default).

Configuring for static calls
----------------------------

To configure the static client for an alternative host and port:

.. code-block:: java

    import static com.github.tomakehurst.wiremock.client.WireMock.*;

    configureFor("wiremock.host", 8089);
    stubFor(get(....));


If you've deployed the server into a servlet container under a path other than root you'll need to set that too:

.. code-block:: java

    WireMock.configureFor("tomcat.host", 8080, "/wiremock");


Newing up
---------

Instances of ``WireMock`` can also be created. This is useful if you need to talk to more than one server instance.

.. code-block:: java

    WireMock wireMock = new WireMock("some.host", 9090, "/wm"); // As above, 3rd param is for non-root servlet deployments
    wireMock.register(get(....)); // Equivalent to stubFor()



