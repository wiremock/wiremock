---
description: Adding additional source of loading mappings via extensions.
---

# Mappings Loader Extensions

Additional source to load the stub mappings can be configured by implementing `MappingsLoaderExtension`.

```java
public class DummyMappingsLoaderExtension extends MappingsLoaderExtension {
  @Override
  public String getName() {
    return "dummy-mappings-loader"; // Return the name of extension
  }

  @Override
  public void loadMappingsInto(StubMappings stubMappings) {
        // implementation to load the mappings
        // mappings can be loaded from any source like git repo, database, file storage, stc
  }
}
```

Registering the extension with wiremock.

```java
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig()
                .extensions(new DummyMappingsLoaderExtension())
                ); // Register your extension here
```