.. _stubbing:

********
Stubbing
********

Basic stubbing
==============

A core feature of WireMock is the ability to return canned HTTP responses for requests matching criteria. These criteria can be
defined in terms of URL, headers and body content.


The following code will configure a response with a status of 200 to be returned when the relative URL exactly
matches ``/some/thing`` (including query parameters). The body of the response will be "Hello world!" and a
``Content-Type`` header will be sent with a value of ``text-plain``.

.. code-block:: java

    @Test
    public void exactUrlOnly() {
        stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Hello world!")));

        assertThat(testClient.get("/some/thing").statusCode(), is(200));
        assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
    }


To create the stub described above via the JSON API, the following document can either be posted to
``http://<host>:<port>/__admin/mappings/new`` or placed in a file with a ``.json`` extension under the
``mappings`` directory:

.. code-block:: javascript

    {
    	"request": {
    		"method": "GET",
    		"url": "/some/thing"
    	},
    	"response": {
    		"status": 200,
    		"body": "Hello world!",
    		"headers": {
    			"Content-Type": "text/plain"
    		}
    	}
    }

HTTP methods currently supported are: ``GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS``. You can specify ``ANY`` if you
want the stub mapping to match on any request method.


URL matching
============

URLs can be matched exactly (as in the example above) or via a regular expression. In Java this is done with the ``urlMatching()``
function:

.. code-block:: java

    stubFor(put(urlMatching("/thing/matching/[0-9]+"))
        .willReturn(aResponse().withStatus(200)));


And in JSON via the ``urlPattern`` attribute:

.. code-block:: javascript

    {
        "request": {
            "method": "PUT",
            "urlPattern": "/thing/matching/[0-9]+"
        },
        "response": {
            "status": 200
        }
    }


Request header matching
=======================

To match stubs according to request headers:

.. code-block:: java

    stubFor(post(urlEqualTo("/with/headers"))
        .withHeader("Content-Type", equalTo("text/xml"))
        .withHeader("Accept", matching("text/.*"))
        .withHeader("etag", notMatching("abcd.*"))
        .withHeader("etag", containing("2134"))
            .willReturn(aResponse().withStatus(200)));

Or

.. code-block:: javascript

    {
    	"request": {
            "method": "POST",
            "url": "/with/headers",
            "headers": {
                "Content-Type": {
                    "equalTo": "text/xml"
                },
                "Accept": {
                    "matches": "text/.*"
                },
                "etag": {
                    "doesNotMatch": "abcd.*"
                },
                "etag": {
                    "contains": "2134"
                }
            }
    	},
    	"response": {
    		"status": 200
    	}
    }



Request body matching
=====================

For PUT and POST requests the contents of the request body can be used to match stubs:

.. code-block:: java

    stubFor(post(urlEqualTo("/with/body"))
        .withRequestBody(matching("<status>OK</status>"))
        .withRequestBody(notMatching(".*ERROR.*"))
            .willReturn(aResponse().withStatus(200)));

Or

.. code-block:: javascript

    {
    	"request": {
            "method": "POST",
            "url": "/with/body",
            "bodyPatterns": [
                { "matches": "<status>OK</status>" },
                { "doesNotMatch": ".*ERROR.*" }
            ]
    	},
    	"response": {
    		"status": 200
    	}
    }


Stub priority
=============


