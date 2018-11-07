package com.github.tomakehurst.wiremock.extension.plugin;

import java.util.ArrayList;
import java.util.List;

public class ExtensionDefinition {

	private String extensionClassname;

	private List<ArgumentDefinition> arguments = new ArrayList<>();

	public String getExtensionClassname() {
		return extensionClassname;
	}

	public void setExtensionClassname(String extensionClassname) {
		this.extensionClassname = extensionClassname;
	}

	public List<ArgumentDefinition> getArguments() {
		return arguments;
	}

	public void setArguments(List<ArgumentDefinition> arguments) {
		this.arguments = arguments;
	}

}
