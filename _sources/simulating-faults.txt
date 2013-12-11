.. _simulating-faults:

*****************
Simulating Faults
*****************

.. rubric::
    One of the main reasons it's beneficial to use web service fakes when testing is to inject faulty behaviour that
    might be difficult to get the real service to produce on demand. In addition to being able to send back any HTTP
    response code indicating an error, WireMock is able to generate a few other types of problem.


.. _simulating-faults-stub-delays:

Stub delays
===========

A stub response can have a fixed delay attached to it, such that the response will not be returned until after the
specified number of milliseconds:

.. code-block:: java

    stubFor(get(urlEqualTo("/delayed")).willReturn(
            aResponse()
                    .withStatus(200)
                    .withFixedDelay(2000)));


Or

.. code-block:: javascript

    {
        "request": {
            "method": "GET",
            "url": "/delayed"
        },
        "response": {
            "status": 200,
            "fixedDelayMilliseconds": 2000
        }
    }

Global stub delays
------------------

A fixed delay can be added to all stubs either by calling ``WireMock.setGlobalFixedDelay()`` or posting a JSON
document of the following form to ``http://<host>:<port>/__admin/settings``:

.. code-block:: javascript

    {
        "fixedDelay": 500
    }


.. _simulating-faults-request-delays:

Request delays (and socket timeouts)
====================================

Adding stub delays by either of the above routes won't allow you to create the conditions for a socket timeout.
This is because data must be sent and received on the socket for WireMock to determine enough about the request (URL,
headers etc.) to select an appropriate stub. To reliably create a socket timeout, or test client behaviour when there
is latency in request handling it is possible to set a delay for all requests that occurs before any processing:

.. code-block:: java

    addRequestProcessingDelay(300); // Milliseconds

Or post the following to ``http://<host>:<port>/__admin/socket-delay``:

.. code-block:: javascript

    { "milliseconds": 300 }

Resetting WireMock removes this delay.


.. _simulating-faults-bad-responses:

Bad responses
=============

It is also possible to create several kinds of corrupted responses:

.. code-block:: java

    stubFor(get(urlEqualTo("/fault"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));


The ``Fault`` enum has the following options:

``EMPTY_RESPONSE``:
Return a completely empty response.

``MALFORMED_RESPONSE_CHUNK``:
Send an OK status header, then garbage, then close the connection.

``RANDOM_DATA_THEN_CLOSE``:
Send garbage then close the connection.


In JSON (fault values are the same as the ones listed above):

.. code-block:: javascript

    {
        "request": {
            "method": "GET",
            "url": "/fault"
        },
        "response": {
            "fault": "MALFORMED_RESPONSE_CHUNK"
        }
    }