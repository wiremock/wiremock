package com.github.tomakehurst.wiremock.standalone;

public class MappingFileException extends RuntimeException {

    public MappingFileException(String filePath, String error) {
        super(String.format("Error loading file %s:\n%s", filePath, error));
    }
}
