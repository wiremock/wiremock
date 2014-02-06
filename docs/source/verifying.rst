.. _verifying:

*********
Verifying
*********

.. rubric::
    The WireMock server records all requests it receives in memory (at least until it is :ref:`stubbing-reset`). This makes it possible
    to verify that a request matching a specific pattern was received, and also to fetch the requests' details.


.. _verifying-checking-for-matching-requests:

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


Via JSON + HTTP
---------------

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


Matching on header absence
--------------------------

When verifying (unlike stubbing) it is possible to specify that a particular header is not present:

.. code-block:: java

    verify(putRequestedFor(urlEqualTo("/without/header")).withoutHeader("Content-Type"));

Which is equivalent to:

.. code-block:: javascript

    {
        "url" : "/without/header",
        "method" : "PUT",
        "headers" : {
            "Content-Type" : {
                "absent" : true
            }
        }
    }

.. _verifying-querying-request-details:

Querying request details
========================

It is also possible to retrieve the details of recorded requests. In Java this is done via a call to ``findAll()``:

.. code-block:: java

    List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching("/api/.*")));


And in JSON + HTTP by posting a criteria document (of the same form as for request counting) to
``http://<host>:<port>/__admin/requests/find``, which will return a response like this:

.. code-block:: javascript

    {
      "requests": [
        {
          "url": "/my/url",
          "absoluteUrl": "http://mydomain.com/my/url",
          "method": "GET",
          "headers": {
            "Accept-Language": "en-us,en;q=0.5",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0) Gecko/20100101 Firefox/9.0",
            "Accept": "image/png,image/*;q=0.8,*/*;q=0.5"
          },
          "body": "",
          "browserProxyRequest": true,
          "loggedDate": 1339083581823,
          "loggedDateString": "2012-06-07 16:39:41"
        },
        {
          "url": "/my/other/url",
          "absoluteUrl": "http://my.other.domain.com/my/other/url",
          "method": "POST",
          "headers": {
            "Accept": "text/plain",
            "Content-Type": "text/plain"
          },
          "body": "My text",
          "browserProxyRequest": false,
          "loggedDate": 1339083581823,
          "loggedDateString": "2012-06-07 16:39:41"
        }
      ]
    }


Listening for requests
======================

If you're using the JUnit rule or you've started ``WireMockServer`` programmatically, you can register listeners to be
called when a request is received.

e.g. with the JUnit rule:

.. code-block:: java

    List<Request> requests = new ArrayList<Request>();
    rule.addMockServiceRequestListener(new RequestListener() {
         @Override
         public void requestReceived(Request request, Response response) {
             requests.add(LoggedRequest.createFrom(request));
         }
    });

    for (Request request: requests) {
        assertThat(request.getUrl(), containsString("docId=92837592847"));
    }

