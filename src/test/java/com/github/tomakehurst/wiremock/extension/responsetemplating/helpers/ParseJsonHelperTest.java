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
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParseJsonHelperTest extends HandlebarsHelperTestBase {
    private ParseJsonHelper helper;

    @BeforeEach
    public void init() {
        helper = new ParseJsonHelper();
    }

    @Test
    public void parsesASimpleJsonObject() throws Exception {
        String inputJson = "{\"testKey1\": \"val1\", \"testKey2\": \"val2\"}";
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(2));
        assertThat(result, hasEntry("testKey1", "val1"));
        assertThat(result, hasEntry("testKey2", "val2"));
    }

    @Test
    public void parsesAJsonObjectContainingArray() throws Exception {
        String inputJson = "{\"arr\": [\"one\", \"two\", \"three\"]}";
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(1));
        assertThat(result, hasKey("arr"));
        assertThat(result.get("arr"), instanceOf(List.class));
        assertThat(result, hasEntry("arr", Arrays.asList(new String[]{"one", "two", "three"})));
    }

    @Test
    public void parseANestedJsonObject() throws Exception {
        String inputJson = "{\"parent\": {\"child\": \"val\"}}";
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        // Check parent level
        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(1));
        assertThat(result, hasKey("parent"));
        // Check child level
        assertThat(result.get("parent"), instanceOf(Map.class));
        Map<String, Object> parent = (Map<String, Object>) result.get("parent");
        assertThat(parent, hasEntry("child", "val"));
    }

    @Test
    public void parsesJsonWithTopLevelArray() throws Exception {
        String inputJson = "[{\"key\": \"val\"}]";
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        // Check list returns
        assertThat(output, instanceOf(List.class));
        List<Object> result = (List<Object>) output;
        assertThat(result, hasSize(1));
        // Check inner is a map
        assertThat(result.get(0), instanceOf(Map.class));
        Map<String, Object> inner = (Map<String, Object>) result.get(0);
        assertThat(inner, hasEntry("key", "val"));
    }

    @Test
    public void parsesNullJsonIfSection() throws Exception {
        String inputJson = null;
        Object output = render(inputJson, new Object[]{}, TagType.SECTION);

        // Check that it returns empty object
        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(0));
    }

    @Test
    public void parsesNullJsonIfNotSection() throws Exception {
        String inputJson = null;
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        // Check that it returns empty object
        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(0));
    }

    @Test
    public void parsesEmptyJsonIfSection() throws Exception {
        String inputJson = "{}";
        Object output = render(inputJson, new Object[]{}, TagType.SECTION);

        // Check that it returns empty object
        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(0));
    }

    @Test
    public void parsesEmptyJsonIfNotSection() throws Exception {
        String inputJson = "{}";
        Object output = render(inputJson, new Object[]{}, TagType.VAR);

        // Check that it returns empty object
        assertThat(output, instanceOf(Map.class));
        Map<String, Object> result = (Map<String, Object>) output;
        assertThat(result, aMapWithSize(0));
    }
    
    private Object render(Object context, Object[] params, TagType tagType) throws IOException {
        return helper.apply(context,
            new Options.Builder(null, null, tagType, createContext(), Template.EMPTY)
                .setParams(params)
                .build()
        );
    }
}