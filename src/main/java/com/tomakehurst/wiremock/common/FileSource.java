package com.tomakehurst.wiremock.common;

import java.util.List;


public interface FileSource {

	TextFile getTextFileNamed(String name);
	void createIfNecessary();
	FileSource child(String subDirectoryName);
	String getPath();
	List<TextFile> list();
	void writeTextFile(String name, String contents);
}
