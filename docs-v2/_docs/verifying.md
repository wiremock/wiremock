---
layout: docs
title: Verifying
toc_rank: 60
redirect_from: "/verifying.html"
description: Verifying whether specific HTTP requests were made.
---

The WireMock server records all requests it receives in memory (at
least until it is [reset](/docs/stubbing#reset)). This makes it possible to verify that
a request matching a specific pattern was received, and also to fetch
the requests' details.

Verifying and querying requests relies on the request journal, which is an in-memory log
of received requests. This can be disabled for load testing - see the [Configuration](/docs/configuration/) section for details.

Like stubbing, verification also uses WireMock's [Request Matching](/docs/request-matching/) system to filter and query requests.

## Verification failures, console output and IntelliJ

When verifying via the Java API all failed verifications will result in a `VerificationException` being thrown.
![Verification exception]({{ base_path }}/images/verification-exception.png)

The message text in the exception is formatted to enable IntelliJ's failure comparison view:
![Comparison failure]({{ base_path }}/images/idea-comparison-failure.png)

## Verifying in Java

To verify that a request matching some criteria was received by WireMock
at least once:

```java
verify(postRequestedFor(urlEqualTo("/verify/this"))
        .withHeader("Content-Type", equalTo("text/xml")));
```

The criteria part in the parameter to `postRequestedFor()` uses the same
builder as for stubbing, so all of the same predicates are available.
See [Stubbing](/docs/stubbing/) for more details.

To check for a precise number of requests matching the criteria, use
this form:

```java
verify(3, postRequestedFor(urlEqualTo("/three/times")));
```

Or you can use some more advanced comparison operators:

```java
verify(lessThan(5), postRequestedFor(urlEqualTo("/many")));
verify(lessThanOrExactly(5), postRequestedFor(urlEqualTo("/many")));
verify(exactly(5), postRequestedFor(urlEqualTo("/many")));
verify(moreThanOrExactly(5), postRequestedFor(urlEqualTo("/many")));
verify(moreThan(5), postRequestedFor(urlEqualTo("/many")));
```

## Verifying via the JSON + HTTP API

There isn't a direct JSON equivalent to the above Java API. However,
it's possible to achieve the same effect by requesting a count of the
number of requests matching the specified criteria (and in fact this is
what the Java method does under the hood).

This can be done by posting a JSON document containing the criteria to
`http://<host>:<port>/__admin/requests/count`:

```json
{
    "method": "POST",
    "url": "/resource/to/count",
    "headers": {
        "Content-Type": {
            "matches": ".*/xml"
        }
    }
}
```

A response of this form will be returned:

```json
{ "count": 4 }
```

## Querying the request journal

It is also possible to retrieve the details of logged requests. In
Java this is done via a call to `findAll()`:

```java
List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching("/api/.*")));
```

And in JSON + HTTP by posting a criteria document (of the same form as
for request counting) to `http://<host>:<port>/__admin/requests/find`,
which will return a response like this:

```json
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
```

## Resetting the request journal

The request log can be reset at any time. If you're using either of the
JUnit rules this will happen automatically at the start of every test
case. However you can do it yourself via a call to
`WireMock.resetAllRequests()` in Java or posting a request with an empty
body to `http://<host>:<port>/__admin/requests/reset`.


## Finding unmatched requests

To find all requests which were received but not matched by a configured stub (i.e. received the default 404 response) do the following in Java:

```java
List<LoggedRequest> unmatched = WireMock.findUnmatchedRequests();
```

To find unmatched requests via the HTTP API, make a `GET` request to `/__admin/requests/unmatched`:

```bash
GET http://localhost:8080/__admin/requests/unmatched
{
  "requests" : [ {
    "url" : "/nomatch",
    "absoluteUrl" : "http://localhost:8080/nomatch",
    "method" : "GET",
    "clientIp" : "0:0:0:0:0:0:0:1",
    "headers" : {
      "User-Agent" : "curl/7.30.0",
      "Accept" : "*/*",
      "Host" : "localhost:8080"
    },
    "cookies" : { },
    "browserProxyRequest" : false,
    "loggedDate" : 1467402464520,
    "bodyAsBase64" : "",
    "body" : "",
    "loggedDateString" : "2016-07-01T19:47:44Z"
  } ],
  "requestJournalDisabled" : false
}
```

## Near misses

"Near Misses" are enabled by the new "distance" concept added to the matching system.
A near miss is essentially a pairing of a request and request pattern that are not an exact match for each other, that can be ranked by distance.
This is useful when debugging test failures as it is quite common for a request not to be matched to a stub due to a minor difference e.g. a miscapitalised character.

Near misses can either represent the closest stubs to a given request, or the closest requests to a given request pattern depending on the type of query submitted.

To find near misses representing stub mappings closest to the specified request in Java:

```java
List<NearMiss> nearMisses = WireMock.findNearMissesFor(myLoggedRequest);
```

To do the same via the HTTP API:

```bash
POST /__admin/near-misses/request

{
  "url": "/actual",
  "absoluteUrl": "http://localhost:8080/actual",
  "method": "GET",
  "clientIp": "0:0:0:0:0:0:0:1",
  "headers": {
    "User-Agent": "curl/7.30.0",
    "Accept": "*/*",
    "Host": "localhost:8080"
  },
  "cookies": {},
  "browserProxyRequest": false,
  "loggedDate": 1467402464520,
  "bodyAsBase64": "",
  "body": "",
  "loggedDateString": "2016-07-01T19:47:44Z"
}
```

will return a response like:

```json
{
  "nearMisses": [
    {
      "request": {
        "url": "/actual",
        "absoluteUrl": "http://localhost:8080/nomatch",
        "method": "GET",
        "clientIp": "0:0:0:0:0:0:0:1",
        "headers": {
          "User-Agent": "curl/7.30.0",
          "Accept": "*/*",
          "Host": "localhost:8080"
        },
        "cookies": {},
        "browserProxyRequest": false,
        "loggedDate": 1467402464520,
        "bodyAsBase64": "",
        "body": "",
        "loggedDateString": "2016-07-01T19:47:44Z"
      },
      "stubMapping": {
        "uuid": "42aedcf2-1f8d-4009-ac7b-9870e4ab2316",
        "request": {
          "url": "/expected",
          "method": "GET"
        },
        "response": {
          "status": 200
        }
      },
      "matchResult": {
        "distance": 0.12962962962962962
      }
    }
  ]
}
```




To find near misses representing stub mappings closest to the specified request in Java:

```java
List<NearMiss> nearMisses = WireMock.findNearMissesFor(
    getRequestedFor(urlEqualTo("/thing-url"))
        .withRequestBody(containing("thing"))
);
```

The equivalent via the HTTP API:

```bash
POST /__admin/near-misses/request-pattern

{
    "url": "/almostmatch",
    "method": "GET"
}
```

will return a response like:

```json
{
  "nearMisses": [
    {
      "request": {
        "url": "/nomatch",
        "absoluteUrl": "http://localhost:8080/nomatch",
        "method": "GET",
        "clientIp": "0:0:0:0:0:0:0:1",
        "headers": {
          "User-Agent": "curl/7.30.0",
          "Accept": "*/*",
          "Host": "localhost:8080"
        },
        "cookies": {},
        "browserProxyRequest": false,
        "loggedDate": 1467402464520,
        "bodyAsBase64": "",
        "body": "",
        "loggedDateString": "2016-07-01T19:47:44Z"
      },
      "requestPattern": {
        "url": "/almostmatch",
        "method": "GET"
      },
      "matchResult": {
        "distance": 0.06944444444444445
      }
    }
  ]
}
```

As a convenience you can also find the top 3 near misses for every unmatched request:

```java
List<NearMiss> nearMisses = WireMock.findNearMissesForAllUnmatched();
```

To do the same via the HTTP API, issue a `GET` to `/__admin/requests/unmatched/near-misses`, which will produce output of the same form as
for the query for near misses by request.
