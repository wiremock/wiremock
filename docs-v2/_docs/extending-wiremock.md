---
layout: docs
title: Extending WireMock
toc_rank: 110
redirect_from: "/extending-wiremock.html"
description: Extending WireMock via custom code.
---

## Registering Extensions


You can register the extension programmatically via its class name,
class or an instance:

```java
new WireMockServer(wireMockConfig()
  .extensions("com.mycorp.BodyContentTransformer", "com.mycorp.HeaderMangler"));

new WireMockServer(wireMockConfig()
  .extensions(BodyContentTransformer.class, HeaderMangler.class));

new WireMockServer(wireMockConfig()
  .extensions(new BodyContentTransformer(), new HeaderMangler()));
```

See [Running as a Standalone Process](/docs/running-standalone/) for details on running with extensions from the command line.

## Transforming Responses


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

Parameters
----------

Transformer extensions can have parameters passed to them on a per-stub
basis via a `Parameters` object passed to their primary method.
`Parameters` derives from Java's `Map` and can be therefore arbitrarily
deeply nested. Only types compatible with JSON (strings, numbers,
booleans, maps and lists) can be used.

Response definition transformation
----------------------------------

To transform `ResponseDefinition`, extend the
`ResponseDefinitionTransformer` class:

```java
public static class ExampleTransformer extends ResponseDefinitionTransformer {

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            return new ResponseDefinitionBuilder()
                    .withHeader("MyHeader", "Transformed")
                    .withStatus(200)
                    .withBody("Transformed body")
                    .build();
        }

        @Override
        public String name() {
            return "example";
        }
    }
```

Transformer classes must have a no-args constructor unless you only
intend to register them via an instance as described below.

### Supplying parameters


Parameters are supplied on a per stub mapping basis:

```java
stubFor(get(urlEqualTo("/transform")).willReturn(
        aResponse()
                .withTransformerParameter("newValue", 66)
                .withTransformerParameter("inner", ImmutableMap.of("thing", "value")))); // ImmutableMap is from Guava, but any Map will do
```

or:

```json
{
    "request" : {
        "url" : "/transform",
        "method" : "GET"
    },
    "response" : {
        "status" : 200,
        "transformerParameters" : {
            "newValue" : 66,
            "inner" : {
                "thing" : "value"
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

```java
stubFor(get(urlEqualTo("/local-transform")).willReturn(aResponse()
        .withStatus(200)
        .withBody("Original body")
        .withTransformers("my-transformer", "other-transformer")));
```

or:

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


A response transformer extension class is identical to
`ResponseDefinitionTransformer` with the exception that it takes a
`Response` in its transform method's parameter list and returns a
`Response`.

```java
public static class StubResponseTransformerWithParams extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            return Response.Builder.like(response)
                    .but().body(parameters.getString("name") + ", "
                            + parameters.getInt("number") + ", "
                            + parameters.getBoolean("flag"))
                    .build();
        }

        @Override
        public String name() {
            return "stub-transformer-with-params";
        }
}
```

## Custom Request Matchers

If WireMock's standard set of request matching strategies isn't
sufficient, you can register one or more request matcher classes
containing your own logic.

Custom matchers can be attached directly to stubs via the Java API when
using the local admin interface (by calling `stubFor(...)` on
`WireMockServer` or `WireMockRule`). They can also be added via the
extension mechanism and used with individual stubs by referring to them
by name as described above for response transformers.

As with response transformers, per stub mapping parameters can be passed
to matchers.

To add a matcher directly to a stub mapping:

```java
wireMockServer.stubFor(requestMatching(new RequestMatcher() {
    public boolean isMatchedBy(Request request, Parameters parameters) {
        return request.getBody().length > 2048;
    }
}).willReturn(aResponse().withStatus(422)));
```

In Java 8 and above this can be achieved using a lambda:

```java
wireMockServer.stubFor(requestMatching(request ->
    request.getBody().length > 2048;
).willReturn(aResponse().withStatus(422)));
```

To create a matcher to be referred to by name, create a class extending
`RequestMatcher` and register it as an extension as per the instructions
at the top of this page e.g.

```java
public static class BodyLengthMatcher extends RequestMatcher {

        @Override
        public String name() {
            return "body-too-long";
        }

        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            int maxLength = parameters.getInt("maxLength");
            return request.getBody().length > maxLength;
        }
    }
```

Then define a stub with it:

```java
stubFor(requestMatching("body-too-long", Parameters.one("maxLemgth", 2048))
        .willReturn(aResponse().withStatus(422)));
```

or via JSON:

```json
{
    "request" : {
        "customMatcher" : {
            "name" : "body-too-long",
            "parameters" : {
                "maxLemgth" : 2048
            }
        }
    },
    "response" : {
        "status" : 422
    }
}
```

## Listening for requests

If you're using the JUnit rule or you've started `WireMockServer`
programmatically, you can register listeners to be called when a request
is received.

e.g. with the JUnit rule:

```java
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
```
