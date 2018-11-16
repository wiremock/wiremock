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
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.Validator;

public class EqualToXsdPattern extends StringValuePattern {

    private Validator validator = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);

    public EqualToXsdPattern(
        @JsonProperty("equalToXsd") String testValue
    ) {
        super(testValue);
        validator.setSchemaSource(Input.fromString(testValue).build());
    }

    public String getEqualTo() {
        return expectedValue;
    }

    @Override
    public MatchResult match(final String value) {
        return MatchResult.of(validator.validateInstance(Input.fromString(value).build()).isValid());
    }
}
