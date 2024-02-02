---
layout: solution
title: "WireMock and C/C++"
meta_title: "C/C++ Solutions | WireMock"
description: "Additional solutions for WireMock when developing with C/C++"
logo: /images/logos/technology/c.png
og_image: solutions/testcontainers/testcontainers_c_opengraph.png
---

## Testcontainers for C/C++ module

<img src="/images/solutions/testcontainers/testcontainers_c_logo_wide.png" alt="Testcontainers C" style="width: 60%; height: auto; margin-top: 1em;"/>

Recently we created an experimental WireMock module for
[Testcontainers for C/C++](https://github.com/oleg-nenashev/testcontainers-c).
It allows provisioning the WireMock server as a standalone container within your tests, based on [WireMock Docker](../standalone/docker.md).
It allows using WireMock with all popular C/C++ testing frameworks
like Google Test, CTest, Doctest, QtTest or CppUnit.

The module is distributed as a shared library and a header,
and hence can be potentially included into other programming languages that support
including native C libraries, for example Lua, D, Swift, etc.
None of that has been tested yet, so we will appreciate your contributions!

### Examples

Initializing WireMock:

```c
#include <stdio.h>
#include <string.h>
#include "testcontainers-c-wiremock.h"

int main() {
    printf("Creating new container: %s\n", DEFAULT_WIREMOCK_IMAGE);
    int requestId = tc_wm_new_default_container();
    tc_wm_with_mapping(requestId, "test_data/hello.json", "hello");
    tc_with_file(requestId, "test_data/hello.json", "/home/wiremock/mappings/hello2.json");
    struct tc_run_container_return ret = tc_run_container(requestId);
    int containerId = ret.r0;
    if (!ret.r1) {
        printf("Failed to run the container: %s\n", ret.r2);
        if (containerId != -1) { // Print container log
            char* log = tc_get_container_log(containerId);
            if (log != NULL) {
                printf("\n%s\n", log);
            }
        }
        return -1;
    }

    // ...
```

Sending HTTP requests

```c
    //..

    struct WireMock_Mapping mapping = tc_wm_get_mappings(containerId);
    if (mapping.responseCode != 200) {
        printf("Failed to get WireMock mapping: %s\n", mapping.error);
        return -1;
    } else {
        printf("WireMock Mapping:\n%s\n", mapping.json);
    }

    printf("Sending HTTP request to the container\n");
    struct tc_send_http_get_return response = tc_send_http_get(containerId, 8080, "/hello");
    if (response.r0 == -1) {
        printf("Failed to send HTTP request: %s\n", response.r2);
        return -1;
    }
    if (response.r0 != 200) {
        printf("Received wrong response code: %d instead of %d\n%s\n", response.r0, 200, response.r2);
        return -1;
    }
    printf("Server Response: HTTP-%d\n%s\n\n", response.r0, response.r1);
    return 0;
}
```
