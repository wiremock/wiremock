.. _verifying:

*********
Verifying
*********

.. rubric::
    The WireMock server records all requests it receives in memory (at least until it is reset). This makes it possible
    to verify that a request matching a specific pattern was received, and also to fetch the requests' details.


.. _verifying-checking-for-matching-requests

Checking for matching requests
==============================


In Java
-------

To verify that a request matching some criteria was received by WireMock at least once:

.. code-block:: java

    verify(postRequestedFor(urlEqualTo("/verify/this"))
            .withHeader("Content-Type", equalTo("text/xml")));

The criteria part in the parameter to ``postRequestedFor()`` uses the same builder as for stubbing, so all of the same
predicates are available. See :ref:`stubbing` for more details.

To check for a precise number of requests matching the criteria, use this form:

.. code-block:: java

    verify(3, postRequestedFor(urlEqualTo("/three/times")));


Via JSON over HTTP
------------------

There isn't a direct JSON equivalent to the above Java API. However, it's possible to achieve the same effect by requesting
a count of the number of requests matching the specified criteria (and in fact this is what the Java method does under the
hood).

This can be done by posting a JSON document containing the criteria to ``http://<host>:<port>/__admin/requests/count``:

.. code-block:: javascript

    {
        "method": "POST",
        "url": "/resource/to/count",
        "headers": {
            "Content-Type": {
                "matches": ".*/xml"
            }
        }
    }

A response of this form will be returned:

.. code-block:: javascript

    { "count": 4 }


Querying requests
=================


