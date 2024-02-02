---
layout: solution
title: "WireMock and GraphQL"
meta_title: "GraphQL Solutions | WireMock"
description: "Additional solutions for WireMock when using Golang"
logo: /images/logos/technology/graphql.svg
og_image: solutions/graphql/wiremock_graphql_opengraph.png
hide-disclaimer: true
---

## WireMock Extension

There is a [GraphQL extension for WireMock](https://github.com/wiremock/wiremock-graphql-extension)
that allows semantically matching GraphQL queries,
regardless of the order of the fields in the original request.
It brings powers of request matching and response templating to the
[GraphQL](https://graphql.org/) query language.

Example:

```kotlin
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.nilwurtz.GraphqlBodyMatcher

fun registerGraphQLWiremock(json: String) {
    WireMock(8080).register(
        post(urlPathEqualTo(endPoint))
            .andMatching(GraphqlBodyMatcher.extensionName, GraphqlBodyMatcher.withRequest(json))
            .willReturn(
                aResponse()
                    .withStatus(200)
            )
    )
}
```

## Read More

- [GraphQL API mocking with the new WireMock extension](https://www.wiremock.io/post/graphql-api-mocking-with-the-new-wiremock-extension?utm_medium=referral&utm_source=wiremock.org&utm_content=solution-page)
  blogpost by Eiki Hayashi
- [GitHub repository with documentation](https://github.com/wiremock/wiremock-graphql-extension)
