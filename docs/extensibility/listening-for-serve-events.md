---
description: Creating and registering serve event listeners
---

# Listening for Serve Events

Serve event listeners are intended for use when you wish to take an action at a specific point in the request processing flow, without affecting processing in any way. For instance a serve event listener would be the most suitable extension point to use for exporting telemetry data to a monitoring/observability tool.

The `ServeEventListener` interface (which deprecates `PostServeAction`) supports two different modes of operation - you can either override specific methods if the listener should only fire at a specific point in the request processing flow, or you can override a generic method then configure which lifecycle points it's fired at when binding the listener to specific stubs. Or it can simply be made to fire at all lifecycle points.

## Listening for specific lifecycle events

The `ServeEventListener` interface has a set of callback methods that can be implemented for specific points in the request lifecycle. These have no-op defaults, so you can override just the ones that are relevant:

```java
public class MyServeEventListener implements ServeEventListener {

    @Override
    public void beforeMatch(ServeEvent serveEvent, Parameters parameters) {
        // Do something before request matching
    }

    @Override
    public void afterMatch(ServeEvent serveEvent, Parameters parameters) {
        // Do something after request matching
    }

    @Override
    public void beforeResponseSent(ServeEvent serveEvent, Parameters parameters) {
        // Do something before the response is sent to the client
    }

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        // Do something after the response has been sent to the client
    }

    @Override
    public String getName() {
        return "my-listener";
    }
}
```

## Listening for all lifecycle events

The alternative approach you can take is to listen for all events along with a request phase value indicating when the event fired:

```java
public class MyServeEventListener implements ServeEventListener {

    @Override
    public void onEvent(
        RequestPhase requestPhase,
        ServeEvent serveEvent,
        Parameters parameters) {
        
        log.debug("Received serve event in phase " + requestPhase);
    }

    @Override
    public String getName() {
        return "my-listener";
    }
}
```