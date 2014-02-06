package com.github.tomakehurst.wiremock.jsontemplate;

interface IStatement {

	void execute(ScopedContext context, ITemplateRenderCallback callback);

}
