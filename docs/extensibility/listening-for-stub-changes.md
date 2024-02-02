---
description: Listening for Stub Changes
---

# Listening for Stub Changes

You can subscribe to changes in the state of WireMock's stubs via the `StubLifecycleListener` extension point.

For instance, to respond after a new stub has been created you would do the following:

```java
public class MyStubEventListener implements StubLifecycleListener {

    @Override
    public void afterStubCreated(StubMapping stub) {
        log.debug("Stub named " + stub.getName() + " was created");
    }

    @Override
    public String getName() {
        return "my-listener";
    }
}
```

The following methods can be overridden to subscribe to various stub lifecycle events:

```java
void beforeStubCreated(StubMapping stub)
void afterStubCreated(StubMapping stub)
void beforeStubEdited(StubMapping oldStub, StubMapping newStub)
void afterStubEdited(StubMapping oldStub, StubMapping newStub)
void beforeStubRemoved(StubMapping stub)
void afterStubRemoved(StubMapping stub)
void beforeStubsReset()
void afterStubsReset()
```