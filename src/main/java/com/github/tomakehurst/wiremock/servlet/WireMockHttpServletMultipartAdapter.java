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
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.http.Request;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.servlet.http.Part;

public class WireMockHttpServletMultipartAdapter implements Request.Part {
    private final Part mPart;

    public WireMockHttpServletMultipartAdapter(Part servletPart) {
        mPart = servletPart;
    }

    public static WireMockHttpServletMultipartAdapter from(Part servletPart) {
        return new WireMockHttpServletMultipartAdapter(servletPart);
    }

    @Override
    public String getName() {
        return mPart.getName();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return mPart.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return mPart.getHeaderNames();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return mPart.getInputStream();
    }
}
