---
layout: docs
title: Stubbing
toc_rank: 50
redirect_from: "/stubbing.html"
description: Returning stubbed HTTP responses to specific requests.
---

A core feature of WireMock is the ability to return canned HTTP
responses for requests matching criteria. These are described in detail in [Request Matching](/docs/request-matching/).

## Basic stubbing

The following code will configure a response with a status of 200 to be
returned when the relative URL exactly matches `/some/thing` (including
query parameters). The body of the response will be "Hello world!" and a
`Content-Type` header will be sent with a value of `text-plain`.

```java
@Test
public void exactUrlOnly() {
    stubFor(get(urlEqualTo("/some/thing"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("Hello world!")));

    assertThat(testClient.get("/some/thing").statusCode(), is(200));
    assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
}
```

> **note**
>
> If you'd prefer to use slightly more BDDish language in your tests you
> can replace `stubFor` with `givenThat`.

To create the stub described above via the JSON API, the following
document can either be posted to
`http://<host>:<port>/__admin/mappings/new` or placed in a file with a
`.json` extension under the `mappings` directory:

```json
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
```



HTTP methods currently supported are:
`GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS`. You can specify `ANY` if
you want the stub mapping to match on any request method.

### Setting the response status message

In addition to the status code, the status message can optionally also
be set.

Java:

```java
@Test
public void statusMessage() {
    stubFor(get(urlEqualTo("/some/thing"))
            .willReturn(aResponse()
                .withStatus(200)
                .withStatusMessage("Everything was just fine!")
                .withHeader("Content-Type", "text/plain")));

    assertThat(testClient.get("/some/thing").statusCode(), is(200));
    assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
}
```

JSON:

```json
{
    "request": {
        "method": "GET",
        "url": "/some/thing"
    },
    "response": {
        "status": 200,
        "statusMessage": "Everything was just fine!"
    }
}
```

## Stub priority

It is sometimes the case that you'll want to declare two or more stub
mappings that "overlap", in that a given request would be a match for
more than one of them. By default, WireMock will use the most recently
added matching stub to satisfy the request. However, in some cases it is
useful to exert more control.

One example of this might be where you want to define a catch-all stub
for any URL that doesn't match any more specific cases. Adding a
priority to a stub mapping facilitates this:

```java
//Catch-all case
stubFor(get(urlMatching("/api/.*")).atPriority(5)
    .willReturn(aResponse().withStatus(401)));

//Specific case
stubFor(get(urlEqualTo("/api/specific-resource")).atPriority(1) //1 is highest
    .willReturn(aResponse()
            .withStatus(200)
            .withBody("Resource state")));
```

Priority is set via the `priority` attribute in JSON:

```json
{
    "priority": 1,
    "request": {
        "method": "GET",
        "url": "/api/specific-resource"
    },
    "response": {
        "status": 200
    }
}
```

## Sending response headers

In addition to matching on request headers, it's also possible to send response headers:

```java
stubFor(get(urlEqualTo("/whatever"))
        .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("Cache-Control", "no-cache")));
```

Or

```json
{
    "request": {
        "method": "GET",
        "url": "/whatever"
    },
    "response": {
        "status": 200,
        "headers": {
            "Content-Type": "text/plain",
            "Cache-Control": "no-cache"
        }
    }
}
```

## Specifying the response body

The simplest way to specify a response body is as a string literal.

Java:

```java
stubFor(get(urlEqualTo("/body"))
        .willReturn(aResponse()
                .withBody("Literal text to put in the body")));
```

JSON:

```json
{
    "request": {
        "method": "GET",
        "url": "/body"
    },
    "response": {
        "status": 200,
        "body": "Literal text to put in the body"
    }
}
```

If you're specifying a JSON body via the JSON API, you can avoid having to escape it like this:

```json
    "response": {
        "status": 200,
        "jsonBody": {
          "arbitrary_json": [1, 2, 3]
        }
    }
```


To read the body content from a file, place the file under the `__files`
directory. By default this is expected to be under `src/test/resources`
when running from the JUnit rule. When running standalone it will be
under the current directory in which the server was started. To make
your stub use the file, simply call `bodyFile()` on the response builder
with the file's path relative to `__files`:

