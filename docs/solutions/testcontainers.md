---
layout: solution
title: "WireMock and Testcontainers"
meta_title: "Testcontainers Solutions | WireMock"
description: "Additional solutions for WireMock when using Testcontainers"
logo: /images/logos/technology/testcontainers.svg
hide-disclaimer: true
---

The WireMock community provides modules for [Testcontainers](https://testcontainers.com/).
They allow provisioning the WireMock server as a standalone container within your tests,
based on [WireMock Docker](https://github.com/wiremock/wiremock-docker).

All the modules are under active development.
If there is no module implemented for your technology stack,
a `GenericContainer` implementation from Testcontainers can be used.
For features that are not implemented yet in Module APIs for your language,
it is possible to use the [Administrative REST API](../standalone/administration.md).
Feedback and contributions are welcome!

See WireMock on the [Testcontainers modules listing](https://testcontainers.com/modules/wiremock/).

## Official Testcontainers modules

WireMock Inc. partners with AtomicJar Inc,
a company stewarding the Testcontainers open source project
and providing Testcontainers Cloud and Testcontainers Desktop
([Partnership Announcement](https://www.wiremock.io/post/atomicjar-partnership-on-testcontainers)).
As a part of the partnership,
the following modules were reviewed and certified
as the official modules:

**Java and other JVM languages.**
Java implementation is a separate library that is available to all
JVM languages, e.g. Java, Kotlin or Scala.
See full documentation in the [GitHub Repository](https://github.com/wiremock/wiremock-testcontainers-java).

**Python.**
The Testcontainers Python module is a part of the
[Python WireMock](https://github.com/wiremock/python-wiremock) library,
so a single library integrates bot the CLI client and the Testcontainers module.
See [this page](https://wiremock.readthedocs.io/en/latest/testcontainers/)
for all documentation and examples.

**Golang.**
Golang implementation is a multi-platform library that includes the Testcontainers module only.
The module's full documentation and examples are available in its
[GitHub Repository](https://github.com/wiremock/wiremock-testcontainers-go).
There is a separate library for the CLI, see the [Golang Solutions page](./golang.md).

## Experimental modules

**C/C++ and other native languages.**
We created a WireMock module for
[Testcontainers for C/C++](https://github.com/oleg-nenashev/testcontainers-c).
It allows provisioning the WireMock server as a standalone container within your tests, based on [WireMock Docker](../standalone/docker.md).
It allows using WireMock with all popular C/C++ testing frameworks
like Google Test, CTest, Doctest, QtTest or CppUnit.
Read More: [C/C++ Solutions Page](./c_cpp.md).

## Other Languages

All Testcontainers implementations provide
API for provisioning custom containers,
also known as _Generic Container_ API.
It allows using WireMock on platforms where
there is no special Testcontainers module implemented yet:
Node.js, Rust, Haskell, Ruby, etc.

## Code examples

Examples of using the Testcontainers Modules for different languages and Testcontainers modules:

=== "Java"

    ```java
    import org.junit.jupiter.api.*;
    import org.testcontainers.junit.jupiter.*;
    import org.wiremock.integrations.testcontainers.testsupport.http.*;
    import static org.assertj.core.api.Assertions.assertThat;

    @Testcontainers
    class WireMockContainerJunit5Test {

        @Container
        WireMockContainer wiremockServer = new WireMockContainer("2.35.0")
                .withMapping("hello", WireMockContainerJunit5Test.class, "hello-world.json");

        @Test
        void helloWorld() throws Exception {
            String url = wiremockServer.getUrl("/hello");
            HttpResponse response = new TestHttpClient().get(url);
            assertThat(response.getBody())
                    .as("Wrong response body")
                    .contains("Hello, world!");
        }
    }
    ```

=== "Python"

    ```python
    import pytest
    from wiremock.testing.testcontainer import wiremock_container

    @pytest.fixture(scope="session") # (1)
    def wm_server():
        with wiremock_container(secure=False) as wm:
            Config.base_url = wm.get_url("__admin") # (2)=
            Mappings.create_mapping(
                Mapping(
                    request=MappingRequest(method=HttpMethods.GET, url="/hello"),
                    response=MappingResponse(status=200, body="hello"),
                    persistent=False,
                )
            ) # (3)
            yield wm

    def test_get_hello_world(wm_server): # (4)
        resp1 = requests.get(wm_server.get_url("/hello"), verify=False)
        assert resp1.status_code == 200
        assert resp1.content == b"hello"
    ```

=== "Golang"

    ```golang
    package testcontainers_wiremock_quickstart

    import (
        "context"
        "testing"

        . "github.com/wiremock/wiremock-testcontainers-go"
    )

    func TestWireMock(t *testing.T) {
        ctx := context.Background()
        mappingFileName := "hello-world.json"

        container, err := RunContainerAndStopOnCleanup(ctx, t,
            WithMappingFile(mappingFileName),
        )
        if err != nil {
            t.Fatal(err)
        }

        statusCode, out, err := SendHttpGet(container, "/hello", nil)
        if err != nil {
            t.Fatal(err, "Failed to get a response")
        }

        // Verify the response
        if statusCode != 200 {
            t.Fatalf("expected HTTP-200 but got %d", statusCode)
        }

        if string(out) != "Hello, world!" {
            t.Fatalf("expected 'Hello, world!' but got %s", out)
        }
    }
    ```

## Coming soon

The following modules are under prototyping at the moment: `.NET`, `Rust`.
A lot more features can be implemented in the listed modules,
and any contributions are welcome!
If you are interested, join us on the [community Slack](http://slack.wiremock.org/).

## References

- Devoxx BE talk on API Integration testing with Testcontainers and WireMock,
by Oleg Nenashev and Oleg Shelaev:
([Video](https://www.youtube.com/watch?v=eFILbyaMI2A),
[Slides](https://docs.google.com/presentation/d/e/2PACX-1vQSgTTCg-LkmrL-5UuAE63zxuWP0kADBetXXBqMVO-oEQWfP6zGu16eFSdKxvEbchDnaCwKZ2a7134F/pub?start=false&loop=false&delayms=3000))
