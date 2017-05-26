package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;

import java.net.URI;
import java.util.List;

public class NoFileSource implements FileSource {

    public static NoFileSource noFileSource() {
        return new NoFileSource();
    }

    @Override
    public BinaryFile getBinaryFileNamed(String name) {
        return null;
    }

    @Override
    public TextFile getTextFileNamed(String name) {
        return null;
    }

    @Override
    public void createIfNecessary() {

    }

    @Override
    public FileSource child(String subDirectoryName) {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public List<TextFile> listFilesRecursively() {
        return null;
    }

    @Override
    public void writeTextFile(String name, String contents) {

    }

    @Override
    public void writeBinaryFile(String name, byte[] contents) {

    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public void deleteFile(String name) {

    }
}
