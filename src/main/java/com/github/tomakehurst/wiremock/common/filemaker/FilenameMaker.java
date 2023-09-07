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

import static com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine.defaultTemplateEngine;

import com.github.tomakehurst.wiremock.extension.responsetemplating.HandlebarsOptimizedTemplate;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class FilenameMaker {
  public static final String DEFAULT_FILENAME_TEMPLATE =
      "{{#if name}}{{{name}}}{{else}}{{{method}}}-{{{url}}}{{/if}}-{{{id}}}";
  private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-.]");
  private static final String DEFAULT_EXTENSION = ".json";
  private static final String POINT = ".";
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  private final TemplateEngine templateEngine;
  private final String filenameTemplate;

  public FilenameMaker() {
    this(null);
  }

  public FilenameMaker(String filenameTemplate) {
    this.templateEngine = defaultTemplateEngine();
    this.filenameTemplate = filenameTemplate != null ? filenameTemplate : DEFAULT_FILENAME_TEMPLATE + DEFAULT_EXTENSION;
  }

  public FilenameMaker(String filenameTemplate, String extension) {
    this.templateEngine = defaultTemplateEngine();
    if (filenameTemplate.equals("default")) {
      this.filenameTemplate = DEFAULT_FILENAME_TEMPLATE + POINT + extension;
    } else {
      this.filenameTemplate = filenameTemplate + POINT + extension;
    }
  }

  public String filenameFor(StubMapping stubMapping) {
    HandlebarsOptimizedTemplate template = templateEngine.getUncachedTemplate(filenameTemplate);

    final FilenameTemplateModel templateModel = new FilenameTemplateModel(stubMapping);
    String parsedFilename = template.apply(templateModel);
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
    String decoratedString = String.join("-", s.split(" "));
    return NON_ALPHANUMERIC.matcher(decoratedString).replaceAll("").toLowerCase(Locale.ROOT);
  }
}
