/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class BinaryEqualToPattern extends ContentPattern<byte[]> {

  public BinaryEqualToPattern(byte[] expected) {
    super(expected);
  }

  @JsonCreator
  public BinaryEqualToPattern(@JsonProperty("binaryEqualTo") String expected) {
    this(Base64.getDecoder().decode(expected));
  }

  @Override
  public MatchResult match(byte[] actual) {
    return MatchResult.of(Arrays.equals(actual, expectedValue));
  }

  @Override
  @JsonIgnore
  public String getName() {
    return "binaryEqualTo";
  }

  @Override
  @JsonIgnore
  public String getExpected() {
    return Base64.getEncoder().encodeToString(expectedValue);
  }

  public String getBinaryEqualTo() {
    return getExpected();
  }

  @Override
  public String toString() {
    return getName() + " " + getExpected();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BinaryEqualToPattern that = (BinaryEqualToPattern) o;

    return Objects.equals(getExpected(), that.getExpected());
  }

  @Override
  public int hashCode() {
    return getExpected() != null ? getExpected().hashCode() : 0;
  }
}
