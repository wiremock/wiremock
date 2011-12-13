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
package com.tomakehurst.wiremock.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.io.Files;

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

	@Override
	public FileSource child(String subDirectoryName) {
		return new SingleRootFileSource(rootPath + separator + subDirectoryName);
	}

	@Override
	public String getPath() {
		return rootPath;
	}

	@Override
	public List<TextFile> listFiles() {
		assertExistsAndIsDirectory();
		File jsonDir = new File(rootPath);
		List<File> fileList = asList(jsonDir.listFiles(filesOnly()));
		return toTextFileList(fileList);
	}

	
	@Override
	public List<TextFile> listFilesRecursively() {
		assertExistsAndIsDirectory();
		File jsonDir = new File(rootPath);
		List<File> fileList = newArrayList();
		recursivelyAddFilesToList(jsonDir, fileList);
		return toTextFileList(fileList);
	}
	
	private void recursivelyAddFilesToList(File root, List<File> fileList) {
		File[] files = root.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				recursivelyAddFilesToList(file, fileList);
			} else {
				fileList.add(file);
			}
		}
	}
	
	private List<TextFile> toTextFileList(List<File> fileList) {
		return newArrayList(transform(fileList, new Function<File, TextFile>() {
			public TextFile apply(File input) {
				return new TextFile(input.getPath());
			}
		}));
	}

	private void assertExistsAndIsDirectory() {
		File jsonDir = new File(rootPath);
		if (jsonDir.exists() && !jsonDir.isDirectory()) {
			throw new RuntimeException(jsonDir + " is not a directory");
		} else if (!jsonDir.exists()) {
			throw new RuntimeException(jsonDir + " does not exist");
		}
	}
	
	@Override
	public void writeTextFile(String name, String contents) {
		assertExistsAndIsDirectory();
		File toFile = new File(rootPath, name);
		try {
			Files.write(contents, toFile, UTF_8);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	private FileFilter filesOnly() {
		return new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		};
	}
}
