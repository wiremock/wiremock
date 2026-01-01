# WireMock URL

A type-safe, immutable URL parsing and manipulation library for Java, designed for lenient parsing
and RFC 3986 compliance.

## Overview

WireMock URL provides a robust set of microtypes for working with URLs and their components. The
library balances strict RFC 3986 compliance with practical leniency, accepting all valid RFC 3986
URLs while being permissive with path, query, and fragment components.

## Design Goals

### Core Objectives

- **Type Safety**: Leverage Java's type system with domain-specific microtypes for URL components
- **Ease of Use**: Simple, intuitive API for common URL manipulation tasks (e.g., modifying query
  parameters)
- **Lenient Parsing**: Accept all RFC 3986 compliant URLs with additional leniency:
  - **Path**: Accepts all non-control characters except `?` and `#`
  - **Query**: Accepts all non-control characters except `#`
  - **Fragment**: Accepts all non-control characters
- **Normalization**: RFC 3986 compliant normalization via `normalise()` method, which
  percent-encodes path, query, and fragment components as needed

### Design Principles

#### Immutability

All types are immutable, except for builder classes. This ensures thread safety and prevents
accidental modification.

#### Consistent API

All types provide a standard parsing interface:

```java
static Type parse(CharSequence stringForm);
```

Types implementing `PercentEncoded` additionally provide:

```java
static Type encode(CharSequence unencoded);
```

#### Invariants

The library maintains the following invariants:

1. **Parse Idempotence**: For all types, parsing and converting to string preserves the original
   input:
   ```java
   Type.parse(input).toString().equals(input) == true;
   ```

2. **Normalization Idempotence**: Normalization is idempotent:
   ```java
   instance.normalise().equals(instance.normalise().normalise()) == true;
   ```

3. **Round-Trip Equality**: In general, round-tripping through parse and toString preserves equality:
   ```java
   UrlReference.parse(urlReference.toString()).equals(instance) == true;
   ```

   **Note**: There are edge cases where this is not possible. For example, a `PathAndQuery`
   starting with `//` will be parsed as a `RelativeRef` when converted to string and re-parsed.

## Components

The library provides distinct types for URL components:

- **Url**: Complete absolute URLs
- **UrlReference**: URL references (absolute or relative)
- **Host**: Host component (domain, IPv4, IPv6)
- **Path**: Path component
- **Query**: Query string component
- **Fragment**: Fragment identifier
- **Segment**: Individual path segments

## Usage

### Parsing URLs

```java
Url url = Url.parse("https://example.com/path?query=value#fragment");
```

### Normalization

```java
Host host = Host.parse("EXAMPLE.COM%2fpath");
Host normalized = host.normalise();  // Returns "example.com%2Fpath"
```

### Decoding

```java
Fragment fragment = Fragment.parse("section%20name");
String decoded = fragment.decode();  // Returns "section name"
```
