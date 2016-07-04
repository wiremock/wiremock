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
package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Joiner;

import java.net.URI;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Math.min;

public class UniqueFilenameGenerator {
    public static final int MAX_FILENAME_WITHOUT_EXTENSION = 240; 
    
    public static String generate(Request request, String prefix, String id) {
        URI uri = URI.create(request.getUrl());
        Iterable<String> uriPathNodes = on("/").omitEmptyStrings().split(uri.getPath());
        int nodeCount = size(uriPathNodes);

        String pathPart = nodeCount > 0 ?
                Joiner.on("-").join(from(uriPathNodes).skip(nodeCount - min(nodeCount, 2))) :
                "(root)";
                
        StringBuilder stringBuilder = new StringBuilder(prefix)
                .append("-")
                .append(pathPart)
                .append("-")
                .append(id);
        trimTooLongFileName(stringBuilder);
        stringBuilder.append(".json");
        
        return stringBuilder.toString();
    }

    private static void trimTooLongFileName(StringBuilder stringBuilder) {
        if (stringBuilder.length() > MAX_FILENAME_WITHOUT_EXTENSION) {
                stringBuilder.delete(MAX_FILENAME_WITHOUT_EXTENSION, Integer.MAX_VALUE);
        }
    }
}