```java
stubFor(get(urlEqualTo("/body-file"))
        .willReturn(aResponse()
                .withBodyFile("path/to/myfile.xml")));
```

Or

```json
{
    "request": {
        "method": "GET",
        "url": "/body-file"
    },
    "response": {
        "status": 200,
        "bodyFileName": "path/to/myfile.xml"
    }
}
```

> **note**
>
> All strings used by WireMock, including the contents of body files are
> expected to be in `UTF-8` format. Passing strings in other character
> sets, whether by JVM configuration or body file encoding will most
> likely produce strange behaviour.

A response body in binary format can be specified as a `byte[]` via an
overloaded `body()`:

```java
stubFor(get(urlEqualTo("/binary-body"))
        .willReturn(aResponse()
                .withBody(new byte[] { 1, 2, 3, 4 })));
```

The JSON API accepts this as a base64 string (to avoid stupidly long
JSON documents):

```json
{
    "request": {
        "method": "GET",
        "url": "/binary-body"
    },
    "response": {
        "status": 200,
        "base64Body" : "WUVTIElOREVFRCE="
    }
}
```

## Saving stubs

Stub mappings which have been created can be persisted to the `mappings`
directory via a call to `WireMock.saveAllMappings` in Java or posting a
request with an empty body to
`http://<host>:<port>/__admin/mappings/save`.

> **note**
> Note that this feature is not available when running WireMock from a servlet container.

## Editing stubs

Existing stub mappings can be modified, provided they have been assigned an ID.

In Java:

```java
wireMockServer.stubFor(get(urlEqualTo("/edit-this"))
    .withId(id)
    .willReturn(aResponse()
        .withBody("Original")));

assertThat(testClient.get("/edit-this").content(), is("Original"));

wireMockServer.editStub(get(urlEqualTo("/edit-this"))
    .withId(id)
    .willReturn(aResponse()
        .withBody("Modified")));

assertThat(testClient.get("/edit-this").content(), is("Modified"));
```

To do the equivalent via the JSON API, `POST` the edited stub mapping, with the same ID to `/__admin/mappings/edit`:

```json
{
  "uuid" : "4c4508c4-3fbd-43b8-9b5a-2775b8eefa27",
  "request" : {
    "urlPath" : "/edit-me",
    "method" : "ANY"
  },
  "response" : {
    "status" : 200
  }
}
```

## Removing stubs

Stub mappings which have been created can be removed via `mappings`
directory via a call to `WireMock.removeStubMapping` in Java or posting
a request with body that has the stub to
`http://<host>:<port>/__admin/mappings/remove`.

WireMock tries to match UUID is it is passed in the body of the stup to
a post request and if it finds the stub it removes it. if match is not
found, then it tries to match the request object found in the stub with
existing mappings and removes the first one that it finds.

For Example - posting following stub as body to
`http://<host>:<port>/__admin/mappings/remove`. will find first mapping
with request that matches url="/v8/asd/26", and method "method": "GET".

```json
{
  "request": {
      "url": "/v8/asd/26",
      "method": "GET"
    },
    "response": {
      "status": 202,
      "headers": { "Content-Type": "text/plain" } },
      "body": "response for test"
}
```

This is because body does not have UUID. If it had an element like
`"uuid": "aa85aed3-66c8-42bb-a79b-38e3264ff2ef"`, in addition to "request"
and "response" then WireMock will remove the one that matches the uuid
provided. removing via uuid has precedence over removing via request
match.

If the remove request UUID does not match with any of the stubs, then it
proceeds to remove the first request whose attributes are equal.

> **note**
> This api only removes one mapping and not multiple ones if they exist

> **note**
> This feature is not available when running WireMock from a servlet container.

## Reset

The WireMock server can be reset at any time, removing all stub mappings
and deleting the request log. If you're using either of the JUnit rules
this will happen automatically at the start of every test case. However
you can do it yourself via a call to `WireMock.reset()` in Java or
posting a request with an empty body to
`http://<host>:<port>/__admin/reset`.

If you've created some file based stub mappings to be loaded at startup
and you don't want these to disappear when you do a reset you can call
`WireMock.resetToDefault()` instead, or post an empty request to
`http://<host>:<port>/__admin/mappings/reset`.
