package com.github.tomakehurst.wiremock.jsontemplate;


public class TemplateCompileOptions {

	private IFormatterResolver moreFormatters;
	private String meta;
	private char formatChar;
	private String defaultFormatter;

	public TemplateCompileOptions() {
		this.moreFormatters = new EmptyFormatterResolver();
		this.meta = "{}";
		this.formatChar = '|';
		this.defaultFormatter = "str";
	}

	public IFormatterResolver getMoreFormatters() {
		return this.moreFormatters;
	}

	static class EmptyFormatterResolver implements IFormatterResolver {
		public IFormatter getFormatter(String formatterName) {
			return null;
		}
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String getMeta() {
		return this.meta;
	}

	public char getFormatChar() {
		return this.formatChar;
	}

	public void setFormatChar(char formatChar) {
		this.formatChar = formatChar;
	}

	public String getDefaultFormatter() {
		return this.defaultFormatter;
	}

	public void setDefaultFormatter(String defaultFormatter) {
		this.defaultFormatter = defaultFormatter;

	}

}
