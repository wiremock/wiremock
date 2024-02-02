---
description: Listening for Settings Changes
---

# Listening for Settings Changes

You can listen for changes to the global settings object.

This is most useful when combined with other extension points, allowing extensions to define and make use of extended settings values rather than rolling their own configuration strategy.

A common pattern is to listen for changes and capture the value locally, using this to affect the main extension's behaviour e.g.:

```java
public class MyConfigurableServeEventListener
        implements ServeEventListener, GlobalSettingsListener {
    
    private volatile String mySetting = "";

    @Override
    public void afterGlobalSettingsUpdated(
            GlobalSettings oldSettings,
            GlobalSettings newSettings) {

        mySetting = newSettings.getExtended().getString("my-setting");
    }

    @Override
    public void onEvent(
            RequestPhase requestPhase,
            ServeEvent serveEvent,
            Parameters parameters) {
        
        log.debug("My setting is " + mySetting);
    }

    @Override
    public String getName() {
        return "my-listener";
    }
}
```