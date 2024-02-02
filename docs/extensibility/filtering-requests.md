---
description: Filtering and modifying requests via extensions
---

# Filtering and Modifying Requests

Requests to both stubs and the admin API can be intercepted and either modified or halted with an immediate response.
This supports a number of use cases including: authentication, URL rewriting and request header injection.

To intercept stub requests, create a class that extends `StubRequestFilter`. For instance, to perform simple authentication:

```java
public class SimpleAuthRequestFilter implements StubRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
        if (request.header("Authorization").firstValue().equals("Basic abc123")) {
            return RequestFilterAction.continueWith(request);
        }

        return RequestFilterAction.stopWith(ResponseDefinition.notAuthorised());
    }

    @Override
    public String getName() {
        return "simple-auth";
    }
}
```

Then add it as an extension as usual e.g.

```java
new WireMockRule(wireMockConfig().extensions(new SimpleAuthRequestFilter()));
```

To intercept admin API follow the same process, but extend `AdminRequestFilter`.


### Modifying the request during interception

To modify the HTTP request, the simplest approach is to wrap the original request with a `RequestWrapper` then continue e.g.

```java
public static class UrlAndHeadersModifyingFilter extends StubRequestFilterV2 {

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
        Request wrappedRequest = RequestWrapper.create()
                .transformAbsoluteUrl(url -> url + "?extraQueryParam=123")
                .addHeader("X-Custom-Header", "headerval")
                .wrap(request);

        return RequestFilterAction.continueWith(wrappedRequest);
    }

    @Override
    public String getName() {
        return "url-and-header-modifier";
    }
}
```

`RequestWrapper` is a builder pattern and allows any number of changes to the request. It supports the following changes:

-   Transformation of the URL. `transformAbsoluteUrl` takes a `FieldTransformer` as a parameter (or equivalent lambda) which maps
    from the old to the new URL. Note that the URL passed in is absolute, and the returned URL must also be.
-   Addition, removal and transformation (via `FieldTransformer`) of headers.
-   Addition, removal and transformation of cookies.
-   Changing the HTTP method.
-   Transformation of the request body.
-   Transformation of body parts (if a multipart encoded request).
