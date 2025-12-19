Goal:

* Microtypes
* Easy to use
* Handles common cases like changing query parameters
* Lenient - all RFC3986 complient URLs should be accepted. In addition, the path, query and fragment
  should be as lenient as possible, the path accepting all non control characters other than `?` and
  `#`, the query all non-control characters other than `#` and the fragment all non-control
  characters.

Principles:

Apart from builders, all types should be immutable.

All types have the following static method:
`static Type parse(Charsequence stringForm)`

Subtypes of `PercentEncoded` have the following static methods:

`static Type encode(Charsequence unencoded)`

For all types, `Type.parse(input).toString().equals(input) == true`.

For all normalisable types, `instance.normalise().equals(instance.normalise().normalise()) == true`.

In general, `UrlReference.parse(urlReference.toString()).equals(instance)` should be true. There are
a few cases where this is impossible - notably, the `toString()` of a `PathAndQuery` starting with
`//` will be parsed to a `RelativeRef`.
