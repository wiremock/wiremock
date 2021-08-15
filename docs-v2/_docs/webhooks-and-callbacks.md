---
layout: docs
title: Webhooks and Callbacks
toc_rank: 105
description: Configuring WireMock to fire outbound HTTP requests when specific stubs are matched.
---

WireMock can make asynchronous outbound HTTP calls when an incoming request is matched to a specific stub. This pattern
is commonly referred to as webhooks or callbacks and is a common design in APIs that need to proactively notify their clients
of events or perform long-running processing asynchronously without blocking.

## Enabling webhooks
Webhooks are provided via a WireMock extension, so this must be added when starting WireMock.

### Java
If you're starting WireMock programmatically the webhooks extension must be added to your classpath.

Maven:

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-webhooks-extension</artifactId>
    <version>{{ site.wiremock_version }}</version>
    <scope>test</scope>
</dependency>
```

Gradle:

```groovy
testCompile "org.wiremock:wiremock-webhooks-extension:{{ site.wiremock_version }}"
```

Then when creating the `WireMockServer` or `WireMockRule` the extension must be passed via the configuration object
in the constructor:

```java
@Rule
public WireMockRule wm = new WireMockRule(wireMockConfig().extensions(Webhooks.class));
```

### Standalone

To use the webhooks extension with standalone WireMock you must download the extension JAR file and add it to the Java classpath
on the startup command line:

```bash
java -cp wiremock-jre8-standalone-{{ site.wiremock_version }}.jar:wiremock-webhooks-extension-{{ site.wiremock_version }}.jar \
  com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
  --extensions org.wiremock.webhooks.Webhooks
```

You can [download the webhooks extension JAR here](https://repo1.maven.org/maven2/org/wiremock/wiremock-webhooks-extension/{{ site.wiremock_version }}/wiremock-webhooks-extension-{{ site.wiremock_version }}.jar).

## A simple, single webhook
You can trigger a single webhook request to a fixed URL, with fixed data like this:

Java:
```java
import static org.wiremock.webhooks.Webhooks.*;
...
  
wm.stubFor(post(urlPathEqualTo("/something-async"))
    .willReturn(ok())
    .withPostServeAction("webhook", webhook()
        .withMethod(POST)
        .withUrl("http://my-target-host/callback")
        .withHeader("Content-Type", "application/json")
        .withBody("{ \"result\": \"SUCCESS\" }"))
  );
```

JSON:

```json
{
  "request" : {
    "urlPath" : "/something-async",
    "method" : "POST"
  },
  "response" : {
    "status" : 200
  },
  "postServeActions" : [{
    "name" : "webhook",
    "parameters" : {
      "method" : "POST",
      "url" : "http://my-target-host/callback",
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : "{ \"result\": \"SUCCESS\" }"
    }
  }]
}
```

## Using data from the original request

Webhooks use the same [templating system](/docs/response-templating/) as WireMock responses. This means that any of the
configuration fields can be provided with a template expression which will be resolved before firing the webhook.

Similarly to response templates the original request data is available, although in this case it is named `originalRequest`.

Supposing we wanted to pass a transaction ID from the original (triggering) request and insert it into the JSON request
body sent by the webhook call.

For an original request body JSON like this:

```json
{
  "transactionId": "12345"
}
```

We could construct a JSON request body in the webhook like this:

Java:

{% raw %}
```java
wm.stubFor(post(urlPathEqualTo("/templating"))
      .willReturn(ok())
      .withPostServeAction("webhook", webhook()
          .withMethod(POST)
          .withUrl("http://my-target-host/callback")
          .withHeader("Content-Type", "application/json")
          .withBody("{ \"message\": \"success\", \"transactionId\": \"{{jsonPath originalRequest.body '$.transactionId'}}\" }")
  );
```
{% endraw %}


JSON:

{% raw %}
```json
{
  "request" : {
    "urlPath" : "/templating",
    "method" : "POST"
  },
  "response" : {
    "status" : 200
  },
  "postServeActions" : [{
    "name" : "webhook",
    "parameters" : {
      "method" : "POST",
      "url" : "http://my-target-host/callback",
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : "{ \"message\": \"success\", \"transactionId\": \"{{jsonPath originalRequest.body '$.transactionId'}}\" }"
    }
  }]
}
```
{% endraw %}


> **note**
>
> Webhook templates currently do not support system or environment variables.


## Implementing a callback using templating
To implement the callback pattern, where the original request contains the target to be called on completion of a long-running task,
we can use templating on the URL and method.

Java:

{% raw %}
```java
wm.stubFor(post(urlPathEqualTo("/something-async"))
      .willReturn(ok())
      .withPostServeAction("webhook", webhook()
          .withMethod("{{jsonPath originalRequest.body '$.callbackMethod'}}")
          .withUrl("{{jsonPath originalRequest.body '$.callbackUrl'}}"))
  );
```
{% endraw %}


JSON:

{% raw %}
```json
{
  "request" : {
    "urlPath" : "/something-async",
    "method" : "POST"
  },
  "response" : {
    "status" : 200
  },
  "postServeActions" : [{
    "name" : "webhook",
    "parameters" : {
      "method" : "{{jsonPath originalRequest.body '$.callbackMethod'}}",
      "url" : "{{jsonPath originalRequest.body '$.callbackUrl'}}"
    }
  }]
}
```
{% endraw %}



## Adding delays
A fixed or random delay can be added before the webhook call is made, using the same style of [delay parameters as stubs](/docs/simulating-faults/).

### Fixed delays

Java:

```java
wm.stubFor(post(urlPathEqualTo("/delayed"))
    .willReturn(ok())
    .withPostServeAction("webhook", webhook()
      .withFixedDelay(1000)
      .withMethod(RequestMethod.GET)
      .withUrl("http://my-target-host/callback")
    )
);
```

JSON:

```json
{
  "request" : {
    "urlPath" : "/delayed",
    "method" : "POST"
  },
  "response" : {
    "status" : 200
  },
  "postServeActions" : [{
    "name" : "webhook",
    "parameters" : {
      "method" : "GET",
      "url" : "http://my-target-host/callback",
      "delay" : {
        "type" : "fixed",
        "milliseconds" : 1000
      }
    }
  }]
}
```

### Random delays
Java:

```java
wm.stubFor(post(urlPathEqualTo("/delayed"))
    .willReturn(ok())
    .withPostServeAction("webhook", webhook()
      .withDelay(new UniformDistribution(500, 1000))
      .withMethod(RequestMethod.GET)
      .withUrl("http://my-target-host/callback")
    )
);
```

JSON:

```json
{
  "request" : {
    "urlPath" : "/delayed",
    "method" : "POST"
  },
  "response" : {
    "status" : 200
  },
  "postServeActions" : [{
    "name" : "webhook",
    "parameters" : {
      "method" : "GET",
      "url" : "http://my-target-host/callback",
      "delay" : {
        "type" : "uniform",
        "lower" : 500,
        "upper" : 1000
      }
    }
  }]
}
```