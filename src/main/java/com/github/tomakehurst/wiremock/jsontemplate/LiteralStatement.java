package com.github.tomakehurst.wiremock.jsontemplate;


class LiteralStatement implements IStatement {

	private String token;

	public LiteralStatement(String token) {
		this.token = token;
	}

	public void execute(ScopedContext context, ITemplateRenderCallback callback) {
		callback.templateDidRender(this.token);
	}

}
