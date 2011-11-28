package com.tomakehurst.wiremock.common;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.List;

import com.google.common.base.Function;

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
	public List<TextFile> list() {
		File jsonDir = new File(rootPath);
		if (jsonDir.exists() && !jsonDir.isDirectory()) {
			throw new RuntimeException(jsonDir + " is not a directory");
		} else if (!jsonDir.exists()) {
			throw new RuntimeException(jsonDir + " does not exist");
		}
		
		return newArrayList(transform(asList(jsonDir.listFiles()), new Function<File, TextFile>() {
			public TextFile apply(File input) {
				return new TextFile(input.getPath());
			}
		}));
	}
    
}
