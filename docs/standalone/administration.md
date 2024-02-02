---
description: Provides tips on managing standalone WireMock servers
---

# Administration API in WireMock Standalone 

WireMock Standalone offers the REST API for administration, troubleshooting and analysis purposes.
You can find the key use-cases and the full specification below.

## Fetching all of your stub mappings (and checking WireMock is working)

A GET request to the root admin URL e.g `http://localhost:8080/__admin`
will return all currently registered stub mappings.
This is a useful way to check whether WireMock is running on the host and port you expect.

### Shutting Down

To shutdown the server,
post a request with an empty body to `http://<host>:<port>/__admin/shutdown`.

## Full specification

The full specification is available [here](./admin-api-reference.md).
