WireMock
========

WireMock is a flexible library for stubbing and mocking web services. Unlike general purpose mocking tools it works by
creating an actual HTTP server that your code under test can connect to as it would a real web service.

It supports HTTP response stubbing, request verification, proxy/intercept, record/playback of stubs and fault injection,
and can be used from within a unit test or deployed into a test environment.

Although it's written in Java, there's also a JSON API so you can use it with pretty much any language out there.

What's it for?
--------------

Some scenarios you might want to consider WireMock for:

* Testing mobile apps that depend on third-party REST APIs
* Creating quick prototypes of your APIs
* Injecting otherwise hard-to-create errors in 3rd party services
* Any unit testing of code that depends on a web service


Why shouldn't I just use my favourite mocking library?
------------------------------------------------------

Mocking HTTP client classes in a way that adequately reflects their real behaviour is pretty hard. Creating real HTTP
exchanges alleviates this.

WireMock can be run as a standalone service or deployed into a servlet container to enable it to be installed into your dev/test
environments.



Contents
--------

.. toctree::
   :maxdepth: 1

   getting-started
   stubbing-and-verifying
   proxying
   record-playback
   stateful-behaviour
   simulating-faults





