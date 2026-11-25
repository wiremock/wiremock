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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.google.common.base.CharMatcher;
import java.io.IOException;

/**
 * Removes leading and trailing Unicode whitespace and collapses internal runs to a single space.
 *
 * <p>Available as {@code normaliseWhitespace} and {@code normalizeWhitespace}, with either an
 * inline value ({@code {{normaliseWhitespace request.body}}}) or a block ({@code
 * {{#normaliseWhitespace}}...{{/normaliseWhitespace}}}). Null values render as an empty string.
 */
public class NormaliseWhitespaceHelper implements Helper<Object> {

  @Override
  public Object apply(Object context, Options options) throws IOException {
    String value =
        options.tagType == TagType.SECTION
            ? options.fn(context).toString()
            : context == null ? "" : context.toString();

    return CharMatcher.whitespace().trimAndCollapseFrom(value, ' ');
  }
}
