---
description: >
    attach arbitrary metadata to stub mappings,
    for use in later search, deletion, or retrieval.
---

# Associating and using metadata with API stubs

It is possible to attach arbitrary metadata to stub mappings, which can be later used for search, deletion, or retrieval.

## Adding metadata to stubs

Data under the `metadata` key is a JSON object (represented in Java by a `Map<String, ?>`). It can be added to a stub mapping on creation.

=== "Java"

    ```java
    stubFor(get("/with-metadata")
        .withMetadata(metadata()
            .attr("singleItem", 1234)
            .list("listItem", 1, 2, 3, 4)
            .attr("nestedObject", metadata()
                .attr("innerItem", "Hello")
            )
    ));
    ```

=== "JSON"

    ```json
    {
        "request": {
            "url": "/with-metadata"
        },
        "response": {
            "status": 200
        },

        "metadata": {
            "singleItem": 1234,
            "listItem": [1, 2, 3, 4],
            "nestedObject": {
                "innerItem": "Hello"
            }
        }
    }
    ```

## Search for stubs by metadata

Stubs can be found by matching against their metadata using the same matching strategies as when [matching HTTP requests](./request-matching.md).
The most useful matcher for this is `matchesJsonPath`:

=== "Java"

    ```java
    List<StubMapping> stubs =
        findStubsByMetadata(matchingJsonPath("$.singleItem", containing("123")));
    ```

=== "JSON"

    ```json
    POST /__admin/mappings/find-by-metadata

    {
        "matchesJsonPath" : {
        "expression" : "$.singleItem",
        "contains" : "123"
        }
    }
    ```

## Remove stubs by metadata

Similarly, stubs with matching metadata can be removed:

=== "Java"

    ```java
    removeStubsByMetadata(matchingJsonPath("$.singleItem", containing("123")));
    ```

=== "JSON"

    POST /__admin/mappings/remove-by-metadata

    ```json
    {
        "matchesJsonPath" : {
        "expression" : "$.singleItem",
        "contains" : "123"
        }
    }
    ```

## Remove request journal events by metadata

See [Removing items from the journal](./verifying.md#by-criteria)
