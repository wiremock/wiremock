package com.tomakehurst.wiremock.common;

import java.io.File;

public class SingleRootFileSource implements FileSource {

	private final String rootPath;

	public SingleRootFileSource(final String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public TextFile getTextFileNamed(final String name) {
		return new TextFile(rootPath + File.separator + name);
	}

    @Override
    public void createIfNecessary() {
        final File file = new File(rootPath);
        if (file.exists() && file.isFile()) {
            throw new IllegalStateException(file + " already exists and is a file");
        } else if (!file.exists()) {
            file.mkdirs();
        }
    }
}
