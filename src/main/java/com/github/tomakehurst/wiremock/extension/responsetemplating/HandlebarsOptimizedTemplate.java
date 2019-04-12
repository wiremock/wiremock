package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class HandlebarsOptimizedTemplate {

	private final Template template;

	private String startContent;
	private String templateContent;
	private String endContent;

	public HandlebarsOptimizedTemplate(final Handlebars handlebars, final String content) throws IOException {
		startContent = content;
		templateContent = "";
		endContent = "";

		int firstDelimStartPosition = content.indexOf(Handlebars.DELIM_START);
		if (firstDelimStartPosition != -1) {
			int lastDelimEndPosition = content.lastIndexOf(Handlebars.DELIM_END);
			if (lastDelimEndPosition != -1) {
				startContent = content.substring(0, firstDelimStartPosition);
				templateContent = content.substring(firstDelimStartPosition,
						lastDelimEndPosition + Handlebars.DELIM_END.length());
				endContent = content.substring(lastDelimEndPosition + Handlebars.DELIM_END.length(), content.length());
			}
		}
		this.template = handlebars.compileInline(templateContent);
	}

	public String apply(Object context) throws IOException {
		StringBuilder sb = new StringBuilder();
		return sb.append(startContent).append(template.apply(context)).append(endContent).toString();
	}
}
