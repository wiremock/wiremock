.. _simulating-faults:

*****************
Simulating Faults
*****************

.. rubric::
    One of the main reasons it's beneficial to use web service fakes when testing is to inject faulty behaviour that
    might be difficult to get the real service to produce on demand. In addition to being able to send back any HTTP
    response code indicating an error, WireMock is able to generate a few other types of problem.


.. _simulating-faults-stub-delays:

Stub fixed delays
=================

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

Stub random delays
==================

In addition to fixed delays, a delay can be sampled from a random distribution. This allows simulation of more specific
downstream latencies, such as a long tail.

Use ``#withRandomDelay`` on the stub to pass in the desired distribution:

.. code-block:: java

    stubFor(get(urlEqualTo("/random/delayed")).willReturn(
            aResponse()
                    .withStatus(200)
                    .withLogNormalRandomDelay(90, 0.1)));

Or set it on the ``delayDistribution`` field via the JSON api:

.. code-block:: javascript

    {
        "request": {
                "method": "GET",
                "url": "/random/delayed"
        },
        "response": {
                "status": 200,
                "delayDistribution": {
                        "type": "lognormal",
                        "median": 80,
                        "sigma": 0.4
                }

        }
    }

Global stub delays
------------------

You can set a random delay globally with ``WireMock.setGlobalRandomDelay()`` or the JSON api at ``http://<host>:<port>/__admin/settings``:

.. code-block:: javascript

    {
        "delayDistribution": {
                "type": "lognormal",
                "median": 90,
                "sigma": 0.1
        }
    }

Available distributions
-----------------------

Lognormal delay
^^^^^^^^^^^^^^^

A lognormal distribution is a pretty good approximation of long tailed latencies centered on the 50th percentile.
It takes two parameters:

* median - The 50th percentile of latencies.
* sigma - Standard deviation. The larger the value, the longer the tail.

`Try different values <https://www.wolframalpha.com/input/?i=lognormaldistribution%28log%2890%29%2C+0.4%29>`_ to
find a good approximation.

To use, instantiate a ``new LogNormal(median, sigma)``, or via JSON:

.. code-block:: javascript

    "delayDistribution": {
            "type": "lognormal",
            "median": 80,
            "sigma": 0.4
    }

Uniform delay
^^^^^^^^^^^^^

A uniform distribution can be used for simulating a stable latency with a fixed amount of jitter. It takes two parameters:

* lower - Lower bound of the range, inclusive.
* upper - Upper bound of the range, inclusive.

For instance, to simulate a stable latency of 20ms +/- 5ms, use lower = 15 and upper = 25.

To use, instantiate a ``new UniformDistribution(15, 25)``, or via JSON:

.. code-block:: javascript

    "delayDistribution": {
            "type": "uniform",
            "lower": 15,
            "upper": 25
    }

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