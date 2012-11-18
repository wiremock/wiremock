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


Running standalone
==================

WireMock can be run completely in its own process like this:

.. code-block:: bash

    java -jar wiremock-1.25-standalone.jar


Deploying into a servlet container
==================================





For more details on verifying requests and stubbing responses, see :ref:`stubbing` and :ref:`verifying`