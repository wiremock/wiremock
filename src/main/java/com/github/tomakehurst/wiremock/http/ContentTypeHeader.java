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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.Strings;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.LITERAL;

public class ContentTypeHeader extends HttpHeader {

    public static final String KEY = "Content-Type";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=(.+)");
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";", LITERAL);

    private final String[] parts;

    public ContentTypeHeader(String stringValue) {
        super(KEY, stringValue);
        parts = stringValue != null ? SEMICOLON_PATTERN.split(stringValue) : new String[0];
    }

    private ContentTypeHeader() {
        super(KEY);
        parts = new String[0];
    }

    public static ContentTypeHeader absent() {
        return new ContentTypeHeader();
    }

    public ContentTypeHeader or(String stringValue) {
        return isPresent() ? this : new ContentTypeHeader(stringValue);
    }

    public String mimeTypePart() {
        return parts.length > 0 ? parts[0] : null;
    }

    public Optional<String> encodingPart() {
        for (int i = 1; i < parts.length; i++) {
            Matcher matcher = CHARSET_PATTERN.matcher(parts[i]);
            if (matcher.find()) {
                return Optional.of(StringUtils.unwrap(matcher.group(1), '"'));
            }
        }

        return Optional.absent();
    }

    public Charset charset() {
        Optional<String> e;
        if (isPresent() && (e = encodingPart()).isPresent()) {
            return Charset.forName(e.get());
        }

        return Strings.DEFAULT_CHARSET;
    }
}
