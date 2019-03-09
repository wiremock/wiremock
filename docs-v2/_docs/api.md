---
layout: splash
title: Admin API Reference
toc_rank: 120
description: The WireMock Admin REST API.
redirect_from: "/wiremock-admin-api.html"
---

The WireMock admin API is described in [OpenAPI 3.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md). The spec file plus an instance of Swagger UI can be accessed from a running WireMock instance under `/__admin/docs/`, e.g. [http://localhost:8080/__admin/docs/](http://localhost:8080/__admin/docs/)

Below is the full API reference:

<redoc hide-hostname="true" path-in-middle-panel="true" spec-url="{{ base_path }}/assets/js/wiremock-admin-api.json"></redoc>
<script src="{{ base_path }}/assets/js/redoc.standalone.js"></script>
