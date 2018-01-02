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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.util.Args;

import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;

public class MockMultipart extends AbstractContentBody {
    private final String name;
    private final byte[] body;

    MockMultipart(String name, byte[] body) {
        super(ContentType.APPLICATION_OCTET_STREAM);
        Args.notEmpty(name, "Name was empty");
        Args.notNull(body, "Body was null");
        this.name = name;
        this.body = body;
    }

    MockMultipart(String name, String body, ContentType contentType) {
        super(contentType);
        Args.notEmpty(name, "Name was empty");
        Args.notEmpty(body, "Body was null");
        this.name = name;
        this.body = bytesFromString(body, contentType.getCharset());
    }

    public static MockMultipart part(String name, byte[] body) {
        return new MockMultipart(name, body);
    }

    public static MockMultipart part(String name, String body, ContentType contentType) {
        return new MockMultipart(name, body, contentType);
    }

    @Override
    public String getFilename() {
        return name;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(body);
    }

    @Override
    public String getTransferEncoding() {
        return "7bit";
    }

    @Override
    public long getContentLength() {
        return body.length;
    }
}
