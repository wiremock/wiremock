.. _junit-rule:

******************
The JUnit 4.x Rule
******************

The JUnit rule provides a convenient way to include WireMock in your test cases. It handles the lifecycle for you, starting
the server before each test method and stopping afterwards.


Basic usage
===========

To make WireMock available to your tests on its default port (8080):

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();


Server configuration
====================

The rule's constructor can take an ``Options`` instance to override various settings. An ``Options`` implementation can
be created via the ``WireMockConfiguration.wireMockConfig()`` builder:

.. code-block:: java

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8888).httpsPort(8889));



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


.. _stubbing-and-verification-via-rule:

Accessing the stubbing and verification DSL from the rule
=========================================================

In addition the the static methods on the ``WireMock`` class, it is also possilble to configure stubs etc. via the rule
object directly. There are two advantages to this - 1) it's a bit faster as it avoids sending commands over HTTP, and
2) if you want to mock multiple services you can declare a rule per service but not have to create a client object for each e.g.

.. code-block:: java

    @Rule
    public WireMockRule service1 = new WireMockRule(8081);

    @Rule
    public WireMockRule service2 = new WireMockRule(8082);

    @Test
    public void bothServicesDoStuff() {
        service1.stubFor(get(urlEqualTo("/blah")).....);
        service2.stubFor(post(urlEqualTo("/blap")).....);

        ...
    }
