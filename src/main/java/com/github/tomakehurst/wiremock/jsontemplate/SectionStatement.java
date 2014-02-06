package com.github.tomakehurst.wiremock.jsontemplate;


class SectionStatement implements IStatement {

	private Section block;

	public SectionStatement(Section block) {
		this.block = block;
	}

	public void execute(ScopedContext context, ITemplateRenderCallback callback) {
		// push a context first
		Object cursorPosition = context
				.pushSection(this.block.getSectionName());
		if (!context.isEmptyContext(cursorPosition)) {
			TemplateExecutor.execute(this.block.getStatements(), context,
					callback);
			context.pop();
		} else {
			context.pop();
			TemplateExecutor.execute(this.block.getStatements("or"), context,
					callback);
		}
	}

}
