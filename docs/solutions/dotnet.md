---
layout: solution
title: "WireMock and .NET"
meta_title: ".NET Solutions | WireMock"
description: "Additional solutions for WireMock when using .NET"
logo: /images/logos/technology/dotnet.svg
---


## WireMock.Net

A .NET implementation of a API mock server in C# based on
[mock4net](https://github.com/alexvictoor/mock4net)
It mimics the functionality from [WireMock](https://github.com/wiremock/wiremock) implemented in Java.
WireMock.NET can be used with all .NET based languages,
both .NET Framework and .NET Core are supported.
It can also be deployed as a standalone server, including Windows service and a container.

**Compatibility Notice**.
WireMock.Net is not fully compatible with WireMock
in terms of the configuration file formats and Administrative REST API.

References:

- [Main repository](https://github.com/WireMock-Net/WireMock.Net)
- [WireMock.Net Docker images](https://github.com/WireMock-Net/WireMock.Net-docker) for Linux and Windows
- [WireMock.Net Examples](https://github.com/WireMock-Net/WireMock.Net-examples)


## WireMockInspector

WireMockInspector is a cross platform UI app that facilitates WireMock troubleshooting.
It presents a list of requests received by the WireMock.Net server,
combines request data with associated mapping,
presents a list of all available mappings with the definition,
generate C# code for defining selected mappings.

**Compatibility Notice**.
The tool is designed for WireMock.Net and not fully compatible with WireMock

WireMockInspector is distributed as `dotnet tool` so it can be easily install on Windows/MacOS/Linux.

References:

- [GitHub Repository](https://github.com/WireMock-Net/WireMockInspector)

## Wiremock UI

Tool for creating mock servers,
proxies servers and proxies servers with the option to save the data traffic from an existing API or Site.
It is a wrapper over WireMock.

**Compatibility Notice**.
The tool is designed for WireMock and not fully compatible with WireMock.Net

References:

- [GitHub repository](https://github.com/juniorgasparotto/WiremockUI)
