.. _https:

*****
HTTPS
*****

Handling HTTPS requests
=======================

To enable HTTPS using WireMock's self-signed certificate just specify an HTTPS port:

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443));


To use your own keystore you can specify its path and optionally its password:

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
        .httpsPort(8443)
        .keystorePath("/path/to/keystore.jks")
        .keystorePassword("verysecret")); // Defaults to "password" if omitted


Requiring client certificates
=============================

To make WireMock require clients to authenticate via a certificate you need to supply a trust store containing the certs
to trust and enable client auth:

.. code-block:: java

     @Rule
     public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
         .httpsPort(8443)
         .needClientAuth(true)
         .trustStorePath("/path/to/truststore.jks")
         .trustStorePassword("mostsecret")); // Defaults to "password" if omitted


See :ref:`running-standalone` for command line equivalents.