/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static java.util.Collections.emptyList;

import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayAddHelper extends AbstractArrayHelper {

  @Override
  public Object apply(List<?> list, Options options) throws IOException {
    if (list == null) {
      return emptyList();
    }

    final ArrayList<Object> mutableList = new ArrayList<>(list);

    if (options.params.length == 0) {
      return handleError("Missing required parameter: additional value to add to list");
    }

    final Object newValue = options.params[0];

    Integer position;
    try {
      position = parsePosition(options);
    } catch (NumberFormatException e) {
      return handleError("position must be 'start', 'end' or an integer");
    }

    if (position != null) {
      mutableList.add(position, newValue);
    } else {
      mutableList.add(newValue);
    }

    return mutableList;
  }
}
