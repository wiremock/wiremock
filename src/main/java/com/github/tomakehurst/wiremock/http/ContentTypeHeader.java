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

import com.google.common.base.Optional;
import com.github.tomakehurst.wiremock.mapping.Request;

public class ContentTypeHeader {

	public static final String KEY = "Content-Type";
	
	private String[] parts;

	public static Optional<ContentTypeHeader> getFrom(Request request) {
		String value = request.getHeader(KEY);
		if (value != null) {
			return Optional.of(new ContentTypeHeader(value));
		}
		
		return Optional.absent();
	}
	
	public ContentTypeHeader(String stringValue) {
		parts = stringValue.split(";");
	}
	
	public String mimeTypePart() {
		return parts[0];
	}
	
	public Optional<String> encodingPart() {
		if (parts.length < 2) {
			return Optional.absent();
		}
		
		return Optional.of(parts[1].split("=")[1]);
	}
}
