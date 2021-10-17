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

import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import com.google.common.base.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentTypeHeaderTest {
	@Test
	public void returnsMimeTypeAndCharsetWhenBothPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain; charset=utf-8");
		assertThat(header.mimeTypePart(), is("text/plain"));
		Optional<String> encoding = header.encodingPart();
		assertTrue(encoding.isPresent());
		assertThat(encoding.get(), is("utf-8"));
	}

	@Test
	public void returnsMimeTypeWhenNoCharsetPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain");
		assertThat(header.mimeTypePart(), is("text/plain"));
	}

	@Test
	public void returnsCharsetWhenNotFirstParameter() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain; param=value; charset=utf-8");
		Optional<String> encoding = header.encodingPart();
		assertTrue(encoding.isPresent());
		assertThat(encoding.get(), is("utf-8"));
	}

	@Test
	public void returnsAbsentOptionalEncodingPartWhenNotPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain");
		assertFalse(header.encodingPart().isPresent());
	}

	@Test
	public void stripsDoubleQuotesFromEncodingPart() {
		ContentTypeHeader header = new ContentTypeHeader("application/json;charset=\"UTF-8\"");
		Optional<String> encoding = header.encodingPart();
		assertTrue(encoding.isPresent());
		assertThat(encoding.get(), is("UTF-8"));
	}

	@Test
	public void fetchesFromRequest() {
		Request request = new MockRequestBuilder()
				.withHeader("Content-Type", "text/xml")
				.build();

		ContentTypeHeader contentTypeHeader = request.contentTypeHeader();
		assertThat(contentTypeHeader.mimeTypePart(), is("text/xml"));
	}

	@Test
	public void throwsExceptionOnAttemptToSetNullHeaderValue() {
		MockRequestBuilder builder = new MockRequestBuilder()
				.withHeader("Content-Type", null);

		assertThrows(NullPointerException.class, builder::build);
	}

	@Test
	public void returnsNullFromMimeTypePartWhenContentTypeIsAbsent() {
		ContentTypeHeader header = ContentTypeHeader.absent();
		assertThat(header.mimeTypePart(), is(nullValue()));
	}

	@Test
	public void returnsCharsetWhenPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain; charset=iso-8859-1");
		assertThat(header.charset(), is(StandardCharsets.ISO_8859_1));
	}

	@Test
	public void returnsDefaultCharsetWhenEncodingNotPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain");
		assertThat(header.charset(), is(Strings.DEFAULT_CHARSET));
	}

	@Test
	public void returnsDefaultCharsetWhenAbsent() {
		ContentTypeHeader header = ContentTypeHeader.absent();
		assertThat(header.charset(), is(Strings.DEFAULT_CHARSET));
	}

	@Test
	public void returnsNullMimeTypePartWhenHeaderValueIsNull() {
		ContentTypeHeader header = new ContentTypeHeader(null);
		assertNull(header.mimeTypePart());
	}

	@Test
	public void returnsNullMimeTypePartWhenAbsent() {
		ContentTypeHeader header = ContentTypeHeader.absent();
		assertNull(header.mimeTypePart());
	}

	@Test
	public void returnsAbsentWhenNoHeaderValue() {
		ContentTypeHeader header = ContentTypeHeader.absent();

		Assertions.assertFalse(header.encodingPart().isPresent());
	}

	@Test
	public void returnsAbsentWhenNoCharset() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain; charset=");

		Optional<String> encoding = header.encodingPart();
		assertFalse(encoding.isPresent());
	}
}
