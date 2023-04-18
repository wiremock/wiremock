/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.filemaker;

import com.github.tomakehurst.wiremock.extension.responsetemplating.HandlebarsOptimizedTemplate;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class FilenameMaker {
  private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-.]");
  private static final String DEFAULT_EXTENSION = ".json";
  private static final String DEFAULT_FILENAME_TEMPLATE =
      "{{{request.method}}}-{{{request.url}}}-{{{id}}}";
  private static final String POINT = ".";
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  private final TemplateEngine templateEngine;
  private final String filenameTemplate;

  public FilenameMaker() {
    this.templateEngine = new TemplateEngine(Collections.emptyMap(), null, Collections.emptySet());
    this.filenameTemplate = DEFAULT_FILENAME_TEMPLATE + DEFAULT_EXTENSION;
  }

  public FilenameMaker(String filenameTemplate) {
    this.templateEngine = new TemplateEngine(Collections.emptyMap(), null, Collections.emptySet());
    this.filenameTemplate = filenameTemplate;
  }

  public FilenameMaker(String filenameTemplate, String extension) {
    this.templateEngine = new TemplateEngine(Collections.emptyMap(), null, Collections.emptySet());
    if (filenameTemplate.equals("default")) {
      this.filenameTemplate = DEFAULT_FILENAME_TEMPLATE + POINT + extension;
    } else {
      this.filenameTemplate = filenameTemplate + POINT + extension;
    }
  }

  public String filenameFor(StubMapping stubMapping) {
    HandlebarsOptimizedTemplate template = templateEngine.getUncachedTemplate(filenameTemplate);
    String parsedFilename = template.apply(stubMapping);
    return sanitise(parsedFilename);
  }

  public String sanitizeUrl(String url) {
    String startingPath = url.replace("/", "_");
    String pathWithoutWhitespace = WHITESPACE.matcher(startingPath).replaceAll("-");
    String normalizedPath = Normalizer.normalize(pathWithoutWhitespace, Normalizer.Form.NFD);
    String slug = sanitise(normalizedPath).replaceAll("^[_]*", "").replaceAll("[_]*$", "");
    slug = StringUtils.truncate(slug, 200);
    return slug;
  }

  private String sanitise(String s) {
    return NON_ALPHANUMERIC.matcher(s).replaceAll("").toLowerCase(Locale.ROOT);
  }
}
