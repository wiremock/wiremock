package com.github.tomakehurst.wiremock.common.ssl;

import com.google.common.io.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class ReadOnlyFileOrClasspathKeyStoreSource extends KeyStoreSource {

    protected final String path;

    public ReadOnlyFileOrClasspathKeyStoreSource(String path, String keyStoreType, char[] keyStorePassword) {
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
                try {
                    URL pathUrl = new URL(path);
                    return pathUrl.openStream();
                } catch (MalformedURLException ignored) {
                    return Resources.getResource(path).openStream();
                }
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
    public void save(KeyStore keyStore) {}

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReadOnlyFileOrClasspathKeyStoreSource that = (ReadOnlyFileOrClasspathKeyStoreSource) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
