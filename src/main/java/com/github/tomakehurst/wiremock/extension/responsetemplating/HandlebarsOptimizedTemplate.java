package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.TypeSafeTemplate;

public class HandlebarsOptimizedTemplate implements Template {

	private static final String NOT_IMPLEMENTED = "not implemented";

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

	@Override
	public void apply(Object context, Writer writer) throws IOException {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}
	
	@Override
	public String apply(Object context) throws IOException {
		StringBuilder sb = new StringBuilder();
		return sb.append(startContent).append(template.apply(context)).append(endContent).toString();
	}

	@Override
	public void apply(Context context, Writer writer) throws IOException {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public String apply(Context context) throws IOException {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public String text() {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public String toJavaScript() {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public <T, S extends TypeSafeTemplate<T>> S as(Class<S> type) {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public <T> TypeSafeTemplate<T> as() {
		return template.as();
	}

	@Override
	public List<String> collect(TagType... tagType) {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> collectReferenceParameters() {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public String filename() {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public int[] position() {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}
}
