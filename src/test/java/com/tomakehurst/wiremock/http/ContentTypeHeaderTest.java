package com.tomakehurst.wiremock.http;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.testsupport.MockRequestBuilder;

@RunWith(JMock.class)
public class ContentTypeHeaderTest {
	
	private Mockery context;
	
	@Before
	public void init() {
		context = new Mockery();
	}

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
	public void returnsAbsentOptionalEncodingPartWhenNotPresent() {
		ContentTypeHeader header = new ContentTypeHeader("text/plain");
		assertFalse(header.encodingPart().isPresent());
	}
	
	@Test
	public void fetchesFromRequest() {
		Request request = new MockRequestBuilder(context)
			.withHeader("Content-Type", "text/xml")
			.build();
		
		Optional<ContentTypeHeader> optionalHeader = ContentTypeHeader.getFrom(request);
		assertThat(optionalHeader.get().mimeTypePart(), is("text/xml"));
	}
	
	@Test
	public void returnsAbsentOptionalWhenHeaderNotPresentInRequest() {
		Request request = new MockRequestBuilder(context)
			.withHeader("Content-Type", null)
			.build();
	
		Optional<ContentTypeHeader> optionalHeader = ContentTypeHeader.getFrom(request);
		assertFalse(optionalHeader.isPresent());
	}
}
