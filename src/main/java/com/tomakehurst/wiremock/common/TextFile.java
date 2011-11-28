package com.tomakehurst.wiremock.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.io.CharStreams;

public class TextFile {

	private File file;
	
	public TextFile(String filePath) {
		file = new File(filePath);
	}
	
	public TextFile(File file) {
		this.file = file;
	}
	
	public String readContents() {
		try {
			String json = CharStreams.toString(new FileReader(file));
			return json;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public String name() {
		return file.getName();
	}
}
