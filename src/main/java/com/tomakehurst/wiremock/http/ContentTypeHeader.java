package com.tomakehurst.wiremock.http;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.mapping.Request;

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
