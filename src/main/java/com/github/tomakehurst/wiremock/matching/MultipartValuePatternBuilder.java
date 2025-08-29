/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** The type Multipart value pattern builder. */
public class MultipartValuePatternBuilder {

  private String name = null;
  private String filename = null;
  private Map<String, MultiValuePattern> headerPatterns = new LinkedHashMap<>();
  private List<ContentPattern<?>> bodyPatterns = new LinkedList<>();
  private MultipartValuePattern.MatchingType matchingType = MultipartValuePattern.MatchingType.ANY;

  /** Instantiates a new Multipart value pattern builder. */
  public MultipartValuePatternBuilder() {}

  /**
   * Instantiates a new Multipart value pattern builder.
   *
   * @param name the name
   */
  public MultipartValuePatternBuilder(String name) {
    withName(name);
  }

  /**
   * Matching type multipart value pattern builder.
   *
   * @param type the type
   * @return the multipart value pattern builder
   */
  public MultipartValuePatternBuilder matchingType(MultipartValuePattern.MatchingType type) {
    matchingType = type;
    return this;
  }

  /**
   * With name multipart value pattern builder.
   *
   * @param name the name
   * @return the multipart value pattern builder
   */
  public MultipartValuePatternBuilder withName(String name) {
    this.name = name;
    return withHeader("Content-Disposition", containing("name=\"" + name + "\""));
  }

  /**
   * With file name multipart value pattern builder.
   *
   * @param filename the filename
   * @return the multipart value pattern builder
   */
  public MultipartValuePatternBuilder withFileName(String filename) {
    this.filename = filename;
    return withHeader("Content-Disposition", containing("filename=\"" + filename + "\""));
  }

  /**
   * With header multipart value pattern builder.
   *
   * @param name the name
   * @param headerPattern the header pattern
   * @return the multipart value pattern builder
   */
  public MultipartValuePatternBuilder withHeader(String name, StringValuePattern headerPattern) {
    headerPatterns.put(name, MultiValuePattern.of(headerPattern));
    return this;
  }

  /**
   * With body multipart value pattern builder.
   *
   * @param bodyPattern the body pattern
   * @return the multipart value pattern builder
   */
  public MultipartValuePatternBuilder withBody(ContentPattern<?> bodyPattern) {
    bodyPatterns.add(bodyPattern);
    return this;
  }

  /**
   * Build multipart value pattern.
   *
   * @return the multipart value pattern
   */
  public MultipartValuePattern build() {
    return headerPatterns.isEmpty() && bodyPatterns.isEmpty()
        ? null
        : headerPatterns.isEmpty()
            ? new MultipartValuePattern(name, filename, matchingType, null, bodyPatterns)
            : new MultipartValuePattern(name, filename, matchingType, headerPatterns, bodyPatterns);
  }
}
