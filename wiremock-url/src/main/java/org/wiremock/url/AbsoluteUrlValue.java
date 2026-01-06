package org.wiremock.url;

import org.jspecify.annotations.Nullable;
import java.util.Objects;

class AbsoluteUrlValue implements AbsoluteUrl {

  private final Scheme scheme;
  private final Authority authority;
  private final Path path;
  private final @Nullable Query query;

  AbsoluteUrlValue(Scheme scheme, Authority authority, Path path, @Nullable Query query) {
    this.scheme = scheme;
    this.authority = authority;
    this.path = path;
    this.query = query;
  }

  @Override
  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public boolean equals(Object obj) {
    return UriReferenceParser.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return UriReferenceParser.hashCode(this);
  }

  @Override
  public String toString() {
    return UriReferenceParser.toString(this);
  }

  @Override
  public AbsoluteUrl normalise() {
    Scheme normalisedScheme = scheme.normalise();
    Authority normalisedAuthority = authority.normalise(normalisedScheme);
    Path normalisedPath = path.normalise();
    if (normalisedPath.isEmpty()) {
      normalisedPath = Path.ROOT;
    }
    Query normalisedQuery = query == null ? null : query.normalise();

    if (scheme.equals(normalisedScheme)
        && authority.equals(normalisedAuthority)
        && path.equals(normalisedPath)
        && Objects.equals(query, normalisedQuery)) {
      return this;
    } else {
      return (AbsoluteUrl)
          UriReference.builder()
              .setScheme(normalisedScheme)
              .setAuthority(normalisedAuthority)
              .setPath(normalisedPath)
              .setQuery(normalisedQuery)
              .build();
    }
  }

  @Override
  public Scheme getScheme() {
    return scheme;
  }

  @Override
  public Authority getAuthority() {
    return authority;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public @Nullable Query getQuery() {
    return query;
  }
}
