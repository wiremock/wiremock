package org.wiremock.url;

import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface Segment extends PctEncoded {
  Segment EMPTY = new SegmentImpl("");
  Segment DOT = new SegmentImpl(".");
  Segment DOT_DOT = new SegmentImpl("..");

  default boolean isDot() {
    return decode().equals(Segment.DOT.toString());
  }

  default boolean isDotDot() {
    return decode().equals(Segment.DOT_DOT.toString());
  }
}

record SegmentImpl(String stringForm) implements Segment {

  @Override
  public String decode() {
    try {
      return URLDecoder.decode(stringForm, UTF_8);
    } catch (IllegalArgumentException ignored) {
      return stringForm;
    }
  }

  @Override
  public int length() {
    return stringForm.length();
  }

  @Override
  public char charAt(int index) {
    return stringForm.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return stringForm.subSequence(start, end);
  }

  @Override
  public String toString() {
    return stringForm;
  }
}
