---
description: Adding extra elements to the template model during request processing
---

# Adding Template Model Data

Extensions that implement the `TemplateHelperProviderExtension` interface provide additional Handlebars helpers to the templating system:

```java
new WireMockServer(.extensions(
    new TemplateModelDataProviderExtension() {
        @Override
        public Map<String, Object> provideTemplateModelData(ServeEvent serveEvent) {
            return Map.of(
                "mydata", Map.of("path", serveEvent.getRequest().getUrl()));
        }

        @Override
        public String getName() {
            return "custom-model-data";
        }
    }
));
```

This can then be accessed via the templating system e.g.:

{% raw %}

```handlebars
{{mydata.path}}
```

{% endraw %}