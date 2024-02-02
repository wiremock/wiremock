---
description: Transforming response definitions and responses via extensions
---

# Transforming response definitions and responses

Sometimes, returning wholly static responses to stub requests isn't
practical e.g. when there are transaction IDs being passed between
request/responses, dates that must be current. Via WireMock's extension
mechanism it is possible to dynamically modify responses, allowing
header re-writing and templated responses amongst other things.

There are two ways to dynamically transform output from WireMock.
WireMock stub mappings consist in part of a `ResponseDefinition`. This
is essentially a description that WireMock (sometimes) combines with
other information when producing the final response. A basic
`ResponseDefinition` closely resembles an actual response in that it has
status, headers and body fields, but it can also indicate to WireMock
that the actual response should be the result of a proxy request to
another system or a fault of some kind.

`ResponseDefinition` objects are "rendered" by WireMock into a
`Response`, and it is possible to interject either before or after this
process when writing an extension, meaning you can either transform the
`ResponseDefinition` prior to rendering, or the rendered `Response`
afterwards.

## Parameters

Transformer extensions can have parameters passed to them on a per-stub
basis via a `Parameters` fetched by calling `serveEvent.getTransformerParameters()`.
`Parameters` derives from Java's `Map` and can be therefore arbitrarily
deeply nested. Only types compatible with JSON (strings, numbers,
booleans, maps and lists) can be used.

## Response definition transformation

To transform `ResponseDefinition` implement the `ResponseDefinitionTransformerV2` interface:

```java
public static class ExampleTransformer implements ResponseDefinitionTransformerV2 {

        @Override
        public ResponseDefinition transform(ServeEvent serveEvent) {
            return new ResponseDefinitionBuilder()
                    .withHeader("MyHeader", "Transformed")
                    .withStatus(200)
                    .withBody("Transformed body")
                    .build();
        }

        @Override
        public String getName() {
            return "example";
        }
    }
```

Transformer classes must have a no-args constructor unless you only
intend to register them via an instance as described below.

### Supplying parameters

Parameters are supplied on a per stub mapping basis:

=== "Java"

    ```java
    stubFor(get(urlEqualTo("/transform")).willReturn(
            aResponse()
                    .withTransformerParameter("newValue", 66)
                    .withTransformerParameter("inner", ImmutableMap.of("thing", "value")))); // ImmutableMap is from Guava, but any Map will do
    ```

=== "JSON"

    ```json
    {
        "request": {
            "url": "/transform",
            "method": "GET"
        },
        "response": {
            "status": 200,
            "transformerParameters": {
                "newValue": 66,
                "inner": {
                    "thing": "value"
                }
            }
        }
    }
    ```

### Non-global transformations

By default transformations will be applied globally. If you only want
them to apply in certain cases you can refer to make them non-global by
adding this to your transformer class:

```java
@Override
public boolean applyGlobally() {
    return false;
}
```

Then you add the transformation to specific stubs via its name:

=== "Java"

    ```java
    stubFor(get(urlEqualTo("/local-transform")).willReturn(aResponse()
            .withStatus(200)
            .withBody("Original body")
            .withTransformers("my-transformer", "other-transformer")));
    ```

=== "JSON"

    ```json
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
    ```

The Java API also has a convenience method for adding transformers and
parameters in one call:

```java
stubFor(get(urlEqualTo("/transform")).willReturn(
        aResponse()
                .withTransformer("body-transformer", "newValue", 66)));
```

### Response transformation

A response transformer extension class is identical to `ResponseDefinitionTransformerV2` with the exception that it takes a
`Response` in its transform method's parameter list and returns a `Response`.

This transformer is the best option if you want to transform the response from a proxy call.

```java
public static class StubResponseTransformerWithParams implements ResponseTransformerV2 {

        @Override
        public Response transform(Response response, ServeEvent serveEvent) {
            Parameters parameters = serveEvent.getTransformerParameters();
            return Response.Builder.like(response)
                    .but().body(parameters.getString("name") + ", "
                            + parameters.getInt("number") + ", "
                            + parameters.getBoolean("flag"))
                    .build();
        }

        @Override
        public String getName() {
            return "stub-transformer-with-params";
        }
}
```
