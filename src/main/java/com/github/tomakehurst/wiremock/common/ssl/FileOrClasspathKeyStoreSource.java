package com.github.tomakehurst.wiremock.common.ssl;

import com.google.common.io.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class FileOrClasspathKeyStoreSource extends AbstractKeyStoreSource {

    private final String path;

    public FileOrClasspathKeyStoreSource(String path, String keyStoreType, char[] keyStorePassword) {
        super(keyStoreType, keyStorePassword);
        this.path = path;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected InputStream createInputStream() {
        try {
            if (exists()) {
                return new FileInputStream(path);
            } else {
                return Resources.getResource(path).openStream();
            }
        } catch (IOException e) {
            return throwUnchecked(e, InputStream.class);
        }
    }

    @Override
    public boolean exists() {
        return new File(path).isFile();
    }

    @Override
    public void save(KeyStore item) {

    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileOrClasspathKeyStoreSource that = (FileOrClasspathKeyStoreSource) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
