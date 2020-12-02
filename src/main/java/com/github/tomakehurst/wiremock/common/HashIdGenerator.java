/*
 * Copyright (C) 2011 Thomas Akehurst
 * Copyright (C) 2020 Mark Salisbury
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

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.*;

import static com.github.tomakehurst.wiremock.core.Options.FileIdMethod.*;

public class HashIdGenerator implements IdGenerator {

    private final Options.FileIdMethod method;
    private final byte[] separator = "\n".getBytes(Charsets.UTF_8);

    public HashIdGenerator(Options.FileIdMethod method) {
        if (method != REQUEST_HASH && method != RESPONSE_HASH && method != REQUEST_RESPONSE_HASH)
            throw new IllegalArgumentException("FileIdMethod is not supported: " + method);
        this.method = method;
    }

    @Override
    public String generate(Request request, Response response, byte[] bodyBytes) {
        // Explicitly pull properties from the request/response instead of relying on toString()
        // to be stable and implemented consistently between different implementations of the
        // interface.

        ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
        if (method == REQUEST_HASH || method == REQUEST_RESPONSE_HASH) {
            // GET, POST, etc.
            write(buffer, request.getMethod().getName());
            // /some/api/endpoint?query=blah&k=etc
            write(buffer, request.getUrl());
            // ALL the http headers in the request
            // Should we include all headers?  Headers like Cache-Control: If-Modified-Since could break the hash...
            writeHeaders(buffer, request.getHeaders());
            // ALL the cookies in the request
            String[] cookies = request.getCookies().keySet().toArray(new String[0]);
            Arrays.sort(cookies);
            for (String cookie : cookies) write(buffer, cookie);
            // Write the request body, if there is one.
            byte[] requestBody = request.getBody();
            if (requestBody != null)
                buffer.write(requestBody);
            buffer.write(separator);
            // If this is a multi-part (form upload) request, add each part to the buffer
            Collection<Request.Part> parts = request.getParts();
            if (parts != null) {
                for (Request.Part part : parts) {
                    write(buffer, part.getName());
                    writeHeaders(buffer, part.getHeaders());
                    if (part.getBody() != null && part.getBody().asBytes() != null)
                        buffer.write(part.getBody().asBytes());
                    buffer.write(separator);
                }
            }
        }
        if (method == RESPONSE_HASH || method == REQUEST_RESPONSE_HASH) {
            // 200, 404, 500, etc.
            write(buffer, Integer.toString(response.getStatus()));
            buffer.write(separator);
            if (response.getStatusMessage() != null)
                write(buffer, response.getStatusMessage());
            buffer.write(separator);
            // ALL the http headers in the response
            writeHeaders(buffer, response.getHeaders());
            // Write the response body, if there is one.
            if (bodyBytes != null)
                buffer.write(bodyBytes);
        }
        HashCode code = Hashing.farmHashFingerprint64().hashBytes(buffer.toByteArray());
        return code.toString(); // returns a sequence of hex values representing the binary hash code
    }

    private void write(ByteArrayDataOutput buffer, String content) {
        buffer.write(content.getBytes(Charsets.UTF_8));
        buffer.write(separator);
    }

    private void writeHeaders(ByteArrayDataOutput buffer, HttpHeaders headers) {
        if (headers != null) {
            HttpHeader[] sortedHeaders = headers.all().toArray(new HttpHeader[0]);
            Arrays.sort(sortedHeaders, Comparator.comparing(HttpHeader::key));
            for (HttpHeader header : sortedHeaders) {
                write(buffer, header.key());
                buffer.write(separator);
                for (String value : header.values()) {
                    write(buffer, value);
                    buffer.write(separator);
                }
            }
        }
    }

}
