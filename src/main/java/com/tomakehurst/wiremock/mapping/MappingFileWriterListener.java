package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.write;
import static java.util.Collections.max;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;

public class MappingFileWriterListener implements RequestListener {
	
	private FileSource mappingsFileSource;
	private FileSource filesFileSource;
	
	public MappingFileWriterListener(FileSource mappingsFileSource, FileSource filesFileSource) {
		this.mappingsFileSource = mappingsFileSource;
		this.filesFileSource = filesFileSource;
	}

	@Override
	public void requestReceived(Request request, Response response) {
		String mappingFileName = generateNewSequentialFileName(mappingsFileSource, "recorded-mapping");
		String bodyFileName = generateNewSequentialFileName(filesFileSource, "recorded-body");
		
		RequestPattern requestPattern = new RequestPattern(request.getMethod(), request.getUrl());
		ResponseDefinition responseToWrite = new ResponseDefinition();
		responseToWrite.setStatus(response.getStatus());
		responseToWrite.setBodyFileName(bodyFileName);
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, responseToWrite);
		
		filesFileSource.writeTextFile(bodyFileName, response.getBodyAsString());
		mappingsFileSource.writeTextFile(mappingFileName, write(mapping));
	}
	
	private String generateNewSequentialFileName(FileSource fileSource, String prefix) {
		final Pattern pattern = Pattern.compile(prefix + "-(\\d+).json");
		Iterable<TextFile> files = filter(fileSource.list(), matching(pattern));
		Integer maxNumber = 0;
		if (size(files) > 0) {
			maxNumber = max(newArrayList(transform(files, toNumberUsingPattern(pattern))));
		}
		return prefix + "-" + (maxNumber + 1) + ".json";
	}
	
	private Function<TextFile, Integer> toNumberUsingPattern(final Pattern pattern) {
		return new Function<TextFile, Integer>() {
			public Integer apply(TextFile input) {
				Matcher matcher = pattern.matcher(input.name());
				matcher.find();
				return Integer.parseInt(matcher.group());
			}
		};
	}
	
	private Predicate<TextFile> matching(final Pattern pattern) {
		return new Predicate<TextFile>() {
			public boolean apply(TextFile input) {
				return pattern.matcher(input.name()).matches();
			}
		};
	}

}
