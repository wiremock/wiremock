/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkNotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
  @JsonSubTypes.Type(AbsentPattern.class),
  @JsonSubTypes.Type(AfterDateTimePattern.class),
  @JsonSubTypes.Type(AnythingPattern.class),
  @JsonSubTypes.Type(BeforeDateTimePattern.class),
  @JsonSubTypes.Type(BinaryEqualToPattern.class),
  @JsonSubTypes.Type(ContainsPattern.class),
  @JsonSubTypes.Type(EqualToDateTimePattern.class),
  @JsonSubTypes.Type(EqualToJsonPattern.class),
  @JsonSubTypes.Type(EqualToPattern.class),
  @JsonSubTypes.Type(EqualToXmlPattern.class),
  @JsonSubTypes.Type(LogicalAnd.class),
  @JsonSubTypes.Type(LogicalOr.class),
  @JsonSubTypes.Type(MatchesJsonPathPattern.class),
  @JsonSubTypes.Type(MatchesJsonSchemaPattern.class),
  @JsonSubTypes.Type(MatchesXPathPattern.class),
  @JsonSubTypes.Type(NegativeContainsPattern.class),
  @JsonSubTypes.Type(NegativeRegexPattern.class),
  @JsonSubTypes.Type(NotPattern.class),
  @JsonSubTypes.Type(PathTemplatePattern.class),
  @JsonSubTypes.Type(RegexPattern.class)
})
public abstract class ContentPattern<T> implements NamedValueMatcher<T> {

  protected final T expectedValue;

  public ContentPattern(T expectedValue) {
    if (!isNullValuePermitted()) {
      checkNotNull(expectedValue, "'" + getName() + "' expected value cannot be null");
    }
    this.expectedValue = expectedValue;
  }

  @JsonIgnore
  public T getValue() {
    return expectedValue;
  }

  protected boolean isNullValuePermitted() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ContentPattern<?> that = (ContentPattern<?>) o;

    return Objects.equals(expectedValue, that.expectedValue);
  }

  @Override
  public int hashCode() {
    return expectedValue != null ? expectedValue.hashCode() : 0;
  }
}
