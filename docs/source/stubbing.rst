.. _stubbing:

Stubbing
========

A core feature of WireMock is the ability to return canned HTTP responses for requests matching criteria. These criteria can be
defined in terms of URL, headers and body content.


The basic case
--------------

Java
----

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


JSON
----
To create the stub described above via the JSON API, the following document can either be posted


