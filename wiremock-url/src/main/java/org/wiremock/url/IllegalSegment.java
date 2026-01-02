package org.wiremock.url;

public class IllegalSegment extends IllegalUriPart {
  public IllegalSegment(String segment) {
    super(segment, "Illegal segment: `" + segment + "`");
  }
}
