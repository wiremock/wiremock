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

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class UniqueFilenameGeneratorTest {

    private Mockery context;

    @Before
    public void init() {
        context = new Mockery();
    }

    @Test
    public void generatesValidNameWhenRequestHasUrlWithTwoPathNodes() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/some/path").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-some-path-random123.json"));
    }

    @Test
    public void generatesValidNameWhenRequestHasUrlWithOnePathNode() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/thing").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-thing-random123.json"));
    }

    @Test
    public void generatesValidNameWhenRequestHasRootPath() {
        String fileName = UniqueFilenameGenerator.generate(
                aRequest(context).withUrl("/").build(),
                "body",
                "random123");

        assertThat(fileName, is("body-(root)-random123.json"));
    }
}
