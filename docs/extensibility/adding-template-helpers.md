---
description: Adding custom Handlebars helpers to the template system via extensions
---

# Adding Custom Template Helpers

Extensions that implement the `TemplateHelperProviderExtension` interface provide additional Handlebars helpers to the templating system:

```java
new WireMockServer(wireMockConfig().extensions(
    new TemplateHelperProviderExtension() {
        @Override
        public String getName() {
            return "custom-helpers";
        }

        @Override
        public Map<String, Helper<?>> provideTemplateHelpers() {
            Helper<String> helper = (context, options) -> context.length();
            return Map.of("string-length", helper);
        }
    }
));
```

This custom `string-length` helper will return the string length of the supplied parameter and is used like this:

{% raw %}

```
{{string-length 'abcde'}}
{{string-length request.body}}
```

{% endraw %}
