package com.tomakehurst.wiremock.common;

import java.io.File;

public class SingleRootFileSource implements FileSource {

	private String rootPath;

	public SingleRootFileSource(String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public TextFile getTextFileNamed(String name) {
		return new TextFile(rootPath + File.separator + name);
	}
}
