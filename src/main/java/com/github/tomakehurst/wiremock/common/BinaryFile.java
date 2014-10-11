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

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class BinaryFile {

    private URI uri;

    public BinaryFile(URI uri) {
        this.uri = uri;
    }
	
	public byte[] readContents() {
		try {
            return ByteStreams.toByteArray(uri.toURL().openStream());
        } catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public String name() {
		return uri.toString();
	}
	
	@Override
	public String toString() {
		return name();
	}
}
