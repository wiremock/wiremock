/*
 * Copyright (C) 2011 Thomas Akehurst
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
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickRandomHelper extends HandlebarsHelper<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (context == null) {
            return this.handleError("Must specify either a single list argument or a set of single value arguments.");
        }

        List<Object> valueList = (Iterable.class.isAssignableFrom(context.getClass())) ?
                ImmutableList.copyOf((Iterable<Object>) context) :
                ImmutableList.builder().add(context).add(options.params).build();

        int index = ThreadLocalRandom.current().nextInt(valueList.size());
        return valueList.get(index).toString();
    }
}
