/*
 * Copyright (C) 2020-2024 Thomas Akehurst
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickRandomHelper extends HandlebarsHelper<Object> {

  @Override
  @SuppressWarnings("unchecked")
  public Object apply(Object context, Options options) throws IOException {
    if (context == null) {
      return this.handleError(
          "Must specify either a single list argument or a set of single value arguments.");
    }

    List<Object> valueList = new ArrayList<>();
    if (Iterable.class.isAssignableFrom(context.getClass())) {
      ((Iterable<Object>) context).forEach(valueList::add);
    } else {
      valueList.add(context);
      valueList.addAll(Arrays.asList(options.params));
    }

    int index = ThreadLocalRandom.current().nextInt(valueList.size());
    return valueList.get(index);
  }
}
