package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CommandLineOptionsTest {

	@Test
	public void returnsVerboseTrueWhenOptionPresent() {
		CommandLineOptions options = new CommandLineOptions("--verbose");
		assertThat(options.verboseLoggingEnabled(), is(true));
	}
	
	@Test
	public void returnsVerboseFalseWhenOptionNotPresent() {
		CommandLineOptions options = new CommandLineOptions("");
		assertThat(options.verboseLoggingEnabled(), is(false));
	}

	@Test
	public void returnsRecordMappingsTrueWhenOptionPresent() {
		CommandLineOptions options = new CommandLineOptions("--record-mappings");
		assertThat(options.recordMappingsEnabled(), is(true));
	}
	
	@Test
	public void returnsRecordMappingsFalseWhenOptionNotPresent() {
		CommandLineOptions options = new CommandLineOptions("");
		assertThat(options.recordMappingsEnabled(), is(false));
	}
	
	@Test
	public void setsPortNumberWhenOptionPresent() {
		CommandLineOptions options = new CommandLineOptions("--port", "8086");
		assertThat(options.specifiesPortNumber(), is(true));
		assertThat(options.portNumber(), is(8086));
	}
	
	@Test(expected=Exception.class)
	public void throwsExceptionWhenPortNumberSpecifiedWithoutNumber() {
		new CommandLineOptions("--port");
	}
	
	@Test
	public void setsProxyAllRootWhenOptionPresent() {
		CommandLineOptions options = new CommandLineOptions("--proxy-all", "http://someotherhost.com/site");
		assertThat(options.specifiesProxyUrl(), is(true));
		assertThat(options.proxyUrl(), is("http://someotherhost.com/site"));
	}
	
	@Test(expected=Exception.class)
	public void throwsExceptionWhenProxyAllSpecifiedWithoutUrl() {
		new CommandLineOptions("--proxy-all");
	}
	
	@Test
	public void setsAll() {
		CommandLineOptions options = new CommandLineOptions("--verbose", "--record-mappings", "--port", "8088", "--proxy-all", "http://somewhere.com");
		assertThat(options.verboseLoggingEnabled(), is(true));
		assertThat(options.recordMappingsEnabled(), is(true));
		assertThat(options.specifiesPortNumber(), is(true));
		assertThat(options.portNumber(), is(8088));
		assertThat(options.specifiesProxyUrl(), is(true));
		assertThat(options.proxyUrl(), is("http://somewhere.com"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void returnsHelpText() {
		CommandLineOptions options = new CommandLineOptions("--help");
		assertThat(options.helpText(), allOf(containsString("verbose")));
	}
}
