/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import java.io.IOException;
import java.util.List;

public class JoinHelper extends HandlebarsHelper<Object> {

  @Override
  public Object apply(Object context, Options options) throws IOException {

    if (context == null || !(context instanceof String)) {
      return handleError("Separator parameter must be a String");
    }

    String separator = (String) context;

    Object param = options.param(0, null);
    if (param == null || !(Iterable.class.isAssignableFrom(param.getClass()))) {
      return handleError("The parameter must be list");
    }
    List<Object> items = (List<Object>) param;
    if (items.isEmpty()) {
      return "";
    }
    String result;
    if (options.tagType == TagType.SECTION) {
      result = processSection(options, separator, items);
    } else {
      result = processWithoutSection(separator, items);
    }

    return result;
  }

  private static String processWithoutSection(String separator, List<Object> items) {
    StringBuilder sb = new StringBuilder();
    boolean initialised = false;

    for (Object item : items) {
      if (initialised) {
        sb.append(separator);
      }
      sb.append(item.toString());
      initialised = true;
    }
    return sb.toString();
  }

  private static String processSection(Options options, String separator, List<Object> list)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      CharSequence itemRendered = options.fn(list.get(i));
      sb.append(itemRendered);
      if (i < list.size() - 1) {
        sb.append(separator);
      }
    }

    return sb.toString();
  }
}
