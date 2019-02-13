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

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.Exceptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParseDateHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(String context, Options options) throws IOException {
        String format = options.hash("format", null);

        try {
            return format == null ?
                new ISO8601DateFormat().parse(context) :
                new SimpleDateFormat(format).parse(context);
        } catch (ParseException e) {
            return Exceptions.throwUnchecked(e, Object.class);
        }
    }
}
