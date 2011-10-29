package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.mapping.HeaderPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;

public class MappingBuilderTest {

	@Test
	public void shouldBuildMappingWithExactUrlNoHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/match/this");
		RequestResponseMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.willReturn(new ResponseDefinitionBuilder().withStatus(201))
			.build();
		
		assertThat(mapping.getRequest().getUrl(), is("/match/this"));
		assertThat(mapping.getRequest().getMethod(), is(POST));
		assertThat(mapping.getResponse().getStatus(), is(201));
	}
	
	@Test
	public void shouldBuildMappingWithMatchedUrlNoHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrlPattern("/match/[A-Z]{5}");
		RequestResponseMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.willReturn(new ResponseDefinitionBuilder())
			.build();
		
		assertThat(mapping.getRequest().getUrlPattern(), is("/match/[A-Z]{5}"));
	}
	
	@Test
	public void shouldBuildMappingWithExactUrlAndRequestHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/match/this");
		
		RequestResponseMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.withHeader("Content-Type", headerStrategyEqualTo("text/plain"))
			.withHeader("Encoding", headerStrategyMatches("UTF-\\d"))
			.withHeader("X-My-Thing", headerStrategyDoesNotMatch("[A-Z]+"))
			.willReturn(new ResponseDefinitionBuilder())
			.build();
		
		assertThat(mapping.getRequest().getHeaders(), hasEntry("Content-Type", headerEqualTo("text/plain")));
		assertThat(mapping.getRequest().getHeaders(), hasEntry("Encoding", headerMatches("UTF-\\d")));
		assertThat(mapping.getRequest().getHeaders(), hasEntry("X-My-Thing", headerDoesNotMatch("[A-Z]+")));
	}
	
	@Test
	public void shouldBuildMappingWithResponseBody() {
		RequestResponseMapping mapping =
			new MappingBuilder(POST, new UrlMatchingStrategy())
			.willReturn(new ResponseDefinitionBuilder().withBody("Some content"))
			.build();
		
		assertThat(mapping.getResponse().getBody(), is("Some content"));
	}
	
	@Test
	public void shouldBuildMappingWithResponseHeaders() {
		RequestResponseMapping mapping =
			new MappingBuilder(POST, new UrlMatchingStrategy())
			.willReturn(new ResponseDefinitionBuilder()
				.withHeader("Content-Type", "text/xml")
				.withHeader("Encoding", "UTF-8"))
			.build();
		
		assertThat(mapping.getResponse().getHeaders(), hasEntry("Content-Type", "text/xml"));
		assertThat(mapping.getResponse().getHeaders(), hasEntry("Encoding", "UTF-8"));
	}
	
	private HeaderPattern headerEqualTo(String value) {
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setEqualTo(value);
		return headerPattern;
	}
	
	private HeaderPattern headerMatches(String value) {
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setMatches(value);
		return headerPattern;
	}
	
	private HeaderPattern headerDoesNotMatch(String value) {
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setDoesNotMatch(value);
		return headerPattern;
	}
	
	private HeaderMatchingStrategy headerStrategyEqualTo(String value) {
		HeaderMatchingStrategy headerStrategy = new HeaderMatchingStrategy();
		headerStrategy.setEqualTo(value);
		return headerStrategy;
	}
	
	private HeaderMatchingStrategy headerStrategyMatches(String value) {
		HeaderMatchingStrategy headerStrategy = new HeaderMatchingStrategy();
		headerStrategy.setMatches(value);
		return headerStrategy;
	}
	
	private HeaderMatchingStrategy headerStrategyDoesNotMatch(String value) {
		HeaderMatchingStrategy headerStrategy = new HeaderMatchingStrategy();
		headerStrategy.setDoesNotMatch(value);
		return headerStrategy;
	}
}
