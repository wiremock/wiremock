package com.github.tomakehurst.wiremock.jsontemplate;


/**
 * Represents a compiled template.
 * 
 * <p>
 * Like many template systems, the template string is compiled into a program,
 * and then it can be expanded any number of times. For example, in a web app,
 * you can compile the templates once at server startup, and use the expand()
 * method at request handling time. expand() uses the compiled representation.
 * </p>
 * 
 * <p>
 * There are various options for controlling parsing &mdash; see
 * {@link TemplateCompiler} and {@link TemplateCompileOptions} Don't go crazy
 * with metacharacters. {}, [], {{}} or &lt;&gt; should cover nearly any
 * circumstance, e.g. generating HTML, CSS XML, JavaScript, C programs, text
 * files, etc.
 * </p>
 * 
 * 
 */
public class Template {
	private Section program;

	public Template(String template, IProgramBuilder builder,
			TemplateCompileOptions options) {
		this.program = TemplateCompiler.compile(template, builder, options);
	}

	public Template(String template) {
		this(template, null, null);
	}

	public Template(String template, IProgramBuilder builder) {
		this(template, builder, null);
	}

	public Template(String template, TemplateCompileOptions options) {
		this(template, null, options);
	}

	/**
	 * Low level method to expands the template piece by piece.
	 * 
	 * @param object
	 *            The JSON data dictionary
	 * @param callback
	 *            A callback which should be called with each expanded token
	 */
	public void render(Object object, ITemplateRenderCallback callback) {
		TemplateExecutor.execute(this.program.getStatements(),
				new ScopedContext(object), callback);
	}

	/**
	 * Expands the template with the given data dictionary, returning a string.
	 * 
	 * @param object
	 *            The JSON data dictionary
	 * @return The expansion of the template with the given data dictionary.
	 */
	public String expand(Object object) {
		final StringBuilder stringBuilder = new StringBuilder();
		this.render(object, new ITemplateRenderCallback() {

			public void templateDidRender(String renderedString) {
				stringBuilder.append(renderedString);

			}

		});
		return stringBuilder.toString();
	}
}
