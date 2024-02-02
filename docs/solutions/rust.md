---
layout: solution
title: "WireMock and Rust"
meta_title: "Rust Solutions | WireMock"
description: "Additional solutions for WireMock when using Rust"
logo: /images/logos/technology/rust.svg
---

## wiremock-rs. Server implementation in Rust

[LukeMathWalker/wiremock-rs](https://github.com/LukeMathWalker/wiremock-rs) is an API Mock Server implementation in Rust.
It provides HTTP mocking to perform black-box testing of Rust applications that interact with third-party APIs.

This project is inspired by WireMock and has the same name in the documentation,
but it is not compatible with WireMock when it comes to CLI, REST API or configuration files.
Please refer to its documentation for more details and guidelines.

```rust
use wiremock::{MockServer, Mock, ResponseTemplate};
use wiremock::matchers::{method, path};

#[async_std::main]
async fn main() {
    // Start a background HTTP server on a random local port
    let mock_server = MockServer::start().await;

    // Arrange the behaviour of the MockServer adding a Mock:
    // when it receives a GET request on '/hello' it will respond with a 200.
    Mock::given(method("GET")).and(path("/hello"))
        .respond_with(ResponseTemplate::new(200))
        .mount(&mock_server).await;

    // Verify the response
    let status = surf::get(format!("{}/hello", &mock_server.uri()))
        .await.unwrap().status();
    assert_eq!(status.as_u16(), 200);
}
```

References:

- Crates: [`wiremock`](https://crates.io/crates/wiremock)
- Documentation: [docs.rs/wiremock](https://docs.rs/wiremock/latest/wiremock/)
- GitHub: [LukeMathWalker/wiremock-rs](https://github.com/LukeMathWalker/wiremock-rs)

## Stubr

[Stubr](https://github.com/beltram/stubr) is an adaptation of `wiremock-rs`
supporting existing WireMock json stubs as input.
It aims at reaching feature parity with WireMock.
The project also provides support for gRPC and offers Docker images.

```rust
use asserhttp::*;

#[tokio::test]
async fn getting_started() {
    // run a mock server with the stub 👇
    let stubr = stubr::Stubr::start("tests/stubs/hello.json").await;
    // or use 'start_blocking' for a non-async version

    // the mock server started on a random port e.g. '127.0.0.1:43125'
    // so we use the stub instance 'path' (or 'uri') method to get the address back
    let uri = stubr.path("/hello");
    reqwest::get(uri).await
        // (optional) use asserhttp for assertions
        .expect_status_ok()
        .expect_content_type_text()
        .expect_body_text_eq("Hello stubr");
}
```

References:

- Crates: [`stubr`](https://crates.io/crates/stubr)
- [Documentation](https://beltram.github.io/stubr/html/)
- [GitHub Repository](https://github.com/beltram/stubr)

## Testcontainers module

We are interested in providing a Testcontainers for Rust module that
would provide SDK for the official [WireMock Docker images](../standalone/docker.md).
This module is on our roadmap but have not been published yet,
see [wiremock/ecosystem #8](https://github.com/wiremock/ecosystem/issues/8).
Contributions are welcome!
