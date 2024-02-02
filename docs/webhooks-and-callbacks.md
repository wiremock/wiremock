---
description: Configuring WireMock to fire outbound HTTP requests when specific stubs are matched.
---

# Simulating Webhooks and Callbacks

WireMock can make asynchronous outbound HTTP calls when an incoming request is matched to a specific stub. This pattern
is commonly referred to as webhooks or callbacks and is a common design in APIs that need to proactively notify their clients
of events or perform long-running processing asynchronously without blocking.

## Enabling webhooks

Prior to WireMock 3.1.0 webhooks were provided via an extension and needed to be explicitly enabled. See [the 2.x docs](https://wiremock.org/2.x/docs/webhooks-and-callbacks/) for details on how to do this.

From version 3.1.0 the webhooks extension is part of WireMock's core and enabled by default.

### Old vs. new extension point

The revised version of webhooks in 3.1.0 makes use of the new `ServeEventListener` extension point.
This article shows how to use this newer extension point, however the legacy `PostServeAction` interface is still supported for backwards compatibility.

## Creating a simple, single webhook

You can trigger a single webhook request to a fixed URL, with fixed data like this:

=== "Java"

    ```java
    import static org.wiremock.webhooks.Webhooks.*;
    ...

    wm.stubFor(post(urlPathEqualTo("/something-async"))
        .willReturn(ok())
        .withServeEventListener("webhook", webhook()
            .withMethod(POST)
            .withUrl("http://my-target-host/callback")
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"result\": \"SUCCESS\" }"))
      );
    ```

=== "JSON"

    ```json
    {
        "request": {
            "urlPath": "/something-async",
            "method": "POST"
        },
        "response": {
            "status": 200
        },
        "serveEventListeners": [
            {
                "name": "webhook",
                "parameters": {
                    "method": "POST",
                    "url": "http://my-target-host/callback",
                    "headers": {
                        "Content-Type": "application/json"
                    },
                    "body": "{ \"result\": \"SUCCESS\" }"
                }
            }
        ]
    }
    ```

## Using data from the original request

Webhooks use the same [templating system](./response-templating.md) as WireMock responses. This means that any of the
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

=== "Java"

{% raw %}

    ```java
    wm.stubFor(post(urlPathEqualTo("/templating"))
          .willReturn(ok())
          .withServeEventListener("webhook", webhook()
              .withMethod(POST)
              .withUrl("http://my-target-host/callback")
              .withHeader("Content-Type", "application/json")
              .withBody("{ \"message\": \"success\", \"transactionId\": \"{{jsonPath originalRequest.body '$.transactionId'}}\" }")
      );
    ```

{% endraw %}

=== "JSON"

{% raw %}

    ```json
    {
        "request": {
            "urlPath": "/templating",
            "method": "POST"
        },
        "response": {
            "status": 200
        },
        "serveEventListeners": [
            {
                "name": "webhook",
                "parameters": {
                    "method": "POST",
                    "url": "http://my-target-host/callback",
                    "headers": {
                        "Content-Type": "application/json"
                    },
                    "body": "{ \"message\": \"success\", \"transactionId\": \"{{jsonPath originalRequest.body '$.transactionId'}}\" }"
                }
            }
        ]
    }
    ```

{% endraw %}

!!! note

    Webhook templates currently do not support system or environment variables.

## Implementing a callback using templating

To implement the callback pattern, where the original request contains the target to be called on completion of a long-running task,
we can use templating on the URL and method.

=== "Java"

{% raw %}
    ```java
    wm.stubFor(post(urlPathEqualTo("/something-async"))
          .willReturn(ok())
          .withServeEventListener("webhook", webhook()
              .withMethod("{{jsonPath originalRequest.body '$.callbackMethod'}}")
              .withUrl("{{jsonPath originalRequest.body '$.callbackUrl'}}"))
      );
    ```
{% endraw %}

=== "JSON"

{% raw %}
    ```json
    {
        "request": {
            "urlPath": "/something-async",
            "method": "POST"
        },
        "response": {
            "status": 200
        },
        "serveEventListeners": [
            {
                "name": "webhook",
                "parameters": {
                    "method": "{{jsonPath originalRequest.body '$.callbackMethod'}}",
                    "url": "{{jsonPath originalRequest.body '$.callbackUrl'}}"
                }
            }
        ]
    }
    ```

{% endraw %}

## Adding delays

A fixed or random delay can be added before the webhook call is made, using the same style of [delay parameters as stubs](./simulating-faults.md).

### Fixed delays

=== "Java"

{% raw %}

    ```java
    wm.stubFor(post(urlPathEqualTo("/delayed"))
        .willReturn(ok())
        .withServeEventListener("webhook", webhook()
          .withFixedDelay(1000)
          .withMethod(RequestMethod.GET)
          .withUrl("http://my-target-host/callback")
        )
    );
    ```
{% endraw %}

=== "JSON"

{% raw %}

    ```json
    {
        "request": {
            "urlPath": "/delayed",
            "method": "POST"
        },
        "response": {
            "status": 200
        },
        "serveEventListeners": [
            {
                "name": "webhook",
                "parameters": {
                    "method": "GET",
                    "url": "http://my-target-host/callback",
                    "delay": {
                        "type": "fixed",
                        "milliseconds": 1000
                    }
                }
            }
        ]
    }
    ```
{% endraw %}

### Random delays

=== "Java"

{% raw %}

    ```java
    wm.stubFor(post(urlPathEqualTo("/delayed"))
        .willReturn(ok())
        .withServeEventListener("webhook", webhook()
          .withDelay(new UniformDistribution(500, 1000))
          .withMethod(RequestMethod.GET)
          .withUrl("http://my-target-host/callback")
        )
    );
    ```
{% endraw %}

=== "JSON"

{% raw %}
    ```json
    {
        "request": {
            "urlPath": "/delayed",
            "method": "POST"
        },
        "response": {
            "status": 200
        },
        "serveEventListeners": [
            {
                "name": "webhook",
                "parameters": {
                    "method": "GET",
                    "url": "http://my-target-host/callback",
                    "delay": {
                        "type": "uniform",
                        "lower": 500,
                        "upper": 1000
                    }
                }
            }
        ]
    }
    ```
{% endraw %}

## Extending webhooks

Webhook behaviour can be further customised in code via an extension point.

This works in a similar fashion to response transformation. The extension class implements the `WebhookTransformer` interface and is then loaded via
the extension mechanism (see [Extending WireMock](https://wiremock.org/docs/extending-wiremock/)).

```java
public class MyWebhookTransformer implements WebhookTransformer {

  @Override
  public WebhookDefinition transform(
    ServeEvent serveEvent,
    WebhookDefinition webhookDefinition) {
    // build and return a new WebhookDefinition with some custom changes
  }
}
```
