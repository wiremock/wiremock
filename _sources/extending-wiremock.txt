.. _extending-wiremock:

******************
Extending WireMock
******************

Transforming Responses
======================

If you want to dynamically alter stub responses sent by WireMock e.g. to render bodies from a template or add generate
additional headers you can do so by adding response transformers via the extensions mechanism.

A response transformer is a single class that extends the ``ResponseTransformer`` abstract class:

.. code-block:: java

    public static class MyTransformer extends ResponseTransformer {

            @Override
            public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files) {
                return ResponseDefinitionBuilder
                        .like(responseDefinition).but()
                        .withBody(responseDefinition.getBody().replace("Hello", "Goodbye")
                        .build();
            }

            @Override
            public String name() {
                return "my-transformer"; // For reference from stub mappings
            }
        }

Response transformers must have a no-args constructor (unless you only intend to register them via an instance,
as described below).


You can register the extension programmatically via its class name, class or an instance:

.. code-block:: java

    new WireMockServer(wireMockConfig().extensions("com.mycorp.BodyContentTransformer", "com.mycorp.HeaderMangler"));

    new WireMockServer(wireMockConfig().extensions(BodyContentTransformer.class, HeaderMangler.class));

    new WireMockServer(wireMockConfig().extensions(new BodyContentTransformer(), new HeaderMangler()));


Non-global transformations
--------------------------

By default transformations will be applied globally. If you only want them to apply in certain cases you can refer to
make them non-global by adding this to your transformer class:

.. code-block:: java

            @Override
            public boolean applyGlobally() {
                return false;
            }

Then you add the transformation to specific stubs via its name:

.. code-block:: java

    wireMock.stubFor(get(urlEqualTo("/local-transform")).willReturn(aResponse()
            .withStatus(200)
            .withBody("Original body")
            .withTransformers("my-transformer", "other-transformer")));

Or:

.. code-block:: javascript

    {
        "request": {
            "method": "GET",
            "url": "/local-transform"
        },
        "response": {
            "status": 200,
            "body": "Original body",
            "transformers": ["my-transformer", "other-transformer"]
        }
    }



See :ref:`running-standalone` for the equivalent command line parameter.