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
        <version>1.25</version>

        <!-- Include this if you have dependency conflicts for Guava, Jetty, Jackson or Apache HTTP Client -->
        <classifier>standalone</classifier>
    </dependency>


JUnit 4.x
=========
To use WireMock's fluent API add the following import:

.. code-block:: java

    import static com.github.tomakehurst.wiremock.client.WireMock.*;

WireMock ships with two JUnit rules to manage the server's lifecycle and setup/tear-down tasks. To start and stop WireMock per-test case, add the following to your test class (or a superclass of it):

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080


Alternatively, if you want the server to continue to run between test cases:

.. code-block:: java

    @Rule
    public static WireMockStaticRule wireMockRule = new WireMockStaticRule(8089);

    @AfterClass
    public static void stopWireMock() {
        wireMockRule.stopServer();
    }

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

Running standalone
==================

The WireMock server can be run in its own process, and configured via the Java API, JSON over HTTP or JSON files.

This will start the server on port 8080:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar

Supported command line options are:

``--port``:
Set the port number e.g. ``--port 9999``

``--verbose``:
Turn on verbose logging to stdout

``--record-mappings``:
Record incoming requests as stub mappings. See :ref:`record-playback`.

``--proxy-all``:
Proxy all requests through to another base URL e.g. ``--proxy-all="http://api.someservice.com"``
Typically used in conjunction with ``--record-mappings`` such that a session on another service can be recorded.

``--enable-browser-proxying``:
Run as a browser proxy. See :ref:`browser-proxying`.

``--help``:
Show command line help


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


Deploying into a servlet container
==================================

WireMock can be packaged up as a WAR and deployed into a servlet container, with some caveats:
fault injection and browser proxying won't work, __files won't be treated as a docroot as with standalone, and the container must be configured to explode the WAR on deployment.
This has only really been tested in Tomcat 6 and Jetty, so YMMV. Running standalone is definitely the preferred option.

The easiest way to create a WireMock WAR project is to clone the `sample app <https://github.com/tomakehurst/wiremock/tree/master/sample-war>`_





