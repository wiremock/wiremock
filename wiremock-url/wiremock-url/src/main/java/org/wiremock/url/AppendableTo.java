/*
 * Copyright (C) 2026 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.url;

import java.util.stream.IntStream;

interface AppendableTo {

  void appendTo(StringBuilder builder);
}

@SuppressWarnings("unused")
class AppendableToAwareStringBuilder implements CharSequence {

  final StringBuilder delegate = new StringBuilder();

  @SuppressWarnings("UnusedReturnValue")
  AppendableToAwareStringBuilder append(AppendableTo appendableTo) {
    appendableTo.appendTo(delegate);
    return this;
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @SuppressWarnings("unused")
  public int compareTo(StringBuilder another) {
    return delegate.compareTo(another);
  }

  public int indexOf(String str) {
    return delegate.indexOf(str);
  }

  @Override
  public IntStream chars() {
    return delegate.chars();
  }

  public int codePointBefore(int index) {
    return delegate.codePointBefore(index);
  }

  public AppendableToAwareStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
    delegate.insert(dstOffset, s, start, end);
    return this;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return delegate.subSequence(start, end);
  }

  public int capacity() {
    return delegate.capacity();
  }

  public AppendableToAwareStringBuilder deleteCharAt(int index) {
    delegate.deleteCharAt(index);
    return this;
  }

  public int offsetByCodePoints(int index, int codePointOffset) {
    return delegate.offsetByCodePoints(index, codePointOffset);
  }

  public AppendableToAwareStringBuilder append(char[] str, int offset, int len) {
    delegate.append(str, offset, len);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, float f) {
    delegate.insert(offset, f);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, char[] str) {
    delegate.insert(offset, str);
    return this;
  }

  public AppendableToAwareStringBuilder appendCodePoint(int codePoint) {
    delegate.appendCodePoint(codePoint);
    return this;
  }

  public AppendableToAwareStringBuilder reverse() {
    delegate.reverse();
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, String str) {
    delegate.insert(offset, str);
    return this;
  }

  public AppendableToAwareStringBuilder append(double d) {
    delegate.append(d);
    return this;
  }

  public AppendableToAwareStringBuilder append(long lng) {
    delegate.append(lng);
    return this;
  }

  public AppendableToAwareStringBuilder append(CharSequence s) {
    delegate.append(s);
    return this;
  }

  public void setCharAt(int index, char ch) {
    delegate.setCharAt(index, ch);
  }

  public void setLength(int newLength) {
    delegate.setLength(newLength);
  }

  public int lastIndexOf(String str) {
    return delegate.lastIndexOf(str);
  }

  public AppendableToAwareStringBuilder append(int i) {
    delegate.append(i);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int index, char[] str, int offset, int len) {
    delegate.insert(index, str, offset, len);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, char c) {
    delegate.insert(offset, c);
    return this;
  }

  public AppendableToAwareStringBuilder append(Object obj) {
    delegate.append(obj);
    return this;
  }

  public AppendableToAwareStringBuilder append(char c) {
    delegate.append(c);
    return this;
  }

  public int codePointAt(int index) {
    return delegate.codePointAt(index);
  }

  public AppendableToAwareStringBuilder append(boolean b) {
    delegate.append(b);
    return this;
  }

  public String substring(int start) {
    return delegate.substring(start);
  }

  public AppendableToAwareStringBuilder replace(int start, int end, String str) {
    delegate.replace(start, end, str);
    return this;
  }

  @Override
  public int length() {
    return delegate.length();
  }

  public AppendableToAwareStringBuilder insert(int dstOffset, CharSequence s) {
    delegate.insert(dstOffset, s);
    return this;
  }

  @Override
  public IntStream codePoints() {
    return delegate.codePoints();
  }

  public AppendableToAwareStringBuilder insert(int offset, double d) {
    delegate.insert(offset, d);
    return this;
  }

  public int codePointCount(int beginIndex, int endIndex) {
    return delegate.codePointCount(beginIndex, endIndex);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public AppendableToAwareStringBuilder append(char[] str) {
    delegate.append(str);
    return this;
  }

  public void ensureCapacity(int minimumCapacity) {
    delegate.ensureCapacity(minimumCapacity);
  }

  public String substring(int start, int end) {
    return delegate.substring(start, end);
  }

  public AppendableToAwareStringBuilder delete(int start, int end) {
    delegate.delete(start, end);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, long l) {
    delegate.insert(offset, l);
    return this;
  }

  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  public AppendableToAwareStringBuilder append(float f) {
    delegate.append(f);
    return this;
  }

  public AppendableToAwareStringBuilder insert(int offset, int i) {
    delegate.insert(offset, i);
    return this;
  }

  public AppendableToAwareStringBuilder append(CharSequence s, int start, int end) {
    delegate.append(s, start, end);
    return this;
  }

  public void trimToSize() {
    delegate.trimToSize();
  }

  public AppendableToAwareStringBuilder insert(int offset, Object obj) {
    delegate.insert(offset, obj);
    return this;
  }

  public int lastIndexOf(String str, int fromIndex) {
    return delegate.lastIndexOf(str, fromIndex);
  }

  public AppendableToAwareStringBuilder append(StringBuffer sb) {
    delegate.append(sb);
    return this;
  }

  @Override
  public char charAt(int index) {
    return delegate.charAt(index);
  }

  public AppendableToAwareStringBuilder insert(int offset, boolean b) {
    delegate.insert(offset, b);
    return this;
  }

  public AppendableToAwareStringBuilder append(String str) {
    delegate.append(str);
    return this;
  }

  public int indexOf(String str, int fromIndex) {
    return delegate.indexOf(str, fromIndex);
  }
}
