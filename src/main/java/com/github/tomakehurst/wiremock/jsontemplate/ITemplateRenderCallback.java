package com.github.tomakehurst.wiremock.jsontemplate;

public interface ITemplateRenderCallback {
	void templateDidRender(String renderedString);
}
