---
layout: docs
title: Response Templating
toc_rank: 71
description: Generating dynamic responses using Handlebars templates
---

Response headers and bodies, as well as proxy URLs, can optionally be rendered using [Handlebars templates](http://handlebarsjs.com/). This enables attributes of the request
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

Command line parameters can be used to enable templating when running WireMock [standalone](/docs/running-standalone/#command-line-options).

## Template caching

By default, all templated fragments (headers, bodies and proxy URLs) are cached in their compiled form for performance,
since compilation can be expensive for larger templates.

The size of the cache is not limited by default, but can be a construction time:

```java
@Rule
public WireMockRule wm = new WireMockRule(options()
    .extensions(ResponseTemplateTransformer.builder()
                                .global(false)
                                .maxCacheEntries(3L)
                                .build())
);
```

Setting the limit to 0 will disable caching completely.


## Proxying

Templating also works when defining proxy URLs, e.g.

### Java

{% raw %}
```java
wm.stubFor(get(urlPathEqualTo("/templated"))
  .willReturn(aResponse()
      .proxiedFrom("{{request.headers.X-WM-Proxy-Url}}")
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
        "proxyBaseUrl": "{{request.headers.X-WM-Proxy-Url}}",
        "transformers": ["response-template"]
    }
}
```
{% endraw %}


## Templated body file

The body file for a response can be selected dynamically by templating the file path:

### Java

{% raw %}
```java
wm.stubFor(get(urlPathMatching("/static/.*"))
  .willReturn(ok()
    .withBodyFile("files/{{request.pathSegments.[1]}}")));

```
{% endraw %}


{% raw %}
### JSON
```json
{
  "request" : {
    "urlPathPattern" : "/static/.*",
    "method" : "GET"
  },
  "response" : {
    "status" : 200,
    "bodyFileName" : "files/{{request.pathSegments.[1]}}"
  }
}
```
{% endraw %}

## The request model
The model of the request is supplied to the header and body templates. The following request attributes are available:
 
`request.url` - URL path and query

`request.path` - URL path

`request.pathSegments.[<n>]`- URL path segment (zero indexed) e.g. `request.pathSegments.[2]`

`request.query.<key>`- First value of a query parameter e.g. `request.query.search`
 
`request.query.<key>.[<n>]`- nth value of a query parameter (zero indexed) e.g. `request.query.search.[5]`

`request.method`- request method e.g. `POST`

`request.host`- hostname part of the URL e.g. `my.example.com`

`request.port`- port number e.g. `8080`

`request.scheme`- protocol part of the URL e.g. `https`

`request.baseUrl`- URL up to the start of the path e.g. `https://my.example.com:8080`
 
`request.headers.<key>`- First value of a request header e.g. `request.headers.X-Request-Id`
 
`request.headers.[<key>]`- Header with awkward characters e.g. `request.headers.[$?blah]`

`request.headers.<key>.[<n>]`- nth value of a header (zero indexed) e.g. `request.headers.ManyThings.[1]`

`request.cookies.<key>` - First value of a request cookie e.g. `request.cookies.JSESSIONID`
 
`request.cookies.<key>.[<n>]` - nth value of a request cookie e.g. `request.cookies.JSESSIONID.[2]`

`request.body` - Request body text (avoid for non-text bodies)


### Values that can be one or many

A number of HTTP elements (query parameters, form fields, headers) can be single or multiple valued. The template request model and built-in helpers attempt to make
this easy to work with by wrapping these in a "list or single" type that returns the first (and often only) value when no index is specified, but also support index access.

For instance, given a request URL like `/multi-query?things=1&things=2&things=3` I can extract the query data in the following ways:

{% raw %}
```
{{request.query.things}} // Will return 1
{{request.query.things.0}} // Will return 1
{{request.query.things.first}} // Will return 1
{{request.query.things.1}} // Will return 2
{{request.query.things.[-1]}} // Will return 2
{{request.query.things.last}} // Will return 3
```
{% endraw %}

> **Note**
>
> When using the `eq` helper with one-or-many values, it is necessary to use the indexed form, even if only one value is present.
> The reason for this is that the non-indexed form returns the wrapper type and not a String, and will therefore fail any comparison
> with another String value. 


### Getting values with keys containing special characters

Certain characters have special meaning in Handlebars and therefore can't be used in key names when referencing values.
If you need to access keys containing these characters you can use the `lookup` helper, which permits you to pass the key
name as a string literal and thus avoid the restriction.

Probably the most common occurrence of this issue is with array-style query parameters, so for instance if your request
URLs you're matching are of the form `/stuff?ids[]=111&ids[]=222&ids[]=333` then you can access these values like:

{% raw %}
```
{{lookup request.query 'ids[].1'}} // Will return 222
```
{% endraw %} 

## Using transformer parameters

Parameter values can be passed to the transformer as shown below (or dynamically added to the parameters map programmatically in custom transformers).

### Java

{% raw %}
```java
wm.stubFor(get(urlPathEqualTo("/templated"))
  .willReturn(aResponse()
      .withBody("{{request.path.[0]}}")
      .withTransformers("response-template")
      .withTransformerParameter("MyCustomParameter", "Parameter Value")));
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
        "transformers": ["response-template"],
        "transformerParameters" : {
            "MyCustomParameter" : "Parameter Value"
        }
    }
}
```
{% endraw %}

These parameters can be referenced in template body content using the `parameters.` prefix:

{% raw %}
```
<h1>The MyCustomParameter value is {{parameters.MyCustomParameter}}</h1>
```
{% endraw %}

## Handlebars helpers
All of the standard helpers (template functions) provided by the [Java Handlebars implementation by jknack](https://github.com/jknack/handlebars.java)
plus all of the [string helpers](https://github.com/jknack/handlebars.java/blob/master/handlebars/src/main/java/com/github/jknack/handlebars/helper/StringHelpers.java)
and the [conditional helpers](https://github.com/jknack/handlebars.java/blob/master/handlebars/src/main/java/com/github/jknack/handlebars/helper/ConditionalHelpers.java)
are available e.g.

{% raw %}
```
{{capitalize request.query.search}}
```
{% endraw %}


## Number and assignment helpers
Variable assignment and number helpers are available:

{% raw %}
```
{{#assign 'myCapitalisedQuery'}}{{capitalize request.query.search}}{{/assign}}

{{isOdd 3}}
{{isOdd 3 'rightBox'}}

{{isEven 2}}
{{isEven 4 'leftBox'}}

{{stripes 3 'row-even' 'row-odd'}}
```
{% endraw %}


## XPath helpers
Addiionally some helpers are available for working with JSON and XML.
 
When the incoming request contains XML, the `xPath` helper can be used to extract values or sub documents via an XPath 1.0 expression. For instance, given the XML

```xml
<outer>
    <inner>Stuff</inner>
</outer>
```

The following will render "Stuff" into the output:
  
{% raw %}
```
{{xPath request.body '/outer/inner/text()'}}
```
{% endraw %}

And given the same XML the following will render `<inner>Stuff</inner>`:
 
{% raw %}
```
{{xPath request.body '/outer/inner'}}
```
{% endraw %}


As a convenience the `soapXPath` helper also exists for extracting values from SOAP bodies e.g. for the SOAP document:
   
```xml
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope/">
    <soap:Body>
        <m:a>
            <m:test>success</m:test>
        </m:a>
    </soap:Body>
</soap:Envelope>
```

The following will render "success" in the output:

{% raw %}
```
{{soapXPath request.body '/a/test/text()'}}
```
{% endraw %}


### Using the output of `xPath` in other helpers

Since version 2.27.0 the XPath helper returns collections of node objects rather than a single string, meaning that the result
can be used in further helpers.

The returned node objects have the following properties:

`name` - the local XML element name.

`text` - the text content of the element.

`attributes` - a map of the element's attributes (name: value)

Referring to the node itself will cause it to be printed.


A common use case for returned node objects is to iterate over the collection with the `each` helper:

{% raw %}
```
{{#each (xPath request.body '/things/item') as |node|}}
  name: {{node.name}}, text: {{node.text}}, ID attribute: {{node.attributes.id}}
{{/each}}
```
{% endraw %}

## JSONPath helper
It is similarly possible to extract JSON values or sub documents via JSONPath using the `jsonPath` helper. Given the JSON

```json
{
  "outer": {
    "inner": "Stuff"
  }
}
```

The following will render "Stuff" into the output:

{% raw %}
```
{{jsonPath request.body '$.outer.inner'}}
```
{% endraw %}

And for the same JSON the following will render `{ "inner": "Stuff" }`:

{% raw %}
```
{{jsonPath request.body '$.outer'}}
```
{% endraw %}

Default value can be specified if the path evaluates to null or undefined:

{% raw %}
```
{{jsonPath request.body '$.size' default='M'}}
```
{% endraw %}

## Parse JSON helper
The `parseJson` helper will parse the input into a map-of-maps. It will assign the result to a variable if a name is specified,
otherwise the result will be returned.

It can accept the JSON from a block:

{% raw %}
```
{{#parseJson 'parsedObj'}}
{
  "name": "transformed"
}
{{/parseJson}}

{{!- Now we can access the object as usual --}}
{{parsedObj.name}}
```
{% endraw %}

Or as a parameter:

{% raw %}
```
{{parseJson request.body 'bodyJson'}}
{{bodyJson.name}}
```
{% endraw %}

Without assigning to a variable:

{% raw %}
```
{{lookup (parseJson request.body) 'name'}}
```
{% endraw %}

## Date and time helpers
A helper is present to render the current date/time, with the ability to specify the format ([via Java's SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)) and offset.
 
{% raw %}
```
{{now}}
{{now offset='3 days'}}
{{now offset='-24 seconds'}}
{{now offset='1 years'}}
{{now offset='10 years' format='yyyy-MM-dd'}}
```
{% endraw %}

Dates can be rendered in a specific timezone (the default is UTC):

{% raw %}
```
{{now timezone='Australia/Sydney' format='yyyy-MM-dd HH:mm:ssZ'}}
```
{% endraw %}

Pass `epoch` as the format to render the date as UNIX epoch time (in milliseconds), or `unix` as the format to render
the UNIX timestamp in seconds.

{% raw %}
```
{{now offset='2 years' format='epoch'}}
{{now offset='2 years' format='unix'}}
```
{% endraw %}


Dates can be parsed using the `parseDate` helper:

{% raw %}
```
// Attempts parsing using ISO8601, RFC 1123, RFC 1036 and ASCTIME formats.
// We wrap in the date helper in order to print the result as a string.
{{date (parseDate request.headers.MyDate)}}

// Parse using a custom date format
{{date (parseDate request.headers.MyDate format='dd/MM/yyyy')}}

// Format can also be unix (epoch seconds) or epoch (epoch milliseconds)
{{date (parseDate request.headers.MyDate format='unix')}}
```
{% endraw %}


Dates can be truncated to e.g. first day of month using the `truncateDate` helper:

{% raw %}
```
// If the MyDate header is Tue, 15 Jun 2021 15:16:17 GMT
// then the result of the following will be 2021-06-01T00:00:00Z
{{date (truncateDate (parseDate request.headers.MyDate) 'first day of month')}}
```
{% endraw %}

See the [full list of truncations here](/docs/request-matching/#all-truncations).

## Random value helper
Random strings of various kinds can be generated:

{% raw %}
```
{{randomValue length=33 type='ALPHANUMERIC'}}
{{randomValue length=12 type='ALPHANUMERIC' uppercase=true}}
{{randomValue length=55 type='ALPHABETIC'}}
{{randomValue length=27 type='ALPHABETIC' uppercase=true}}
{{randomValue length=10 type='NUMERIC'}}
{{randomValue length=5 type='ALPHANUMERIC_AND_SYMBOLS'}}
{{randomValue type='UUID'}}
{{randomValue length=32 type='HEXADECIMAL' uppercase=true}}
```
{% endraw %}


## Pick random helper
A value can be randomly selected from a literal list:

{% raw %}
```
{{{pickRandom '1' '2' '3'}}}
```
{% endraw %}

Or from a list passed as a parameter:

{% raw %}
```
{{{pickRandom (jsonPath request.body '$.names')}}}
```
{% endraw %}

## Random number helpers
These helpers produce random numbers of the desired type. By returning actual typed numbers rather than strings
we can use them for further work e.g. by doing arithemetic with the `math` helper or randomising the bound in a `range`.

Random integers can be produced with lower and/or upper bounds, or neither:

{% raw %}
```
{{randomInt}}
{{randomInt lower=5 upper=9}}
{{randomInt upper=54323}}
{{randomInt lower=-24}}
```
{% endraw %}

Likewise decimals can be produced with or without bounds:

{% raw %}
```
{{randomDecimal}}
{{randomDecimal lower=-10.1 upper=-0.9}}
{{randomDecimal upper=12.5}}
{{randomDecimal lower=-24.01}}
```
{% endraw %}


## Math helper
The `math` (or maths, depending where you are) helper performs common arithmetic operations. It can accept integers, decimals
or strings as its operands and will always yield a number as its output rather than a string.

Addition, subtraction, multiplication, division and remainder (mod) are supported:

{% raw %}
```
{{math 1 '+' 2}}
{{math 4 '-' 2}}
{{math 2 '*' 3}}
{{math 8 '/' 2}}
{{math 10 '%' 3}}
```
{% endraw %}

## Range helper
The `range` helper will produce an array of integers between the bounds specified:

{% raw %}
```
{{range 3 8}}
{{range -2 2}}
```
{% endraw %}

This can be usefully combined with `randomInt` and `each` to output random length, repeating pieces of content e.g.

{% raw %}
```
{{#each (range 0 (randomInt lower=1 upper=10)) as |index|}}
id: {{index}}
{{/each}}
```
{% endraw %}

## Array literal helper
The `array` helper will produce an array from the list of parameters specified. The values can be any valid type.
Providing no parameters will result in an empty array.

{% raw %}
```
{{array 1 'two' true}}
{{array}}
```
{% endraw %}

## Contains helper
The `contains` helper returns a boolean value indicating whether the string or array passed as the first parameter
contains the string passed in the second.

It can be used as parameter to the `if` helper:

{% raw %}
```
{{#if (contains 'abcde' 'abc')}}YES{{/if}}
{{#if (contains (array 'a' 'b' 'c') 'a')}}YES{{/if}}
```
{% endraw %}

Or as a block element on its own:

{% raw %}
```
{{#contains 'abcde' 'abc'}}YES{{/contains}}
{{#contains (array 'a' 'b' 'c') 'a'}}YES{{/contains}}
```
{% endraw %}

## Matches helper
The `matches` helper returns a boolean value indicating whether the string passed as the first parameter matches the
regular expression passed in the second:

Like the `contains` helper it can be used as parameter to the `if` helper:

{% raw %}
```
{{#if (matches '123' '[0-9]+')}}YES{{/if}}
```
{% endraw %}

Or as a block element on its own:

{% raw %}
```
{{#matches '123' '[0-9]+'}}YES{{/matches}}
```
{% endraw %}

## String trim helper
Use the `trim` helper to remove whitespace from the start and end of the input:

{% raw %}
```
{{trim request.headers.X-Padded-Header}}

{{#trim}}
    
    Some stuff with whitespace
     
{{/trim}}
```
{% endraw %}


## Base64 helper
The `base64` helper can be used to base64 encode and decode values:

{% raw %}
```
{{base64 request.headers.X-Plain-Header}}
{{base64 request.headers.X-Encoded-Header decode=true}}

{{#base64}}
Content to encode     
{{/base64}}

{{#base64 padding=false}}
Content to encode without padding    
{{/base64}}

{{#base64 decode=true}}
Q29udGVudCB0byBkZWNvZGUK     
{{/base64}}
```
{% endraw %}


## URL encoding helper
The `urlEncode` helper can be used to URL encode and decode values:

{% raw %}
```
{{urlEncode request.headers.X-Plain-Header}}
{{urlEncode request.headers.X-Encoded-Header decode=true}}

{{#urlEncode}}
Content to encode     
{{/urlEncode}}

{{#urlEncode decode=true}}
Content%20to%20decode     
{{/urlEncode}}
```
{% endraw %}


## Form helper
The `formData` helper parses its input as an HTTP form, returning an object containing the individual fields as attributes.
The helper takes the input string and variable name as its required parameters, with an optional `urlDecode` parameter
indicating that values should be URL decoded. The folowing example will parse the request body as a form, then output a single field `formField3`: 

{% raw %}
```
{{formData request.body 'form' urlDecode=true}}{{form.formField3}}
```
{% endraw %}

If the form submitted has multiple values for a given field, these can be accessed by index:

{% raw %}
```
{{formData request.body 'form' urlDecode=true}}{{form.multiValueField.1}}, {{form.multiValueField.2}}
{{formData request.body 'form' urlDecode=true}}{{form.multiValueField.first}}, {{form.multiValueField.last}}
```
{% endraw %}


## Regular expression extract helper
The `regexExtract` helper supports extraction of values matching a regular expresson from a string.

A single value can be extracted like this:

{% raw %}
```
{{regexExtract request.body '[A-Z]+'}}"
```
{% endraw %}

Regex groups can be used to extract multiple parts into an object for later use (the last parameter is a variable name to which the object will be assigned):

{% raw %}
``` 
{{regexExtract request.body '([a-z]+)-([A-Z]+)-([0-9]+)' 'parts'}}
{{parts.0}},{{parts.1}},{{parts.2}}
```
{% endraw %}

Optionally, a default value can be specified for when there is no match.

{% raw %}
```
{{regexExtract 'abc' '[0-9]+' default='my default value'}}
```
{% endraw %}
 

## Size helper

The `size` helper returns the size of a string, list or map:

{% raw %}
``` 
{{size 'abcde'}}
{{size request.query.things}}
```
{% endraw %}

## Hostname helper

The local machine's hostname can be printed:

{% raw %}
```
{{hostname}}
```
{% endraw %}


## System property helper
Environment variables and system properties can be printed: 
 
{% raw %}
```
{{systemValue type='ENVIRONMENT' key='PATH'}}
{{systemValue type='PROPERTY' key='os.path'}}
```
{% endraw %}

To avoid disclosure of sensitive variables, only permitted variables can be read. Permitted variable names
are defined via a set of regular expressions. These can be configured when constructing the response template extension:

```java
@Rule
public WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .withRootDirectory(defaultTestFilesRoot())
        .extensions(new ResponseTemplateTransformer.Builder()
                .global(true)
                .permittedSystemKeys("allowed.*","also_permitted.*")
                .build()
        )
);
```

The regular expressions are matched in a case-insensitive manner.

If no permitted system key patterns are set, a single default of `wiremock.*` will be used. 

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
public WireMockRule wm = new WireMockRule(
    options().extensions(new ResponseTemplateTransformer(false, "string-length", stringLengthHelper))
);
```


