/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayRemoveHelper extends AbstractArrayHelper {

  @Override
  public Object apply(List<?> list, Options options) throws IOException {
    Integer position;
    try {
      position = parsePosition(options);
    } catch (NumberFormatException e) {
      return handleError("position must be 'start', 'end' or an integer");
    }

    final Integer positionToRemove = getFirstNonNull(position, list.size() - 1);
    final ArrayList<Object> mutableList = new ArrayList<>(list);
    if (position != null && (position < 0 || position > mutableList.size())) {
      return handleError(
          "position must be greater than or equal to 0 and less than or equal to the size of the list");
    }
    mutableList.remove((int) positionToRemove);

    return mutableList;
  }
}
