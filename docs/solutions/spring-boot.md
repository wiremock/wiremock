---
layout: solution
title: "Using WireMock with Spring Boot"
meta_title: Running WireMock with Spring Boot | WireMock
toc_rank: 116
description: The team behind Spring Cloud Contract have created a library to support running WireMock using the “ambient” HTTP server
logo: /images/logos/technology/spring.svg
---

## WireMock Spring Boot

[WireMock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot) 
simplifies testing HTTP clients in Spring Boot & Junit 5 based integration tests.
It includes fully declarative WireMock setup,
supports multiple `WireMockServer` instances,
automatically sets Spring environment properties,
and does not pollute Spring application context with extra beans.

Example:

```java
@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(name = "user-service", property = "user-client.url")
})
class TodoControllerTests {

    @InjectWireMock("user-service")
    private WireMockServer wiremock;
    
    @Autowired
    private Environment env;

    @Test
    void aTest() {
        // returns a URL to WireMockServer instance
        env.getProperty("user-client.url"); 
        wiremock.stubFor(stubFor(get("/todolist").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        [
                            { "id": 1, "userId": 1, "title": "my todo" },
                        ]
                        """)
        )));
    }
}
```

## Spring Cloud Contract

The team behind Spring Cloud Contract have created a library to support running WireMock using the "ambient" HTTP server.
It also simplifies some aspects of configuration and eliminates some common issues that occur when running Spring Boot and WireMock together.

See [Spring Cloud Contract WireMock](https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/project-features.html#features-wiremock) for details.

The article [Faking OAuth2 Single Sign-on in Spring](https://engineering.pivotal.io/post/faking_oauth_sso/)
from Pivotal's blog shows how WireMock can be used to test Spring apps that use 3rd party OAuth2 login.

## Useful pages

- [WireMock on Java and JVM](./jvm.md) - Most of JVM generic solutions are applicable to Spring Boot  development too
