package com.tomakehurst.wiremock.common;


public interface FileSource {

	TextFile getTextFileNamed(String name);
	void createIfNecessary();
}
