/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.global;

import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsHolder {

	private AtomicReference<GlobalSettings> globalSettingsRef = new AtomicReference<>(GlobalSettings.defaults());
	
	public void replaceWith(GlobalSettings globalSettings) {
		globalSettingsRef.set(globalSettings);
	}
	
	public GlobalSettings get() {
		return globalSettingsRef.get();
	}
}
