---
description: Adding custom request matchers via extensions
---

# Adding custom request matchers

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
wireMockServer.stubFor(requestMatching(new RequestMatcherExtension() {
    @Override
    public MatchResult match(Request request, Parameters parameters) {
        return MatchResult.of(request.getBody().length > 2048);
    }
}).willReturn(aResponse().withStatus(422)));
```

To use it in a verification :

```java
verify(2, requestMadeFor(new ValueMatcher<Request>() {
    @Override
    public MatchResult match(Request request) {
        return MatchResult.of(request.getBody().length > 2048);
    }
}));
```

In Java 8 and above this can be achieved using a lambda:

```java
wireMockServer.stubFor(requestMatching(request ->
    MatchResult.of(request.getBody().length > 2048)
).willReturn(aResponse().withStatus(422)));
```

To create a matcher to be referred to by name, create a class extending
`RequestMatcher` and register it as an extension as per the instructions
at the top of this page e.g.

```java
public class BodyLengthMatcher extends RequestMatcherExtension {

    @Override
    public String getName() {
        return "body-too-long";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        int maxLength = parameters.getInt("maxLength");
        return MatchResult.of(request.getBody().length > maxLength);
    }
}
```

Then define a stub with it:

=== "Java"

    ```java
    stubFor(requestMatching("body-too-long", Parameters.one("maxLength", 2048))
            .willReturn(aResponse().withStatus(422)));
    ```

=== "JSON"

    ```json
    {
        "request": {
            "customMatcher": {
                "name": "body-too-long",
                "parameters": {
                    "maxLength": 2048
                }
            }
        },
        "response": {
            "status": 422
        }
    }
    ```

### Combining standard and custom request matchers

An inline custom matcher can be used in combination with standard matchers in the following way:

```java
stubFor(get(urlPathMatching("/the/.*/one"))
        .andMatching(new MyRequestMatcher()) // Will also accept a Java 8+ lambda
        .willReturn(ok()));
```

Note that inline matchers of this form can only be used from Java, and only when `stubFor` is being called against a local
WireMock server. An exception will be thrown if attempting to use an inline custom matcher against a remote instance.

Custom matchers defined as extensions can also be combined with standard matchers.

=== "Java"

```java
stubFor(get(urlPathMatching("/the/.*/one"))
        .andMatching("path-contains-param", Parameters.one("path", "correct"))
        .willReturn(ok()));
```

=== "JSON"

    ```json
    {
        "request": {
            "urlPathPattern": "/the/.*/one",
            "method": "GET",
            "customMatcher": {
                "name": "path-contains-param",
                "parameters": {
                    "path": "correct"
                }
            }
        },
        "response": {
            "status": 200
        }
    }
    ```
