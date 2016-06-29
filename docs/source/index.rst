WireMock
========

WireMock is a flexible library for stubbing and mocking web services. Unlike general purpose mocking tools it works by
creating an actual HTTP server that your code under test can connect to as it would a real web service.

It supports HTTP response stubbing, request verification, proxy/intercept, record/playback of stubs and fault injection,
and can be used from within a unit test or deployed into a test environment.

Although it's written in Java, there's also a JSON API so you can use it with pretty much any language out there.


News - WireMock 2.0 RC
----------------------
A release candidate is now available for WireMock 2.0.

This also means that changes are now taking place on the master branch again, so please direct your PRs to master rather than the 2.0-beta branch.

Here are the major changes made between 1.x and 2.x:

* Total re-write of the matching code to introduce the idea of "distance", which is a 0-1 normalised value representing distance between and request pattern and a request. This supports a couple of new features - a "near misses" API supporting querying for stubs that almost matched a request or requests that almost matched a pattern, and much better reporting of test failures by the @Rule, including IntelliJ style diffs.
* Drop support for JDK 6.
* Upgrade to Jetty 9.2, which is higher performing, fixes some bugs and paves the way for HTTP/2 (which unfortunately requires JDK 7 to be dropped).
* Upgrade to JSONPath 2.0.0.
* Upgrade to XMLUnit 2.0 internally
* Improving the extensions API, with support for transforming ``Response`` s directly in addition to ``ResponseDefinition``, writing custom matchers and parameterising extensions.
* Adding some additional type safety to the Java DSL.
* Putting the standalone JAR under its own Maven artifact so it can have it's own POM (avoiding the need to exlclude all large swathes of dependencies).
* Matching on cookies and pre-emptive basic auth
* GZip support (request and response body)
* Edit and remove stub mappings
* Better Android support - can now be built and run on Android without modification. See `Sam Edwards' blog post <http://handstandsam.com/2016/01/30/running-wiremock-on-android/>`_ for details.

Also, watch this space for a brandspanking new website.

The `1.x docs are preserved here <http://one.wiremock.org>`_.


What's it for?
--------------

Some scenarios you might want to consider WireMock for:

* Testing mobile apps that depend on third-party REST APIs
* Creating quick prototypes of your APIs
* Injecting otherwise hard-to-create errors in 3rd party services
* Any unit testing of code that depends on a web service


Who makes it?
-------------
WireMock was created and is maintained by `Tom Akehurst <http://www.tomakehurst.com/about>`_.

The following people have been kind enough to submit improvements:

* `Tim Perry <https://github.com/pimterry>`_
* `Dominic Tootell <https://github.com/tootedom>`_
* `mangotang <https://github.com/mangotang>`_
* `Rob Elliot <https://github.com/mahoney>`_
* `Neil Green <https://github.com/neilg>`_
* `Rowan Hill <https://github.com/rowanhill>`_
* `Christian Trimble <https://github.com/ctrimble>`_
* `Aman King <https://github.com/amanking>`_
* `Oliver Schönherr <https://github.com/oschoen>`_
* `Jay Goldberg <https://github.com/carthoris>`_
* `Matt Nathan <https://github.com/mattnathan>`_



Why shouldn't I just use my favourite mocking library?
------------------------------------------------------

Mocking HTTP client classes in a way that adequately reflects their real behaviour is pretty hard. Creating real HTTP
exchanges alleviates this by allowing you to use your production HTTP client implementation in your tests.

Object based mocking isn't really suitable for acceptance/functional testing scenarios.
WireMock can be run as a standalone service or deployed into a servlet container to enable it to be installed into your dev/test
environments.


I like the idea, but the implementation stinks/you've missed something I need/it's the wrong colour
---------------------------------------------------------------------------------------------------

Here are some alternative JVM based libraries with similar goals:

* `Betamax <http://freeside.co/betamax/>`_
* `REST-driver <https://github.com/rest-driver/rest-driver>`_
* `MockServer <http://www.mock-server.com/>`_
* `Moco <https://github.com/dreamhead/moco>`_


I couldn't possibly be seen using Java, I've got my image to think about!
-------------------------------------------------------------------------

Luckily, `Rowan Hill <https://github.com/rowanhill>`_ has built a `PHP binding <https://github.com/rowanhill/wiremock-php>`_,
so you can bring it to your next Shoreditch hackathon without fear of ridicule!


Contents
--------

.. toctree::
   :maxdepth: 1

   getting-started
   running-standalone
   https
   junit-rule
   java-usage
   stubbing
   verifying
   proxying
   record-playback
   stateful-behaviour
   simulating-faults
   extending-wiremock





