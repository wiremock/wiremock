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

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;

public class Base64Helper implements Helper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        String value = options.tagType == TagType.SECTION ?
                options.fn(context).toString() :
                context.toString();

        if (Boolean.TRUE.equals(options.hash.get("decode"))) {
            return new String(decodeBase64(value));
        }

        Object paddingOption = options.hash.get("padding");
        boolean padding = paddingOption == null || Boolean.TRUE.equals(paddingOption);
        return encodeBase64(value.getBytes(), padding);
    }
}
