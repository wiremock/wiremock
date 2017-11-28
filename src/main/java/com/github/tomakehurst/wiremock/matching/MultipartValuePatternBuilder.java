/*
 * Copyright (C) 2017 Arjan Duijzer
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
package com.github.tomakehurst.wiremock.matching;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class MultipartValuePatternBuilder {
    private Map<String, List<MultiValuePattern>> headerPatterns = newLinkedHashMap();
    private List<ContentPattern<?>> bodyPatterns = new LinkedList<>();
    private MultipartValuePattern.MatchingType matchingType = MultipartValuePattern.MatchingType.ANY;

    public MultipartValuePatternBuilder() {
    }

    public MultipartValuePatternBuilder(String name) {
        withName(name);
    }

    public MultipartValuePatternBuilder matchingType(MultipartValuePattern.MatchingType type) {
        matchingType = type;
        return this;
    }

    public MultipartValuePatternBuilder withName(String name) {
        return withHeader("Content-Disposition", containing("name=\"" + name + "\""));
    }

    public MultipartValuePatternBuilder withHeader(String name, StringValuePattern headerPattern) {
        List<MultiValuePattern> patterns = headerPatterns.get(name);
        if (patterns == null) {
            patterns = new LinkedList<>();
        }
        patterns.add(MultiValuePattern.of(headerPattern));
        headerPatterns.put(name, patterns);

        return this;
    }

    public MultipartValuePatternBuilder withMultipartBody(ContentPattern<?> bodyPattern) {
        bodyPatterns.add(bodyPattern);
        return this;
    }

    public MultipartValuePattern build() {
        return headerPatterns.isEmpty() && bodyPatterns.isEmpty() ? null :
                headerPatterns.isEmpty() ? new MultipartValuePattern(matchingType, null, bodyPatterns) :
                        new MultipartValuePattern(matchingType, headerPatterns, bodyPatterns);
    }
}
