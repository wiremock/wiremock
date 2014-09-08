.. _getting-started:

***************
Getting Started
***************

Maven
=====
To add WireMock to your Java project, put the following in the dependencies section of your POM:

.. code-block:: xml

    <dependency>
        <groupId>com.github.tomakehurst</groupId>
        <artifactId>wiremock</artifactId>
        <version>1.48</version>

        <!-- Include this if you have dependency conflicts for Guava, Jetty, Jackson or Apache HTTP Client -->
        <classifier>standalone</classifier>
    </dependency>


JUnit 4.x
=========
To use WireMock's fluent API add the following import:

.. code-block:: java

    import static com.github.tomakehurst.wiremock.client.WireMock.*;

WireMock ships with some JUnit rules to manage the server's lifecycle and setup/tear-down tasks. To start and stop WireMock per-test case, add the following to your test class (or a superclass of it):

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080


Now you're ready to write a test case like this:

.. code-block:: java

    @Test
    public void exampleTest() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("text/xml"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBody("<response>Some content</response>")));

        Result result = myHttpServiceCallingObject.doSomething();

        assertTrue(result.wasSuccessFul());

        verify(postRequestedFor(urlMatching("/my/resource/[a-z0-9]+"))
                .withRequestBody(matching(".*<message>1234</message>.*"))
                .withHeader("Content-Type", notMatching("application/json")));
    }

For many more examples of JUnit tests look no further than `WireMock's own acceptance tests <https://github.com/tomakehurst/wiremock/tree/master/src/test/java/com/github/tomakehurst/wiremock>`_

For more details on verifying requests and stubbing responses, see :ref:`stubbing` and :ref:`verifying`

Other @Rule configurations
==========================

With a bit more effort you can make the WireMock server continue to run between test cases.
This is easiest in JUnit 4.10:

.. code-block:: java

    @ClassRule
    @Rule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);


Unfortunately JUnit 4.11 prohibits ``@Rule`` on static members so a slightly more verbose form is required:

.. code-block:: java

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;


And if you're still using JUnit 4.8:

.. code-block:: java

    @Rule
    public static WireMockStaticRule wireMockRule = new WireMockStaticRule(8089);

    @AfterClass
    public static void stopWireMock() {
        wireMockRule.stopServer();
    }

.. note::
    ``WireMockStaticRule`` is deprecated as the above usage isn't permitted from JUnit 4.11 onwards


Detailed configuration
======================

For a bit more control over the settings of the WireMock server created by the rule you can pass a fluently built
Options object to either (non-deprecated) rule's constructor:

.. code-block:: java

    import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
    ...

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089).httpsPort(8443));



Non-JUnit and general Java usage
================================

If you're not using JUnit or neither of the WireMock rules manage its lifecycle in a suitable way you can construct and start the
server directly:

.. code-block:: java

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8089)); //No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();

    // Do some stuff

    WireMock.reset();

    // Finish doing stuff

    wireMockServer.stop();

If you've changed the port number and/or you're running the server on another host, you'll need to tell the client:

.. code-block:: java

    WireMock.configureFor("wiremock.host", 8089);

And if you've deployed it into a servlet container under a path other than root you'll need to set that too:

.. code-block:: java

    WireMock.configureFor("tomcat.host", 8080, "/wiremock");

Running standalone
==================

The WireMock server can be run in its own process, and configured via the Java API, JSON over HTTP or JSON files.

This will start the server on port 8080:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar

You can `download the standalone JAR from here <http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.48/wiremock-1.48-standalone.jar>`_.

Supported command line options are:

``--port``:
Set the HTTP port number e.g. ``--port 9999``

``--https-port``:
If specified, enables HTTPS on the supplied port.

``--https-keystore``:
Path to a keystore file containing an SSL certificate to use with HTTPS. The keystore must have a password of "password".
This option will only work if ``--https-port`` is specified. If this option isn't used WireMock will default to its
own self-signed certificate.

