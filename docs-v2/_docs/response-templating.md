---
layout: docs
title: Response Templating
toc_rank: 71
description: Generating dynamic responses using Handlebars templates
---

Response headers and bodies can optionally be rendered using [Handlebars templates](http://handlebarsjs.com/). This enables attributes of the request
to be used in generating the response e.g. to pass the value of a request ID header as a response header or
render an identifier from part of the URL in the response body.
 
## Enabling response templating
When starting WireMock programmatically, response templating can be enabled by adding `ResponseTemplateTransformer` as an extension e.g.

```java
@Rule
public WireMockRule wm = new WireMockRule(options()
    .extensions(new ResponseTemplateTransformer(false))
);
```


The boolean constructor parameter indicates whether the extension should be applied globally. If true, all stub mapping responses will be rendered as templates prior
to being served.

Otherwise the transformer will need to be specified on each stub mapping by its name `response-template`:

Command line parameters can be used to enable templating when running WireMock [standalone](/docs/running-standalone/#command-line-options). 
  
### Java

{% raw %}
```java
wm.stubFor(get(urlPathEqualTo("/templated"))
  .willReturn(aResponse()
      .withBody("{{request.path.[0]}}")
      .withTransformers("response-template")));
```
{% endraw %}


{% raw %}
### JSON
```json
{
    "request": {
        "urlPath": "/templated"
    },
    "response": {
        "body": "{{request.path.[0]}}",
        "transformers": ["response-template"]
    }
}
```
{% endraw %}

## The request model
The model of the request is supplied to the header and body templates. The following request attributes are available:
 
`request.url` - URL path and query

`request.path` - URL path

`request.path.[<n>]`- URL path segment (zero indexed) e.g. `request.path.[2]`

`request.query.<key>`- First value of a query parameter e.g. `request.query.search`
 
`request.query.<key>.[<n>]`- nth value of a query parameter (zero indexed) e.g. `request.query.search.[5]`
 
`request.headers.<key>`- First value of a request header e.g. `request.headers.X-Request-Id`
 
`request.headers.[<key>]`- Header with awkward characters e.g. `request.headers.[$?blah]`

`request.headers.<key>.[<n>]`- nth value of a header (zero indexed) e.g. `request.headers.ManyThings.[1]`

`request.cookies.<key>` - Value of a request cookie e.g. `request.cookies.JSESSIONID` 

`request.body` - Request body text (avoid for non-text bodies)


## Handlebars helpers
All of the standard helpers (template functions) provided by the [Java Handlebars implementation by jknack](https://github.com/jknack/handlebars.java)
plus all of the [string helpers](https://github.com/jknack/handlebars.java/blob/master/handlebars/src/main/java/com/github/jknack/handlebars/helper/StringHelpers.java)
are available e.g.

{% raw %}
```
{{capitalize request.query.search}}
```
{% endraw %}


## Custom helpers
Custom Handlebars helpers can be registered with the transformer on construction:
  
```java
Helper<String> stringLengthHelper = new Helper<String>() {
    @Override
    public Object apply(String context, Options options) throws IOException {
        return context.length();
    }
};

@Rule
public WireMockRule wm = new WireMockRule(options()
    .extensions(new ResponseTemplateTransformer(false), "string-length", stringLengthHelper)
);
```


