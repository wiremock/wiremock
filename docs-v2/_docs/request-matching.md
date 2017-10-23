---
layout: docs
title: Request Matching
toc_rank: 61
description: Matching and filtering HTTP requests in WireMock.
---

WireMock supports matching of requests to stubs and verification queries using the following attributes:

* URL
* HTTP Method
* Query parameters
* Headers
* Basic authentication (a special case of header matching)
* Cookies
* Request body

Here's an example showing all attributes being matched using WireMock's in-built match operators. It is also possible to write [custom matching logic](/docs/extending-wiremock/#custom-request-matchers) if
you need more precise control:

Java:

```java
stubFor(any(urlPathEqualTo("/everything"))
  .withHeader("Accept", containing("xml"))
  .withCookie("session", matching(".*12345.*"))
  .withQueryParam("search_term", equalTo("WireMock"))
  .withBasicAuth("jeff@example.com", "jeffteenjefftyjeff")
  .withRequestBody(equalToXml("<search-results />"))
  .withRequestBody(matchingXPath("//search-results"))
  .willReturn(aResponse()));
```

JSON:

```json
{
  "request" : {
    "urlPath" : "/everything",
    "method" : "ANY",
    "headers" : {
      "Accept" : {
        "contains" : "xml"
      }
    },
    "queryParameters" : {
      "search_term" : {
        "equalTo" : "WireMock"
      }
    },
    "cookies" : {
      "session" : {
        "matches" : ".*12345.*"
      }
    },
    "bodyPatterns" : [ {
      "equalToXml" : "<search-results />"
    }, {
      "matchesXPath" : "//search-results"
    } ],
    "basicAuthCredentials" : {
      "username" : "jeff@example.com",
      "password" : "jeffteenjefftyjeff"
    }
  },
  "response" : {
    "status" : 200
  }
}
```

The following sections describe each type of matching strategy in detail.

## URL matching

URLs can be matched either by equality or by regular expression. You also have a choice of whether to match just the path part of the URL or the path and query together.

It is usually preferable to match on path only if you want to match multiple query parameters in an order invariant manner.

### Equality matching on path and query

Java:

```java
urlEqualTo("/your/url?and=query")
```


JSON:

```json
{
  "request": {
    "url": "/your/url?and=query"
    ...
  },
  ...
}
```

### Regex matching on path and query

Java:

```java
urlMatching("/your/([a-z]*)\\?and=query")
```


JSON:

```json
{
  "request": {
    "urlPattern": "/your/([a-z]*)\\?and=query"
    ...
  },
  ...
}
```

### Equality matching on the path only

Java:

```java
urlPathEqualTo("/your/url")
```


JSON:

```json
{
  "request": {
    "urlPath": "/your/url"
    ...
  },
  ...
}
```

### Regex matching on the path only

Java:

```java
urlPathMatching("/your/([a-z]*)")
```


JSON:

```json
{
  "request": {
    "urlPathPattern": "/your/([a-z]*)"
    ...
  },
  ...
}
```


## Matching other attributes

All request attributes other the the URL can be matched using the following set of operators.

### Equality

Deems a match if the entire attribute value equals the expected value.

Java:

```java
.withHeader("Content-Type", equalTo("application/json"))
```

JSON:

```json
{
  "request": {
    ...
    "headers": {
      "Content-Type": {
        "equalTo": "application/json"
      }
    }
    ...
  },
  ...
}
```

### Case-insensitive equality

Deems a match if the entire attribute value equals the expected value, ignoring case.

Java:

```java
.withHeader("Content-Type", equalToIgnoreCase("application/json"))
```

JSON:

```json
{
  "request": {
    ...
    "headers": {
      "Content-Type": {
        "equalTo": "application/json",
        "caseInsensitive": true
      }
    }
    ...
  },
  ...
}
```

### Binary Equality

Deems a match if the entire binary attribute value equals the expected value. Unlike the above equalTo operator, this compares byte arrays (or their equivalent base64 representation).

Java:

```java
// Specifying the expected value as a byte array
.withRequestBody(binaryEqualTo(new byte[] { 1, 2, 3 }))

// Specifying the expected value as a base64 String
.withRequestBody(binaryEqualTo("AQID"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [{
        "binaryEqualTo" : "AQID" // Base 64
    }]
    ...
  },
  ...
}
```

### Substring (contains)

Deems a match if the a portion of the attribute value equals the expected value.

Java:

```java
.withCookie("my_profile", containing("johnsmith@example.com"))
```

JSON:

```json
{
  "request": {
    ...
    "cookies" : {
      "my_profile" : {
        "contains" : "johnsmith@example.com"
      }
    }
    ...
  },
  ...
}
```


### Regular expression

Deems a match if the entire attribute value matched the expected regular expression.

Java:

```java
.withQueryParam("search_term", matches("^(.*)wiremock([A-Za-z]+)$"))
```

JSON:

```json
{
  "request": {
    ...
    "queryParameters" : {
      "search_term" : {
        "matches" : "^(.*)wiremock([A-Za-z]+)$"
      }
    }
    ...
  },
  ...
}
```

It is also possible to perform a negative match i.e. the match succeeds when the attribute value does not match the regex:

Java:

```java
.withQueryParam("search_term", notMatching("^(.*)wiremock([A-Za-z]+)$"))
```

JSON:

```json
{
  "request": {
    ...
    "queryParameters" : {
      "search_term" : {
        "doesNotMatch" : "^(.*)wiremock([A-Za-z]+)$"
      }
    }
    ...
  },
  ...
}
```

### JSON equality

Deems a match if the attribute (most likely the request body in practice) is valid JSON and is a semantic match for the expected value.

Java:

```java
.withRequestBody(equalToJson("{ \"total_results\": 4 }"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "equalToJson" : "{ \"total_results\": 4 }"
    } ]
    ...
  },
  ...
}
```


By default different array orderings and additional object attributes will trigger a non-match. However, both of these conditions can be disabled individually.

Java:

```java
.withRequestBody(equalToJson("{ \"total_results\": 4  }", true, true))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "equalToJson" : "{ \"total_results\": 4  }",
      "ignoreArrayOrder" : true,
      "ignoreExtraElements" : true
    } ]
    ...
  },
  ...
}
```


### JSON Path

Deems a match if the attribute value is valid JSON and matches the [JSON Path](http://goessner.net/articles/JsonPath/) expression supplied. A JSON body will be considered to match a path expression if the expression returns either a non-null single value (string, integer etc.), or a non-empty object or array.

#### Presence matching

Deems a match if the attribute value is present in the JSON.

Java:

```java
.withRequestBody(matchingJsonPath("$.name"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$.name"
    } ]
    ...
  },
  ...
}
```

Request body example:

```
// matching
{ "name": "Wiremock" }
// not matching
{ "price": 15 }
```

#### Equality matching

Deems a match if the attribute value equals the expected value.

Java:

```java
.withRequestBody(matchingJsonPath("$.things[?(@.name == 'RequiredThing')]"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$.things[?(@.name == 'RequiredThing')]"
    } ]
    ...
  },
  ...
}
```

Request body example:

```
// matching
{ "things": { "name": "RequiredThing" } }
{ "things": [ { "name": "RequiredThing" }, { "name": "Wiremock" } ] }
// not matching
{ "price": 15 }
{ "things": { "name": "Wiremock" } }
```

#### Regex matching

Deems a match if the attribute value matches the regex expected value.

Java:

```java
.withRequestBody(matchingJsonPath("$.things[?(@.name =~ /Required.*/i)]"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$.things[?(@.name =~ /Required.*/i)]"
    } ]
    ...
  },
  ...
}
```

Request body example:

```
// matching
{ "things": { "name": "RequiredThing" } }
{ "things": [ { "name": "Required" }, { "name": "Wiremock" } ] }
// not matching
{ "price": 15 }
{ "things": { "name": "Wiremock" } }
{ "things": [ { "name": "Thing" }, { "name": "Wiremock" } ] }
```

#### Size matching

Deems a match if the attribute size matches the expected size.

Java:

```java
.withRequestBody(matchingJsonPath("$[?(@.things.size() == 2)]"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : "$[?(@.things.size() == 2)]"
    } ]
    ...
  },
  ...
}
```

Request body example:

```
// matching
{ "things": [ { "name": "RequiredThing" }, { "name": "Wiremock" } ] }
// not matching
{ "things": [ { "name": "RequiredThing" } ] }
```

#### Nested value matching

The JSONPath matcher can be combined with another matcher, such that the value returned from the JSONPath query is evaluated against it:
 
Java:

```java
.withRequestBody(matchingJsonPath("$..todoItem", containing("wash")))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : {
         "expression": "$..todoItem",
         "contains": "wash"
      }
    } ]
    ...
  },
  ...
}
```

Since WireMock's matching operators all work on strings, the value selected by the JSONPath expression will be coerced to a string before the match is evaluated. This true even if the returned value
is an object or array. A benefit of this is that this allows a sub-document to be selected using JSONPath, then matched using the `equalToJson` operator. E.g. for the following request body:

```json
{
    "outer": {
        "inner": 42
    }
}
```

The following will match:

```java
.withRequestBody(matchingJsonPath("$.outer", equalToJson("{                \n" +
                                                         "   \"inner\": 42 \n" +
                                                         "}")))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesJsonPath" : {
         "expression": "$.outer",
         "equalToJson": "{ \"inner\": 42 }"
      }
    } ]
    ...
  },
  ...
}
```


### XML equality

Deems a match if the attribute value is valid XML and is semantically equal to the expected XML document. The underlying engine for determining XML equality is [XMLUnit](http://www.xmlunit.org/).

Java:

```java
.withRequestBody(equalToXml("<thing>Hello</thing>"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "equalToXml" : "<thing>Hello</thing>"
    } ]
    ...
  },
  ...
}
```

### XPath

Deems a match if the attribute value is valid XML and matches the XPath expression supplied. An XML document will be considered to match if any elements are returned by the XPath evaluation. WireMock delegates to Java's in-built XPath engine (via XMLUnit), therefore up to (at least) Java 8 it supports XPath version 1.0.

Java:

```java
.withRequestBody(matchingXPath("/todo-list[count(todo-item) = 3]"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesXPath" : "/todo-list[count(todo-item) = 3]"
    } ]
    ...
  },
  ...
}
```

The above example will only work with non-namespaced XML. If you need to match a namespaced document with  it is necessary to declare the namespaces:

Java:

```java
.withRequestBody(matchingXPath("/stuff:outer/more:inner[.=111]")
  .withXPathNamespace("stuff", "http://stuff.example.com")
  .withXPathNamespace("more", "http://more.example.com"))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesXPath" : "/stuff:outer/more:inner[.=111]",
      "xPathNamespaces" : {
        "stuff" : "http://stuff.example.com",
        "more"  : "http://more.example.com"
      }
    } ]
    ...
  },
  ...
}
```

#### Nested value matching

The XPath matcher described above can be combined with another matcher, such that the value returned from the XPath query is evaluated against it:
 
Java:

```java
.withRequestBody(matchingXPath("//todo-item/text()", containing("wash")))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesXPath" : {
         "expression": "//todo-item/text()",
         "contains": "wash"
      }
    } ]
    ...
  },
  ...
}
```

If multiple nodes are returned from the XPath query, all will be evaluated and the returned match will be the one with the shortest distance.
 
If the XPath expression returns an XML element rather than a value, this will be rendered as an XML string before it is passed to the value matcher.
This can be usefully combined with the `equalToXml` matcher e.g.
 
Java:

```java
.withRequestBody(matchingXPath("//todo-item", equalToXml("<todo-item>Do the washing</todo-item>")))
```

JSON:

```json
{
  "request": {
    ...
    "bodyPatterns" : [ {
      "matchesXPath" : {
         "expression": "//todo-item",
         "equalToXml": "<todo-item>Do the washing</todo-item>"
      }
    } ]
    ...
  },
  ...
}
```

### Absence

Deems a match if the attribute specified is absent from the request.

Java:

```java
.withCookie("session", absent())
.withQueryParam("search_term", absent())
.withHeader("X-Absent", absent())
```

JSON:

```json
{
  "request": {
    ...
    "headers" : {
      "X-Absent" : {
        "absent" : true
      }
    },
    "queryParameters" : {
      "search_term" : {
        "absent" : true
      }
    },
    "cookies" : {
      "session" : {
        "absent" : true
      }
    }
    ...
  },
  ...
}
```

## Basic Authentication

Although matching on HTTP basic authentication could be supported via a
correctly encoded `Authorization` header, you can also do this more simply
via the API.

Java:

```java
stubFor(get(urlEqualTo("/basic-auth")).withBasicAuth("user", "pass")
```

JSON:

```json
{
    "request": {
        "method": "GET",
        "url": "/basic-auth",
        "basicAuth" : {
            "username" : "user",
            "password" : "pass"
        }
    },
    "response": {
        "status": 200
    }
}
```