``--verbose``:
Turn on verbose logging to stdout

``--root-dir``:
Sets the root directory, under which ``mappings`` and ``__files`` reside. This defaults to the current directory.

``--record-mappings``:
Record incoming requests as stub mappings. See :ref:`record-playback`.

``--match-headers``:
When in record mode, capture request headers with the keys specified. See :ref:`record-playback`.

``--proxy-all``:
Proxy all requests through to another base URL e.g. ``--proxy-all="http://api.someservice.com"``
Typically used in conjunction with ``--record-mappings`` such that a session on another service can be recorded.

``--preserve-host-header``: When in proxy mode, it passes the Host header as it comes from the client through to the
proxied service. When this option is not present, the Host header value is deducted from the proxy URL. This option is
only available if the ``--proxy-all`` option is specified.

``--proxy-via``:
When proxying requests (either by using --proxy-all or by creating stub mappings that proxy to other hosts), route via
another proxy server (useful when inside a corporate network that only permits internet access via an opaque proxy).
e.g.
``--proxy-via webproxy.mycorp.com`` (defaults to port 80)
or
``--proxy-via webproxy.mycorp.com:8080``

``--enable-browser-proxying``:
Run as a browser proxy. See :ref:`browser-proxying`.

``--no-request-journal``:
Disable the request journal, which records incoming requests for later verification. This allows WireMock to be run
(and serve stubs) for long periods (without resetting) without exhausting the heap. The ``--record-mappings`` option isn't
available if this one is specified.

``--help``:
Show command line help


File serving
------------

When running standalone files placed under the ``__files`` directory will be served up as if from under the docroot,
except if stub mapping matching the URL exists. For example if a file exists ``__files/things/myfile.html`` and
no stub mapping will match ``/things/myfile.html`` then hitting ``http://<host>:<port>/things/myfile.html`` will
serve the file.



Configuring via JSON
--------------------

Once the server has started you can give it a spin by setting up a stub mapping via the JSON API:

.. code-block:: console

    $ curl -X POST --data '{ "request": { "url": "/get/this", "method": "GET" }, "response": { "status": 200, "body": "Here it is!\n" }}' http://localhost:8080/__admin/mappings/new

Then fetching it back:

.. code-block:: console

    $ curl http://localhost:8080/get/this
    Here it is!


You can also use the JSON API via files. When the WireMock server starts it creates two directories under the current one:
``mappings`` and ``__files``.

To create a stub like the one above by this method, drop a file with a ``.json`` extension under ``mappings``
with the following content:

.. code-block:: javascript

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

After restarting the server you should be able to do this:

.. code-block:: console

    $ curl http://localhost:8080/api/mytest
    More content


See :ref:`stubbing` and :ref:`verifying` for more on the JSON API.

Fetching all of your stub mappings (and checking WireMock is working)
---------------------------------------------------------------------
A GET request to the root admin URL e.g ``http://localhost:8080/__admin`` will return all currently registered stub mappings. This is a useful way to check
whether WireMock is running on the host and port you expect:



Deploying into a servlet container
==================================

WireMock can be packaged up as a WAR and deployed into a servlet container, with some caveats:
fault injection and browser proxying won't work, __files won't be treated as a docroot as with standalone,
the server cannot be remotely shutdown, and the container must be configured to explode the WAR on deployment.
This has only really been tested in Tomcat 6 and Jetty, so YMMV. Running standalone is definitely the preferred option.

The easiest way to create a WireMock WAR project is to clone the `sample app <https://github.com/tomakehurst/wiremock/tree/master/sample-war>`_

Deploying under a sub-path of the context root
----------------------------------------------
If you want WireMock's servlet to have a non-root path, the additional init param ``mappedUnder`` must be set with the sub-path
web.xml (in addition to configuring the servlet mapping appropriately).

See `the custom mapped WAR example <https://github.com/tomakehurst/wiremock/blob/master/sample-war/src/main/webappCustomMapping/WEB-INF/web.xml>`_ for details.

