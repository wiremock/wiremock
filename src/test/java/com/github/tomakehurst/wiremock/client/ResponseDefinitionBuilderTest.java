/*
 * Copyright (C) 2017 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseDefinitionBuilderTest {

    @Test
    public void withTransformerParameterShouldNotChangeOriginalTransformerParametersValue() {
        ResponseDefinition originalResponseDefinition = ResponseDefinitionBuilder
            .responseDefinition()
            .withTransformerParameter("name", "original")
            .build();

        ResponseDefinition transformedResponseDefinition = ResponseDefinitionBuilder
            .like(originalResponseDefinition)
            .but()
                .withTransformerParameter("name", "changed")
            .build();

        assertThat(originalResponseDefinition.getTransformerParameters().getString("name"), is("original"));
        assertThat(transformedResponseDefinition.getTransformerParameters().getString("name"), is("changed"));
    }
}
