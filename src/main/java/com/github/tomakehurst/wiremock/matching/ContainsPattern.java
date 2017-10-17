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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.content.Text;

public class ContainsPattern extends StringValuePattern {

    public ContainsPattern(@JsonProperty("contains") Text expectedValue) {
        super(expectedValue);
    }

    public String getContains() {
        return expectedValue.getValue();
    }

    @Override
    public MatchResult match(Text value) {
        return MatchResult.of(value != null && value.getValue().contains(expectedValue.getValue()));
    }
}
