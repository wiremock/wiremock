package org.wiremock.url;

public interface Segment extends PctEncoded {
  Segment EMPTY = new SegmentImpl("");
  Segment DOT = new SegmentImpl(".");
  Segment DOT_DOT = new SegmentImpl("..");
}

record SegmentImpl(String stringForm) implements Segment {

  @Override
  public String decode() {
    throw new UnsupportedOperationException();
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
