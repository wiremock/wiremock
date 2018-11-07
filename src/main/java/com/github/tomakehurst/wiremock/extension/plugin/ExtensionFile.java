package com.github.tomakehurst.wiremock.extension.plugin;

import java.util.ArrayList;
import java.util.List;

public class ExtensionFile {

	private List<HelperDefinition> helpers = new ArrayList<>();

	private List<ExtensionDefinition> extensionList = new ArrayList<>();

	public List<HelperDefinition> getHelpers() {
		return helpers;
	}

	public void setHelpers(List<HelperDefinition> helpers) {
		this.helpers = helpers;
	}

	public List<ExtensionDefinition> getExtensionList() {
		return extensionList;
	}

	public void setExtensionList(List<ExtensionDefinition> extensionList) {
		this.extensionList = extensionList;
	}

}
