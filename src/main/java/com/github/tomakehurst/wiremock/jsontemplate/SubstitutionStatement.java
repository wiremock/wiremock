package com.github.tomakehurst.wiremock.jsontemplate;


class SubstitutionStatement implements IStatement {

	private IFormatter[] formatters;
	private String name;

	public SubstitutionStatement(String name, IFormatter... formatters) {
		this.name = name;
		this.formatters = formatters;
	}

	public void execute(ScopedContext context, ITemplateRenderCallback callback) {
		Object value;
		if ("@".equals(this.name)) {
			value = context.getCursorValue();
		} else {
			value = context.lookup(this.name);
		}
		for (IFormatter f : formatters) {
			try {
				value = f.format(value);
			} catch (RuntimeException e) {
				throw new EvaluationError(
						String
								.format(
										"Formatting value %s with formatter %s raised exception: %s",
										value.toString(), f.getClass().getName(), e
												.getClass().getSimpleName()), e);
			}
		}
		callback.templateDidRender(value.toString());
	}

}
