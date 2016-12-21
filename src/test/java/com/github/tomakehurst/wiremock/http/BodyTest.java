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

import com.fasterxml.jackson.databind.node.IntNode;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;


import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BodyTest {

    @Test
    public void constructsFromBytes() {
        Body body = Body.fromOneOf("this content".getBytes(), "not this content", new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(true));
    }

    @Test
    public void constructsFromString() {
        Body body = Body.fromOneOf(null, "this content", new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(false));
    }

    @Test
    public void constructsFromJson() {
        Body body = Body.fromOneOf(null, null, new IntNode(1), "lskdjflsjdflks");

        assertThat(body.asString(), is("1"));
        assertThat(body.isBinary(), is(false));
    }

    @Test
    public void constructsFromBase64() {
        byte[] base64Encoded = Base64.encodeBase64("this content".getBytes());
        String encodedText = stringFromBytes(base64Encoded);
        Body body = Body.fromOneOf(null, null, null, encodedText);

        assertThat(body.asString(), is("this content"));
        assertThat(body.isBinary(), is(true));
    }

}
