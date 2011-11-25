package com.tomakehurst.wiremock.global;

import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsHolder {

	private AtomicReference<GlobalSettings> globalSettingsRef = new AtomicReference<GlobalSettings>(new GlobalSettings());
	
	public void replaceWith(GlobalSettings globalSettings) {
		globalSettingsRef.set(globalSettings);
	}
	
	public GlobalSettings get() {
		return globalSettingsRef.get();
	}
}
