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
package com.github.tomakehurst.wiremock.common;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;

public class TextFile {

	private final File file;
	
	public TextFile(final String filePath) {
		file = new File(filePath);
	}
	
	public TextFile(final File file) {
		this.file = file;
	}
	
	public String readContents() {
		try {
			final String json = Files.toString(file, UTF_8);
			return json;
		} catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public String name() {
		return file.getName();
	}
	
	@Override
	public String toString() {
		return file.getName();
	}
}
